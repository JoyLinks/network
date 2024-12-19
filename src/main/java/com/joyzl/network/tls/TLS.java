package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

public class TLS {

	/** SSL 3.0 */
	public final static short SSL30 = 0x0300;
	/** TLS 1.0 / SSL 3.0 */
	public final static short V10 = 0x0301;
	/** TLS 1.1 */
	public final static short V11 = 0x0302;
	/** TLS 1.2 */
	public final static short V12 = 0x0303;
	/** TLS 1.3 */
	public final static short V13 = 0x0304;

	final static byte[] EMPTY_BYTES = new byte[0];
	final static byte[] ZERO_BYTES = new byte[] { 0 };

	/** 16K (2^14) */
	final static int CHUNK_MAX = 16384;

	public DataBuffer encode(Record message) throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		// ContentType 1Byte
		buffer.writeByte(message.getType().code());
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getProtocolVersion());
		// length 2Byte(uint16)
		buffer.writeShort(0);
		// fragment nByte
		if (message.getType() == ContentType.HANDSHAKE) {
			// TLS Plaintext
			encode((Handshake) message, buffer);
		} else if (message.getType() == ContentType.CHANGE_CIPHER_SPEC) {
			encode((ChangeCipherSpec) message, buffer);
		} else if (message.getType() == ContentType.APPLICATION_DATA) {
			encode((ApplicationData) message, buffer);
		} else if (message.getType() == ContentType.HEARTBEAT) {
			encode((Heartbeat) message, buffer);
		} else if (message.getType() == ContentType.INVALID) {
			encode((Invalid) message, buffer);
		} else if (message.getType() == ContentType.ALERT) {
			encode((Alert) message, buffer);
		} else {
			throw new UnsupportedOperationException("不支持的TLS ContentType:" + message.getType());
		}
		// SET Length
		int length = buffer.readable() - 3;
		buffer.set(3, (byte) (length << 8));
		buffer.set(4, (byte) length);
		return buffer;
	}

	private void encode(Alert message, DataBuffer buffer) {
		// AlertLevel 1Byte
		buffer.writeByte(message.getLevel().code());
		// AlertDescription 1Byte
		buffer.writeByte(message.getDescription().code());
	}

	private void encode(Handshake message, DataBuffer buffer) throws IOException {
		// HandshakeType 1Byte
		buffer.writeByte(message.getMsgType().code());
		// Length 3Byte(uint24)
		buffer.writeMedium(0);
		//
		if (message.getMsgType() == HandshakeType.CLIENT_HELLO) {
			encode((ClientHello) message, buffer);
		} else if (message.getMsgType() == HandshakeType.SERVER_HELLO) {
			encode((ServerHello) message, buffer);
		} else if (message.getMsgType() == HandshakeType.END_OF_EARLY_DATA) {
		} else if (message.getMsgType() == HandshakeType.ENCRYPTED_EXTENSIONS) {
			encode((EncryptedExtensions) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE_REQUEST) {
			encode((CertificateRequest) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE) {
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE_VERIFY) {
		} else if (message.getMsgType() == HandshakeType.FINISHED) {
		} else if (message.getMsgType() == HandshakeType.NEW_SESSION_TICKET) {
		} else if (message.getMsgType() == HandshakeType.KEY_UPDATE) {
		} else {
			throw new UnsupportedOperationException("不支持的TLS HandshakeType:" + message.getMsgType());
		}
		// SET LENGTH
		int length = buffer.readable() - 6;
		buffer.set(6, (byte) (length << 16));
		buffer.set(7, (byte) (length << 8));
		buffer.set(8, (byte) (length));
	}

	public void encode(ClientHello message, DataBuffer buffer) throws IOException {
		// struct {
		// ProtocolVersion legacy_version = 0x0303; /* TLS v1.2 */
		// Random random;
		// opaque legacy_session_id<0..32>;
		// CipherSuite cipher_suites<2..2^16-2>;
		// opaque legacy_compression_methods<1..2^8-1>;
		// Extension extensions<8..2^16-1>;
		// } ClientHello;

		int index;
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getVersion());
		// Random 32Byte
		buffer.write(message.getRandom());
		// SessionID <0..32>
		buffer.writeShort(message.getSessionId().length);
		buffer.write(message.getSessionId());
		// CipherSuites <2..2^16-2>
		buffer.writeShort(message.getCipherSuites().length);
		for (index = 0; index < message.getCipherSuites().length; index++) {
			buffer.writeShort(message.getCipherSuites()[index]);
		}
		// Compression Methods <1..2^8-1>
		buffer.writeShort(message.getCompressionMethods().length);
		buffer.write(message.getCompressionMethods());

		// Extensions <8..2^16-1>
		Extension extension;
		for (index = 0; index < message.getExtensions().size(); index++) {
			extension = message.getExtensions().get(index);
			// Type
			buffer.writeShort(extension.getType().code());
			// Data Length
			buffer.writeShort(extension.getData().length);
			// Data Bytes
			buffer.write(extension.getData());
		}
		// 1.3
		// supported_versions
	}

	public void encode(ServerHello message, DataBuffer buffer) throws IOException {
		int index;
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getVersion());
		// Random 32Byte
		buffer.write(message.getRandom());
		// SessionID <0..32>
		buffer.writeShort(message.getSessionId().length);
		buffer.write(message.getSessionId());
		// CipherSuites 2Byte
		buffer.writeShort(message.getCipherSuite());
		// Compression Methods 1Byte
		buffer.writeByte(message.getCompressionMethod());

		// Extensions <6..2^16-1>
		Extension extension;
		for (index = 0; index < message.getExtensions().size(); index++) {
			extension = message.getExtensions().get(index);
			// Type
			buffer.writeShort(extension.getType().code());
			// Data
			buffer.writeShort(extension.getData().length);
			buffer.write(extension.getData());
		}
		// 1.3
		// supported_versions
		// pre_shared_key
		// key_share
	}

	public void encode(CertificateRequest message, DataBuffer buffer) throws IOException {
		// Extensions <6..2^16-1>
		Extension extension;
		for (int index = 0; index < message.getExtensions().size(); index++) {
			extension = message.getExtensions().get(index);
			// Type
			buffer.writeShort(extension.getType().code());
			// Data
			buffer.writeShort(extension.getData().length);
			buffer.write(extension.getData());
		}
	}

	public void encode(EncryptedExtensions message, DataBuffer buffer) throws IOException {
		// Extensions <6..2^16-1>
		Extension extension;
		for (int index = 0; index < message.getExtensions().size(); index++) {
			extension = message.getExtensions().get(index);
			// Type
			buffer.writeShort(extension.getType().code());
			// Data
			buffer.writeShort(extension.getData().length);
			buffer.write(extension.getData());
		}
	}

	public Handshake decode(DataBuffer buffer) throws IOException {
		buffer.mark();
		// HandshakeType 1Byte
		final int type = buffer.readUnsignedByte();
		// uint24 length
		int length = buffer.readUnsignedMedium();
		if (buffer.readable() >= length) {
			if (type == HandshakeType.CLIENT_HELLO.code()) {
				return decodeClientHello(buffer);
			} else if (type == HandshakeType.SERVER_HELLO.code()) {
				return decodeServerHello(buffer);
			} else if (type == HandshakeType.END_OF_EARLY_DATA.code()) {
			} else if (type == HandshakeType.ENCRYPTED_EXTENSIONS.code()) {
			} else if (type == HandshakeType.CERTIFICATE_REQUEST.code()) {
			} else if (type == HandshakeType.CERTIFICATE.code()) {
			} else if (type == HandshakeType.CERTIFICATE_VERIFY.code()) {
			} else if (type == HandshakeType.FINISHED.code()) {
			} else if (type == HandshakeType.NEW_SESSION_TICKET.code()) {
			} else if (type == HandshakeType.KEY_UPDATE.code()) {
			} else {

			}
		}
		buffer.reset();
		return null;
	}

	public ClientHello decodeClientHello(DataBuffer buffer) throws IOException {
		final ClientHello message = new ClientHello();
		int index, length;

		// Version 2Byte
		message.setVersion(buffer.readShort());

		// Random 32Byte
		final byte[] random = new byte[32];
		buffer.readFully(random);
		message.setRandom(random);

		// SessionID
		length = buffer.readUnsignedShort();
		final byte[] session = new byte[length];
		buffer.readFully(session);
		message.setSessionId(session);

		// CipherSuites
		length = buffer.readUnsignedShort() / 2;
		final short[] suites = new short[length];
		for (index = 0; index < length; index++) {
			suites[index] = buffer.readShort();
		}
		message.setCipherSuites(suites);

		// Compression Methods
		length = buffer.readUnsignedShort();
		final byte[] methods = new byte[length];
		buffer.readFully(methods);
		message.setCompressionMethods(methods);

		// extensions
		length = buffer.readUnsignedShort();

		return message;
	}

	public ServerHello decodeServerHello(DataBuffer buffer) throws IOException {
		final ServerHello message = new ServerHello();
		int length;

		// Version 2Byte
		message.setVersion(buffer.readShort());

		// Random 32Byte
		final byte[] random = new byte[32];
		buffer.readFully(random);
		message.setRandom(random);

		// SessionID
		length = buffer.readUnsignedShort();
		final byte[] session = new byte[length];
		buffer.readFully(session);
		message.setSessionId(session);

		// CipherSuite
		message.setCipherSuite(buffer.readShort());

		// Compression Method
		message.setCompressionMethod(buffer.readByte());

		// extensions
		length = buffer.readUnsignedShort();

		return message;
	}

	public static void main(String[] argments) {
		// 2^9(1), 2^10(2), 2^11(3), 2^12(4),
		System.out.println(Math.pow(2, 9));
		System.out.println(Math.pow(2, 10));
		System.out.println(Math.pow(2, 11));
		System.out.println(Math.pow(2, 12));
	}
}