package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;

/**
 * 记录层编码解码
 * 
 * @author ZhangXi 2025年2月10日
 */
abstract class RecordCoder extends TLS {

	/**
	 * 指示当前连接是否已握手（已收到对端公钥）
	 */
	protected abstract boolean handshaked();

	/**
	 * 解密缓存数据中指定部分，并通过新的缓存对象返回解密后的数据。<br>
	 * 不要释放传入的缓存对象，其中可能还有后续消息的数据。
	 */
	protected abstract DataBuffer decrypt(DataBuffer buffer, int length) throws Exception;

	/**
	 * 加密全部缓存数据，并返回加密后的数据。<br>
	 * 如果返回新的缓存对象，应将传入的缓存对象释放。
	 */
	protected abstract DataBuffer encrypt(DataBuffer buffer) throws Exception;

	/**
	 * 解码数据为握手消息对象，并执行必要的消息哈希计算已用于密钥导出计划。
	 */
	protected abstract Handshake decode(DataBuffer buffer) throws Exception;

	/**
	 * 编码握手消息对象为数据，获取必要的消息哈希结果按需导出流量密钥。
	 */
	protected abstract void encode(Handshake handshake, DataBuffer buffer) throws Exception;

	/**
	 * 通知接收到的握手消息，执行必要的协商处理。
	 */
	protected abstract void received(ChainChannel<Object> chain, Handshake handshake) throws Exception;

	/**
	 * 编码除APPLICATION_DATA之外的记录消息，视连接状态加密消息<br>
	 * APPLICATION_DATA记录将直接由DataBuffer实例包装为记录数据
	 */
	protected DataBuffer encode(Record record) throws Exception {
		// 兼容性消息
		if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
			return encode((ChangeCipherSpec) record);
		}

