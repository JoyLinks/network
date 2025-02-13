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

	protected abstract boolean handshaked();

	protected abstract DataBuffer decrypt(DataBuffer buffer, int length) throws Exception;

	protected abstract DataBuffer encrypt(DataBuffer buffer) throws Exception;

	protected abstract Handshake decode(DataBuffer buffer) throws Exception;

	protected abstract void encode(Handshake handshake, DataBuffer buffer) throws Exception;

	protected abstract void received(ChainChannel<Object> chain, Handshake handshake) throws Exception;

	/**
	 * 编码除APPLICATION_DATA之外的记录消息，视连接状态加密消息
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
	 * 编码APPLICATION_DATA的记录消息，始终加密消息
	 */
	protected DataBuffer encodeCiphertext(Record record, DataBuffer data) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();

		while (data.readable() > Record.PLAINTEXT_MAX) {
			final DataBuffer temp = DataBuffer.instance();
			// ContentType + PADDING(0)
			// 负载已满时无填充数据(padding)
			data.transfer(temp, Record.PLAINTEXT_MAX);
			temp.writeByte(Record.APPLICATION_DATA);

			// cipher.encryptAdditional(temp.readable());
			// cipher.encryptFinal(temp);
			encrypt(temp);

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

		// ContentType + PADDING(*)
		data.writeByte(record.contentType());
		int p = Record.PLAINTEXT_MAX - data.readable();
		if (p > 8) {
			p = 8 - (p % 8);
		} else if (p > 4) {
			p = 4 - (p % 4);
		}
		while (p-- > 0) {
			data.writeByte(0);
		}

		// cipher.encryptAdditional(data.readable());
		// cipher.encryptFinal(data);
		encrypt(data);

		// ContentType 1Byte
		buffer.writeByte(Record.APPLICATION_DATA);
		// ProtocolVersion 2Byte
		buffer.writeShort(TLS.V12);
		// length 2Byte(uint16)
		buffer.writeShort(data.readable());
		// opaque
		data.transfer(buffer);
		data.release();
		return buffer;
	}

	/**
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

				final Record record;
				if (type == Record.HANDSHAKE) {
					while (data.readable() > 0) {
						received(chain, decode(data));
					}
					record = null;
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
			buffer.clear();
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * 此消息始终明文编码
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
		buffer.writeShort(message.getPayload().length);
		// opaque payload nByte
		buffer.write(message.getPayload());
		// opaque padding
		int size = 16;
		while (size-- > 0) {
			buffer.writeByte(1);
		}
	}

	protected HeartbeatMessage decodeHeartbeat(DataBuffer buffer, int length) throws IOException {
		final HeartbeatMessage message = new HeartbeatMessage();
		message.setMessageType(buffer.readByte());
		final byte[] payload = new byte[buffer.readUnsignedShort()];
		buffer.readFully(payload);
		message.setPayload(payload);
		// ignored opaque padding
		buffer.skipBytes(length - payload.length - 3);
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