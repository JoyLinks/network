package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

public class TLSCoder extends TLS {

	public static DataBuffer encode(Record message) throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		// ContentType 1Byte
		buffer.writeByte(message.contentType().code());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(0);
		// fragment nByte
		if (message.contentType() == ContentType.APPLICATION_DATA) {
			// TLSCiphertext
			encode((ApplicationData) message, buffer);
		} else if (message.contentType() == ContentType.CHANGE_CIPHER_SPEC) {
			// TLS Plaintext
			encode((ChangeCipherSpec) message, buffer);
		} else if (message.contentType() == ContentType.HANDSHAKE) {
			// TLS Plaintext
			encode((Handshake) message, buffer);
		} else if (message.contentType() == ContentType.HEARTBEAT) {
			// TLS Plaintext
			encode((HeartbeatMessage) message, buffer);
		} else if (message.contentType() == ContentType.INVALID) {
			// TLS Plaintext
			encode((Invalid) message, buffer);
		} else if (message.contentType() == ContentType.ALERT) {
			// TLS Plaintext
			encode((Alert) message, buffer);
		} else {
			throw new UnsupportedOperationException("TLS 不支持的内容类型:" + message.contentType());
		}
		// SET Length
		int length = buffer.readable() - 5;
		buffer.set(3, (byte) (length << 8));
		buffer.set(4, (byte) length);
		return buffer;
	}

	public static Record decode(DataBuffer buffer) throws IOException {
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
			if (type == ContentType.APPLICATION_DATA.code()) {
				// TLSCiphertext
				record = decodeApplicationData(buffer);
			} else if (type == ContentType.CHANGE_CIPHER_SPEC.code()) {
				// TLS Plaintext
				record = decodeChangeCipherSpec(buffer);
			} else if (type == ContentType.HANDSHAKE.code()) {
				// TLS Plaintext
				record = decodeHandshake(buffer);
			} else if (type == ContentType.HEARTBEAT.code()) {
				// TLS Plaintext
				record = decodeHeartbeat(buffer, length);
			} else if (type == ContentType.INVALID.code()) {
				// TLS Plaintext
				record = decodeInvalid(buffer, length);
			} else if (type == ContentType.ALERT.code()) {
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

	private static void encode(Handshake message, DataBuffer buffer) throws IOException {
		HandshakeCoder.encode(message, buffer);
	}

	private static Handshake decodeHandshake(DataBuffer buffer) throws IOException {
		return HandshakeCoder.decode(buffer);
	}

	private static void encode(ApplicationData message, DataBuffer buffer) {
		// opaque encrypted_record
	}

	private static ApplicationData decodeApplicationData(DataBuffer buffer) {
		return null;
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
		buffer.writeByte(message.getMessageType().code());
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
		message.setMessageType(buffer.readUnsignedByte());
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
		buffer.writeByte(message.getLevel().code());
		// AlertDescription 1Byte
		buffer.writeByte(message.getDescription().code());
	}

	private static Alert decodeAlert(DataBuffer buffer) throws IOException {
		final Alert alert = new Alert();
		// AlertLevel 1Byte
		alert.setLevel(buffer.readUnsignedByte());
		// AlertDescription 1Byte
		alert.setDescription(buffer.readUnsignedByte());
		return alert;
	}
}