		final DataBuffer data = DataBuffer.instance();
		if (record.contentType() == Record.HANDSHAKE) {
			encode((Handshake) record, data);
		} else if (record.contentType() == Record.HEARTBEAT) {
			encode((HeartbeatMessage) record, data);
		} else if (record.contentType() == Record.INVALID) {
			encode((Invalid) record, data);
		} else if (record.contentType() == Record.ALERT) {
			encode((Alert) record, data);
		} else {
			throw new UnsupportedOperationException("TLS:意外的记录类型" + record.contentType());
		}
		if (handshaked()) {
			return encodeCiphertext(record, data);
		} else {
			return encodePlaintext(record, data);
		}
	}

	/**
	 * 编码明文记录，如有必要执行分块
	 */
	protected DataBuffer encodePlaintext(Record record, DataBuffer data) throws IOException {
		final DataBuffer buffer = DataBuffer.instance();

		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(record.contentType());
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX);
			// opaque
			data.transfer(buffer, Record.PLAINTEXT_MAX);
		}

		// ContentType 1Byte
		buffer.writeByte(record.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(data.readable());
		// opaque
		data.transfer(buffer);
		data.release();
		return buffer;
	}

	/**
	 * 编码APPLICATION_DATA的记录消息，始终加密消息，如有必要执行分块和填充
	 */
	protected DataBuffer encodeCiphertext(Record record, DataBuffer data) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();

		while (data.readable() > Record.PLAINTEXT_MAX) {
			DataBuffer temp = DataBuffer.instance();
			// ContentType + PADDING(0)
			// 负载已满时无填充数据(padding)
			data.transfer(temp, Record.PLAINTEXT_MAX);
			temp.writeByte(record.contentType());

			// cipher.encryptAdditional(temp.readable());
			// cipher.encryptFinal(temp);
			temp = encrypt(temp);

			// ContentType 1Byte
			buffer.writeByte(Record.APPLICATION_DATA);
			// ProtocolVersion 2Byte
			buffer.writeShort(TLS.V12);
			// length 2Byte(uint16)
			buffer.writeShort(temp.readable());
			// opaque
			temp.transfer(buffer);
			temp.release();
		}

		// ContentType 1Byte
		data.writeByte(record.contentType());
		// PADDING(*)
		int p = Record.PLAINTEXT_MAX - data.readable();
		if (p > 16) {
			p = 16 - (p % 16);
		} else if (p > 8) {
			p = 8 - (p % 8);
		} else if (p > 4) {
			p = 4 - (p % 4);
		} else {
			p = 4 - p;
		}
		while (p-- > 0) {
			data.writeByte(0);
		}

		// cipher.encryptAdditional(data.readable());
		// cipher.encryptFinal(data);
		data = encrypt(data);

		// ContentType 1Byte
		buffer.writeByte(Record.APPLICATION_DATA);
		// ProtocolVersion 2Byte
		buffer.writeShort(TLS.V12);
		// length 2Byte(uint16)
		buffer.writeShort(data.readable());
		// opaque
		data.transfer(buffer);
		// buffer.write(data);
		data.release();
		// System.out.println(buffer);
		return buffer;
	}

	/**
	 * 解码数据为消息，如果数据不足以解析消息对象则返回null，如果为应用数据之外的消息已记录对象返回；
	 * 应用数据以DataBuffer返回，注意：不会返回实例化的ApplicationData对象。
	 * 
	 * @return null/Record/DataBuffer
	 */
	protected Object decode(ChainChannel<Object> chain, DataBuffer buffer) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		int type = buffer.readByte();
		// ProtocolVersion 2Byte
		short version = buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		if (version == TLS.V12) {
			if (type == Record.APPLICATION_DATA) {

				// CIPHERTEXT

				if (length > Record.CIPHERTEXT_MAX) {
					buffer.clear();
					return new Alert(Alert.RECORD_OVERFLOW);
				}

				// 解密
				final DataBuffer data;
				if (buffer.readable() == length) {
					data = decrypt(buffer, length);
				} else if (buffer.readable() > length) {
					data = decrypt(buffer, length);
				} else {
					buffer.reset();
					return null;
				}
				// 解密失败
				if (data == null) {
					return new Alert(Alert.BAD_RECORD_MAC);
				}

				// 删除 zeros 查找 ContentType
				type = data.backByte();
				while (type == 0) {
					type = data.backByte();
				}

				if (type == Record.APPLICATION_DATA) {
					if (data.readable() > 0) {
						return data;
					} else {
						data.release();
						return null;
					}
				}

				Record record;
				// System.out.println(data);
				if (type == Record.HANDSHAKE) {
					// 如果负载包含多项握手消息
					// 必须返回最后解码的握手消息
					record = decode(data);
					while (data.readable() > 0) {
						received(chain, (Handshake) record);
						record = decode(data);
					}
				} else if (type == Record.CHANGE_CIPHER_SPEC) {
					record = decodeChangeCipherSpec(data);
				} else if (type == Record.HEARTBEAT) {
					record = decodeHeartbeat(data, data.readable());
				} else if (type == Record.INVALID) {
					record = decodeInvalid(data, data.readable());
				} else if (type == Record.ALERT) {
					record = decodeAlert(data);
				} else {
					record = new Alert(Alert.UNEXPECTED_MESSAGE);
				}
				if (data.readable() > 0) {
					data.release();
					throw new IOException("TLS:数据残留");
				} else {
					data.release();
				}
				return record;
			} else {

				// PLAINTEXT

				if (length > Record.PLAINTEXT_MAX) {
					buffer.clear();
					return new Alert(Alert.RECORD_OVERFLOW);
				}

				if (buffer.readable() >= length) {
					if (type == Record.CHANGE_CIPHER_SPEC) {
						return decodeChangeCipherSpec(buffer);
					} else if (type == Record.HANDSHAKE) {
						return decode(buffer);
					} else if (type == Record.HEARTBEAT) {
						return decodeHeartbeat(buffer, length);
					} else if (type == Record.INVALID) {
						return decodeInvalid(buffer, length);
					} else if (type == Record.ALERT) {
						return decodeAlert(buffer);
					} else {
						buffer.clear();
						return new Alert(Alert.UNEXPECTED_MESSAGE);
					}
				} else {
					buffer.reset();
					return null;
				}
			}
		} else {
			// buffer.reset();
			// System.out.println(buffer);
			// return null;
			buffer.clear();
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 此消息始终明文编码，此方法始终执行完整的记录编码
	 */
	protected DataBuffer encode(ChangeCipherSpec message) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(1);
		// 0x01 1Byte
		buffer.writeByte(ChangeCipherSpec.ONE);
		return buffer;
	}

	protected Record decodeChangeCipherSpec(DataBuffer buffer) {
		final int value = buffer.readByte();
		if (value == ChangeCipherSpec.ONE) {
			return ChangeCipherSpec.INSTANCE;
		} else {
			return new Alert(Alert.UNEXPECTED_MESSAGE);
		}
	}

	protected void encode(HeartbeatMessage message, DataBuffer buffer) throws IOException {
		// HeartbeatMessageType 1Byte
		buffer.writeByte(message.getMessageType());
		// payload_length 2Byte(uint16)
		// opaque payload nByte
		buffer.writeShort(message.getPayload().length);
		buffer.write(message.getPayload());
		// opaque padding >= 16Byte
		int size = Record.PLAINTEXT_MAX - message.getPayload().length - 3;
		if (size > 64) {
			size = 64;
		}
		while (size-- > 0) {
			buffer.writeByte(size);
		}
	}

	protected HeartbeatMessage decodeHeartbeat(DataBuffer buffer, int length) throws IOException {
		final HeartbeatMessage message = new HeartbeatMessage();
		// HeartbeatMessageType 1Byte
		message.setMessageType(buffer.readByte());
		// payload_length 2Byte(uint16)
		message.setPayload(new byte[buffer.readUnsignedShort()]);
		// opaque payload nByte
		buffer.readFully(message.getPayload());
		// ignored opaque padding
		buffer.skipBytes(length - message.getPayload().length - 3);
		return message;
	}

	protected void encode(Invalid message, DataBuffer buffer) {
		// 不应出现的情形
	}

	protected Invalid decodeInvalid(DataBuffer buffer, int length) throws IOException {
		buffer.skipBytes(length);
		return Invalid.INSTANCE;
	}

	protected void encode(Alert message, DataBuffer buffer) {
		// AlertLevel 1Byte
		buffer.writeByte(message.getLevel());
		// AlertDescription 1Byte
		buffer.writeByte(message.getDescription());
	}

	protected Alert decodeAlert(DataBuffer buffer) throws IOException {
		final Alert alert = new Alert();
		// AlertLevel 1Byte
		alert.setLevel(buffer.readByte());
		// AlertDescription 1Byte
		alert.setDescription(buffer.readByte());
		return alert;
	}
}