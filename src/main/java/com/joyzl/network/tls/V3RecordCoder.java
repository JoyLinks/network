package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 记录层编码解码
 * 
 * @author ZhangXi 2025年2月10日
 */
class V3RecordCoder extends RecordCoder {

	/**
	 * 解码记录层数据
	 */
	static int v13Decode(V3CipherSuiter cipher, DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		int type = buffer.readByte();
		// ProtocolVersion 2Byte
		buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		if (type == Record.APPLICATION_DATA) {
			// CIPHERTEXT
			if (length > Record.CIPHERTEXT_MAX) {
				buffer.clear();
				throw new TLSException(Alert.RECORD_OVERFLOW);
			}
			// 解密
			try {
				if (buffer.readable() == length) {
					cipher.v13DecryptAEAD(length);
					cipher.decryptFinal(buffer, data);
				} else if (buffer.readable() > length) {
					cipher.v13DecryptAEAD(length);
					cipher.decryptFinal(buffer, data, length);
				} else {
					buffer.reset();
					return -1;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new TLSException(Alert.BAD_RECORD_MAC);
			}
			// 删除 zeros 查找 ContentType
			type = data.backByte();
			while (type == 0) {
				type = data.backByte();
			}
			return type;
		} else {
			// PLAINTEXT
			if (length > Record.PLAINTEXT_MAX) {
				buffer.clear();
				throw new TLSException(Alert.RECORD_OVERFLOW);
			}
			if (buffer.readable() >= length) {
				buffer.transfer(data, length);
				return type;
			} else {
				buffer.reset();
				return -1;
			}
		}
	}

	/**
	 * 编码明文记录，如有必要执行分块
	 */
	static void encodePlaintext(Record record, DataBuffer data, DataBuffer buffer) throws IOException {
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
	}

	static int decodePlaintext(DataBuffer buffer, DataBuffer data) throws Exception {
		buffer.mark();
		// ContentType 1Byte
		int type = buffer.readByte();
		// ProtocolVersion 2Byte
		buffer.readShort();
		// length 2Byte(uint16)
		int length = buffer.readUnsignedShort();

		// PLAINTEXT
		if (length > Record.PLAINTEXT_MAX) {
			buffer.clear();
			throw new TLSException(Alert.RECORD_OVERFLOW);
		}
		if (buffer.readable() >= length) {
			buffer.transfer(data, length);
			return type;
		} else {
			buffer.reset();
			return -1;
		}
	}

	/**
	 * 编码APPLICATION_DATA的记录消息，始终加密消息，如有必要执行分块和填充
	 */
	static void v13EncodeCiphertext(V3CipherSuiter cipher, Record record, DataBuffer data, DataBuffer buffer) throws Exception {
		while (data.readable() > Record.PLAINTEXT_MAX) {
			// ContentType 1Byte
			buffer.writeByte(Record.APPLICATION_DATA);
			// ProtocolVersion 2Byte
			buffer.writeShort(record.getProtocolVersion());
			// length 2Byte(uint16)
			buffer.writeShort(Record.PLAINTEXT_MAX + 1/* ContentType */ + cipher.tagLength());

			// encrypted_record: DATA + ContentType
			// 负载已满时无填充数据(padding)
			cipher.v13EncryptAEAD(Record.PLAINTEXT_MAX + 1/* ContentType */ + cipher.tagLength());
			cipher.encryptUpdate(data, buffer, Record.PLAINTEXT_MAX);
			cipher.encryptUpdate(new byte[] { record.contentType() }, buffer);
			cipher.encryptFinal(buffer);
		}

		// encrypted_record: DATA + ContentType + PADDING(n)
		// ContentType 1Byte
		data.writeByte(record.contentType());
		v13Padding(data, 0);

		// ContentType 1Byte
		buffer.writeByte(Record.APPLICATION_DATA);
		// ProtocolVersion 2Byte
		buffer.writeShort(record.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(data.readable() + cipher.tagLength());
		// encrypted_record
		cipher.v13EncryptAEAD(data.readable() + cipher.tagLength());
		cipher.encryptFinal(data, buffer);
	}

	/**
	 * 零长度应用数据，此方法始终执行完整的记录编码
	 */
	static void encode(ApplicationData message, DataBuffer buffer) throws Exception {
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(0);
	}

	/**
	 * 此消息始终明文编码，此方法始终执行完整的记录编码
	 */
	static void encode(ChangeCipherSpec message, DataBuffer buffer) throws Exception {
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(1);
		// 0x01 1Byte
		buffer.writeByte(ChangeCipherSpec.ONE);
	}

	static Record decodeChangeCipherSpec(DataBuffer buffer) {
		if (buffer.readByte() == ChangeCipherSpec.ONE) {
			return ChangeCipherSpec.INSTANCE;
		} else {
			return new Alert(Alert.UNEXPECTED_MESSAGE);
		}
	}

	static void encode(HeartbeatMessage message, DataBuffer buffer) throws IOException {
		// HeartbeatMessageType 1Byte
		buffer.writeByte(message.getMessageType());
		// payload_length 2Byte(uint16)
		// opaque payload nByte
		buffer.writeShort(message.getPayload().length);
		buffer.write(message.getPayload());
		// opaque padding >= 16Byte
		v13Padding(buffer, 16);
	}

	static HeartbeatMessage decodeHeartbeat(DataBuffer buffer) throws IOException {
		final HeartbeatMessage message = new HeartbeatMessage();
		// HeartbeatMessageType 1Byte
		message.setMessageType(buffer.readByte());
		// payload_length 2Byte(uint16)
		message.setPayload(new byte[buffer.readUnsignedShort()]);
		// opaque payload nByte
		buffer.readFully(message.getPayload());
		// ignored opaque padding
		buffer.skipBytes(buffer.readable());
		return message;
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

	/** PADDING(zero*) */
	static void v13Padding(DataBuffer data, int min) {
		int size = Record.PLAINTEXT_MAX - data.readable();
		if (size > min) {
			if (size > 32) {
				size = 32 - (size % 32);
			} else if (size > 16) {
				size = 16 - (size % 16);
			} else if (size > 8) {
				size = 8 - (size % 8);
			} else if (size > 4) {
				size = 4 - (size % 4);
			} else {
				size = 4 - size;
			}
			while (size-- > 0) {
				data.writeByte(0);
			}
		}
	}
}