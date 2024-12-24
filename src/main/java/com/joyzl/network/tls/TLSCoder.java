package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

public class TLSCoder extends TLS {

	public static DataBuffer encodeByClient(Record message) throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(0);
		// fragment nByte
		if (message.contentType() == Record.APPLICATION_DATA) {
			// TLSCiphertext
			encode((ApplicationData) message, buffer);
		} else if (message.contentType() == Record.CHANGE_CIPHER_SPEC) {
			// TLS Plaintext
			encode((ChangeCipherSpec) message, buffer);
		} else if (message.contentType() == Record.HANDSHAKE) {
			// TLS Plaintext
			HandshakeCoder.encodeByClient((Handshake) message, buffer);
		} else if (message.contentType() == Record.HEARTBEAT) {
			// TLS Plaintext
			encode((HeartbeatMessage) message, buffer);
		} else if (message.contentType() == Record.INVALID) {
			// TLS Plaintext
			encode((Invalid) message, buffer);
		} else if (message.contentType() == Record.ALERT) {
			// TLS Plaintext
			encode((Alert) message, buffer);
		} else {
			throw new UnsupportedOperationException("TLS 不支持的内容类型:" + message.contentType());
		}
		// SET Length
		int length = buffer.readable() - 5;
		buffer.set(3, (byte) (length >>> 8));
		buffer.set(4, (byte) length);
		return buffer;
	}

	public static DataBuffer encodeByServer(Record message) throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		// ContentType 1Byte
		buffer.writeByte(message.contentType());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(0);
		// fragment nByte
		if (message.contentType() == Record.APPLICATION_DATA) {
			// TLSCiphertext
			encode((ApplicationData) message, buffer);
		} else if (message.contentType() == Record.CHANGE_CIPHER_SPEC) {
			// TLS Plaintext
			encode((ChangeCipherSpec) message, buffer);
		} else if (message.contentType() == Record.HANDSHAKE) {
			// TLS Plaintext
			HandshakeCoder.encodeByServer((Handshake) message, buffer);
		} else if (message.contentType() == Record.HEARTBEAT) {
			// TLS Plaintext
			encode((HeartbeatMessage) message, buffer);
		} else if (message.contentType() == Record.INVALID) {
			// TLS Plaintext
			encode((Invalid) message, buffer);
		} else if (message.contentType() == Record.ALERT) {
			// TLS Plaintext
			encode((Alert) message, buffer);
		} else {
			throw new UnsupportedOperationException("TLS 不支持的内容类型:" + message.contentType());
		}
		// SET Length
		int length = buffer.readable() - 5;
		buffer.set(3, (byte) (length >>> 8));
		buffer.set(4, (byte) length);
		return buffer;
	}

	public static Record decodeByClient(DataBuffer buffer) throws IOException {
		buffer.mark();
		// ContentType 1Byte
		final int type = buffer.readByte();
		// ProtocolVersion 2Byte
		final short version = buffer.readShort();
		// length 2Byte(uint16)
		final int length = buffer.readUnsignedShort();
		if (length > CHUNK_MAX) {
			buffer.clear();
			return Invalid.INSTANCE;
		}
		if (buffer.readable() >= length) {
			final Record record;
			if (type == Record.APPLICATION_DATA) {
				// TLSCiphertext
				record = decodeApplicationData(buffer, length);
			} else if (type == Record.CHANGE_CIPHER_SPEC) {
				// TLS Plaintext
				record = decodeChangeCipherSpec(buffer);
			} else if (type == Record.HANDSHAKE) {
				// TLS Plaintext
				record = HandshakeCoder.decodeByClient(buffer);
			} else if (type == Record.HEARTBEAT) {
				// TLS Plaintext
				record = decodeHeartbeat(buffer, length);
			} else if (type == Record.INVALID) {
				// TLS Plaintext
				record = decodeInvalid(buffer, length);
			} else if (type == Record.ALERT) {
				// TLS Plaintext
				record = decodeAlert(buffer);
			} else {
				buffer.clear();
				throw new UnsupportedOperationException("TLS 不支持的内容类型:" + type);
			}
			record.setProtocolVersion(version);
			return record;
		} else {
			buffer.reset();
			return null;
		}
	}

	public static Record decodeByServer(DataBuffer buffer) throws IOException {
		buffer.mark();
		// ContentType 1Byte
		final int type = buffer.readByte();
		// ProtocolVersion 2Byte
		final short version = buffer.readShort();
		// length 2Byte(uint16)
		final int length = buffer.readUnsignedShort();
		if (length > CHUNK_MAX) {
			buffer.clear();
			return Invalid.INSTANCE;
		}
		if (buffer.readable() >= length) {
			final Record record;
			if (type == Record.APPLICATION_DATA) {
				// TLSCiphertext
				record = decodeApplicationData(buffer, length);
			} else if (type == Record.CHANGE_CIPHER_SPEC) {
				// TLS Plaintext
				record = decodeChangeCipherSpec(buffer);
			} else if (type == Record.HANDSHAKE) {
				// TLS Plaintext
				record = HandshakeCoder.decodeByServer(buffer);
			} else if (type == Record.HEARTBEAT) {
				// TLS Plaintext
				record = decodeHeartbeat(buffer, length);
			} else if (type == Record.INVALID) {
				// TLS Plaintext
				record = decodeInvalid(buffer, length);
			} else if (type == Record.ALERT) {
				// TLS Plaintext
				record = decodeAlert(buffer);
			} else {
				buffer.clear();
				throw new UnsupportedOperationException("TLS 不支持的内容类型:" + type);
			}
			record.setProtocolVersion(version);
			return record;
		} else {
			buffer.reset();
			return null;
		}
	}

	private static void encode(ApplicationData message, DataBuffer buffer) {
		// opaque encrypted_record
		message.getData().transfer(buffer);
		message.getData().release();
		message.setData(null);
	}

	private static ApplicationData decodeApplicationData(DataBuffer buffer, int length) {
		final ApplicationData data = new ApplicationData();
		data.setData(DataBuffer.instance());
		buffer.transfer(data.getData(), length);
		return data;
	}

	private static void encode(ChangeCipherSpec message, DataBuffer buffer) {
		// 0x01 1Byte
		buffer.writeByte(ChangeCipherSpec.ONE);
	}

	private static Record decodeChangeCipherSpec(DataBuffer buffer) {
		final int value = buffer.readByte();
		if (value == ChangeCipherSpec.ONE) {
			return ChangeCipherSpec.INSTANCE;
		} else {
			return Invalid.INSTANCE;
		}
	}

	private static void encode(HeartbeatMessage message, DataBuffer buffer) throws IOException {
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

	private static HeartbeatMessage decodeHeartbeat(DataBuffer buffer, int length) throws IOException {
		final HeartbeatMessage message = new HeartbeatMessage();
		message.setMessageType(buffer.readByte());
		final byte[] payload = new byte[buffer.readUnsignedShort()];
		buffer.readFully(payload);
		message.setPayload(payload);
		// ignored opaque padding
		buffer.skipBytes(length - payload.length - 3);
		return message;
	}

	private static void encode(Invalid message, DataBuffer buffer) {
		// 不应出现的情形
	}

	private static Invalid decodeInvalid(DataBuffer buffer, int length) throws IOException {
		buffer.skipBytes(length);
		return new Invalid();
	}

	private static void encode(Alert message, DataBuffer buffer) {
		// AlertLevel 1Byte
		buffer.writeByte(message.getLevel());
		// AlertDescription 1Byte
		buffer.writeByte(message.getDescription());
	}

	private static Alert decodeAlert(DataBuffer buffer) throws IOException {
		final Alert alert = new Alert();
		// AlertLevel 1Byte
		alert.setLevel(buffer.readByte());
		// AlertDescription 1Byte
		alert.setDescription(buffer.readByte());
		return alert;
	}
}