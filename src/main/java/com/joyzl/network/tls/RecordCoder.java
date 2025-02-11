package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 编码解码
 * 
 * @author ZhangXi 2025年2月10日
 */
class RecordCoder extends TLS {

	/**
	 * 编码除APPLICATION_DATA之外的记录消息，视连接状态加密消息
	 */
	static DataBuffer encode(Record record, CipherSuiter cipher) throws Exception {
		if (record.contentType() == Record.HANDSHAKE) {
			final Handshake handshake = (Handshake) record;
			final DataBuffer data = DataBuffer.instance();
			HandshakeCoder.encodeByClient(handshake, data);
			// 握手消息应执行哈希计算
			cipher.hash(data);

			if (handshake.msgType() == Handshake.SERVER_HELLO //
					|| handshake.msgType() == Handshake.CLIENT_HELLO) {
				// 始终明文发送的握手消息
				return encodePlaintext(handshake, data);
			} else {
				return encodeCiphertext(handshake, data, cipher);
			}
		}

		// 兼容性消息
		if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
			// 此消息始终明文编码
			final DataBuffer data = DataBuffer.instance();
			encode((ChangeCipherSpec) record, data);
			return encodePlaintext(record, data);
		}

		// 其它消息由hasKey()状态来判断是否加密
		final DataBuffer data = DataBuffer.instance();
		if (record.contentType() == Record.HEARTBEAT) {
			RecordCoder.encode((HeartbeatMessage) record, data);
		} else if (record.contentType() == Record.INVALID) {
			RecordCoder.encode((Invalid) record, data);
		} else if (record.contentType() == Record.ALERT) {
			RecordCoder.encode((Alert) record, data);
		} else {
			throw new UnsupportedOperationException("意外的记录类型:" + record.contentType());
		}
		if (cipher.hasKey()) {
			return encodeCiphertext(record, data, cipher);
		} else {
			return encodePlaintext(record, data);
		}
	}

	static DataBuffer encodePlaintext(Record record, DataBuffer data) throws IOException {
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
	static DataBuffer encodeCiphertext(Record record, DataBuffer data, CipherSuiter cipher) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();

		while (data.readable() > Record.PLAINTEXT_MAX) {
			final DataBuffer temp = DataBuffer.instance();
			// ContentType + PADDING(0)
			// 负载已满时无填充数据(padding)
			data.transfer(temp, Record.PLAINTEXT_MAX);
			temp.writeByte(Record.APPLICATION_DATA);

			cipher.encryptAdditional(temp.readable());
			cipher.encryptFinal(temp);

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

		cipher.encryptAdditional(data.readable());
		cipher.encryptFinal(data);

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

	static Object decode(DataBuffer buffer, CipherSuiter cipher) throws Exception {
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
					data = DataBuffer.instance();
					cipher.decryptAdditional(length);
					cipher.decryptFinal(buffer, data);
				} else if (buffer.readable() > length) {
					data = DataBuffer.instance();
					cipher.decryptAdditional(length);
					cipher.decryptFinal(buffer, data, length);
				} else {
					buffer.reset();
					return null;
				}
				// 删除 zeros 查找 ContentType
				type = data.backByte();
				while (type == 0) {
					type = data.backByte();
				}
				// if (type == 0 || data.readable() <= 0) {
				// data.release();
				// return new Alert(Alert.UNEXPECTED_MESSAGE);
				// }
				// data.writeByte(type);
				// return data;

				if (type == Record.APPLICATION_DATA) {
					return data;
				}

				// TODO 多次解码消息如何返回
				Record record;
				while (data.readable() > 0) {
					if (type == Record.CHANGE_CIPHER_SPEC) {
						record = RecordCoder.decodeChangeCipherSpec(data);
					} else if (type == Record.HANDSHAKE) {
						cipher.hash(data);
						record = HandshakeCoder.decodeByClient(data);
					} else if (type == Record.HEARTBEAT) {
						record = RecordCoder.decodeHeartbeat(data, length);
					} else if (type == Record.INVALID) {
						record = RecordCoder.decodeInvalid(data, length);
					} else if (type == Record.ALERT) {
						record = RecordCoder.decodeAlert(data);
					} else {
						record = new Alert(Alert.UNEXPECTED_MESSAGE);
					}
				}
				data.release();
				return null;
			} else {

				// PLAINTEXT

				if (length > Record.PLAINTEXT_MAX) {
					buffer.clear();
					return new Alert(Alert.RECORD_OVERFLOW);
				}
				if (buffer.readable() >= length) {
					if (type == Record.CHANGE_CIPHER_SPEC) {
						return RecordCoder.decodeChangeCipherSpec(buffer);
					} else if (type == Record.HANDSHAKE) {
						if (buffer.readable() == length) {
							cipher.hash(buffer);
						} else {
							cipher.hash(buffer, length);
						}
						return HandshakeCoder.decodeByClient(buffer);
					} else if (type == Record.HEARTBEAT) {
						return RecordCoder.decodeHeartbeat(buffer, length);
					} else if (type == Record.INVALID) {
						return RecordCoder.decodeInvalid(buffer, length);
					} else if (type == Record.ALERT) {
						return RecordCoder.decodeAlert(buffer);
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
			// throw new UnsupportedOperationException();
			return Invalid.INSTANCE;
		}
	}

	static void encode(ChangeCipherSpec message, DataBuffer buffer) {
		// 0x01 1Byte
		buffer.writeByte(ChangeCipherSpec.ONE);
	}

	static Record decodeChangeCipherSpec(DataBuffer buffer) {
		final int value = buffer.readByte();
		if (value == ChangeCipherSpec.ONE) {
			return ChangeCipherSpec.INSTANCE;
		} else {
			return new Alert(Alert.UNEXPECTED_MESSAGE);
		}
	}

	static void encode(HeartbeatMessage message, DataBuffer buffer) throws IOException {
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

	static HeartbeatMessage decodeHeartbeat(DataBuffer buffer, int length) throws IOException {
		final HeartbeatMessage message = new HeartbeatMessage();
		message.setMessageType(buffer.readByte());
		final byte[] payload = new byte[buffer.readUnsignedShort()];
		buffer.readFully(payload);
		message.setPayload(payload);
		// ignored opaque padding
		buffer.skipBytes(length - payload.length - 3);
		return message;
	}

	static void encode(Invalid message, DataBuffer buffer) {
		// 不应出现的情形
	}

	static Invalid decodeInvalid(DataBuffer buffer, int length) throws IOException {
		buffer.skipBytes(length);
		return Invalid.INSTANCE;
	}

	static void encode(Alert message, DataBuffer buffer) {
		// AlertLevel 1Byte
		buffer.writeByte(message.getLevel());
		// AlertDescription 1Byte
		buffer.writeByte(message.getDescription());
	}

	static Alert decodeAlert(DataBuffer buffer) throws IOException {
		final Alert alert = new Alert();
		// AlertLevel 1Byte
		alert.setLevel(buffer.readByte());
		// AlertDescription 1Byte
		alert.setDescription(buffer.readByte());
		return alert;
	}
}