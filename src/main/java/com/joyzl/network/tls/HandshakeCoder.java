package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

public class HandshakeCoder extends TLS {

	public static void encode(Handshake message, DataBuffer buffer) throws IOException {
		// HandshakeType 1Byte
		buffer.writeByte(message.getMsgType().code());
		// Length 3Byte(uint24)
		int position = buffer.readable();
		buffer.writeMedium(0);
		// SUB Handshake
		if (message.getMsgType() == HandshakeType.HELLO_REQUEST) {
			encode((HelloRequest) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CLIENT_HELLO) {
			encode((ClientHello) message, buffer);
		} else if (message.getMsgType() == HandshakeType.SERVER_HELLO) {
			encode((ServerHello) message, buffer);
		} else if (message.getMsgType() == HandshakeType.NEW_SESSION_TICKET) {
			encode((NewSessionTicket) message, buffer);
		} else if (message.getMsgType() == HandshakeType.END_OF_EARLY_DATA) {
			encode((EndOfEarlyData) message, buffer);
		} else if (message.getMsgType() == HandshakeType.ENCRYPTED_EXTENSIONS) {
			encode((EncryptedExtensions) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE) {
			encode((Certificate) message, buffer);
		} else if (message.getMsgType() == HandshakeType.SERVER_KEY_EXCHANGE) {
			encode((ServerKeyExchange) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE_REQUEST) {
			encode((CertificateRequest) message, buffer);
		} else if (message.getMsgType() == HandshakeType.SERVER_HELLO_DONE) {
			encode((ServerHelloDone) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE_VERIFY) {
			encode((CertificateVerify) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CLIENT_KEY_EXCHANGE) {
			encode((ClientKeyExchange) message, buffer);
		} else if (message.getMsgType() == HandshakeType.FINISHED) {
			encode((Finished) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE_URL) {
			encode((CertificateURL) message, buffer);
		} else if (message.getMsgType() == HandshakeType.CERTIFICATE_STATUS) {
			encode((CertificateStatus) message, buffer);
		} else if (message.getMsgType() == HandshakeType.KEY_UPDATE) {
			encode((KeyUpdate) message, buffer);
		} else if (message.getMsgType() == HandshakeType.MESSAGE_HASH) {
			encode((MessageHash) message, buffer);
		} else {
			throw new UnsupportedOperationException("TLS 不支持的握手消息类型:" + message.getMsgType());
		}
		// SET LENGTH
		int length = buffer.readable() - position - 3;
		buffer.set(position++, (byte) (length << 16));
		buffer.set(position++, (byte) (length << 8));
		buffer.set(position, (byte) (length));
	}

	public static Handshake decode(DataBuffer buffer) throws IOException {
		buffer.mark();
		// HandshakeType 1Byte
		final int type = buffer.readByte();
		// uint24 length
		int length = buffer.readUnsignedMedium();
		if (buffer.readable() >= length) {
			if (type == HandshakeType.HELLO_REQUEST.code()) {
				return decodeHelloRequest(buffer);
			} else if (type == HandshakeType.CLIENT_HELLO.code()) {
				return decodeClientHello(buffer);
			} else if (type == HandshakeType.SERVER_HELLO.code()) {
				return decodeServerHello(buffer);
			} else if (type == HandshakeType.NEW_SESSION_TICKET.code()) {
				return decodeNewSessionTicket(buffer);
			} else if (type == HandshakeType.END_OF_EARLY_DATA.code()) {
				return decodeEndOfEarlyData(buffer);
			} else if (type == HandshakeType.ENCRYPTED_EXTENSIONS.code()) {
				return decodeEncryptedExtensions(buffer);
			} else if (type == HandshakeType.CERTIFICATE.code()) {
				return decodeCertificate(buffer);
			} else if (type == HandshakeType.SERVER_KEY_EXCHANGE.code()) {
				return decodeServerKeyExchange(buffer);
			} else if (type == HandshakeType.CERTIFICATE_REQUEST.code()) {
				return decodeCertificateRequest(buffer);
			} else if (type == HandshakeType.SERVER_HELLO_DONE.code()) {
				return decodeServerHelloDone(buffer);
			} else if (type == HandshakeType.CERTIFICATE_VERIFY.code()) {
				return decodeCertificateVerify(buffer);
			} else if (type == HandshakeType.CLIENT_KEY_EXCHANGE.code()) {
				return decodeClientKeyExchange(buffer);
			} else if (type == HandshakeType.FINISHED.code()) {
				return decodeFinished(buffer);
			} else if (type == HandshakeType.CERTIFICATE_URL.code()) {
				return decodeCertificateURL(buffer);
			} else if (type == HandshakeType.CERTIFICATE_STATUS.code()) {
				return decodeCertificateStatus(buffer);
			} else if (type == HandshakeType.KEY_UPDATE.code()) {
				return decodeKeyUpdate(buffer);
			} else if (type == HandshakeType.MESSAGE_HASH.code()) {
				return decodeMessageHash(buffer);
			} else {
				throw new UnsupportedOperationException("TLS 不支持的握手消息类型:" + type);
			}
		}
		buffer.reset();
		return null;
	}

	private static void encode(HelloRequest message, DataBuffer buffer) throws IOException {
		// EMPTY content
	}

	private static HelloRequest decodeHelloRequest(DataBuffer buffer) throws IOException {
		return HelloRequest.INSTANCE;
	}

	private static void encode(ClientHello message, DataBuffer buffer) throws IOException {
		int index;
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getVersion());
		// Random 32Byte
		buffer.write(message.getRandom());
		// SessionID <0..32>
		buffer.writeByte(message.getSessionId().length);
		buffer.write(message.getSessionId());
		// CipherSuites <2..2^16-2>
		buffer.writeShort(message.getCipherSuites().length * 2);
		for (index = 0; index < message.getCipherSuites().length; index++) {
			buffer.writeShort(message.getCipherSuites()[index]);
		}
		// Compression Methods <1..2^8-1>
		buffer.writeByte(message.getCompressionMethods().length);
		buffer.write(message.getCompressionMethods());
		// Extensions <8..2^16-1>
		ExtensionCoder.encode(message, buffer);
	}

	private static ClientHello decodeClientHello(DataBuffer buffer) throws IOException {
		final ClientHello message = new ClientHello();
		// Version 2Byte
		message.setVersion(buffer.readShort());
		// Random 32Byte
		final byte[] random = new byte[32];
		buffer.readFully(random);
		message.setRandom(random);
		// SessionID
		final byte[] session = new byte[buffer.readUnsignedShort()];
		buffer.readFully(session);
		message.setSessionId(session);
		// CipherSuites
		final short[] suites = new short[buffer.readUnsignedShort() / 2];
		for (int index = 0; index < suites.length; index++) {
			suites[index] = buffer.readShort();
		}
		message.setCipherSuites(suites);
		// Compression Methods
		final byte[] methods = new byte[buffer.readUnsignedShort()];
		buffer.readFully(methods);
		message.setCompressionMethods(methods);
		// extensions
		ExtensionCoder.decode(message, buffer);
		return message;
	}

	private static void encode(ServerHello message, DataBuffer buffer) throws IOException {
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
		ExtensionCoder.encode(message, buffer);
	}

	private static ServerHello decodeServerHello(DataBuffer buffer) throws IOException {
		final ServerHello message = new ServerHello();
		// Version 2Byte
		message.setVersion(buffer.readShort());
		// Random 32Byte
		final byte[] random = new byte[32];
		buffer.readFully(random);
		message.setRandom(random);
		// SessionID
		final byte[] session = new byte[buffer.readUnsignedShort()];
		buffer.readFully(session);
		message.setSessionId(session);
		// CipherSuite
		message.setCipherSuite(buffer.readShort());
		// Compression Method
		message.setCompressionMethod(buffer.readByte());
		// extensions
		ExtensionCoder.decode(message, buffer);
		return message;
	}

	private static void encode(NewSessionTicket message, DataBuffer buffer) throws IOException {
		// uint32 ticket_lifetime;
		buffer.writeInt(message.getLifetime());
		// uint32 ticket_age_add;
		buffer.writeInt(message.getAgeAdd());
		// opaque ticket_nonce<0..255>;
		buffer.writeShort(message.getNonce().length);
		buffer.write(message.getNonce());
		// opaque ticket<1..2^16-1>;
		buffer.writeShort(message.getTicket().length);
		buffer.write(message.getTicket());
		// Extension extensions<0..2^16-2>;
		ExtensionCoder.encode(message, buffer);
	}

	private static NewSessionTicket decodeNewSessionTicket(DataBuffer buffer) throws IOException {
		final NewSessionTicket message = new NewSessionTicket();
		byte[] opaque;
		// uint32 ticket_lifetime;
		message.setLifetime(buffer.readInt());
		// uint32 ticket_age_add;
		message.setAgeAdd(buffer.readInt());
		// opaque ticket_nonce<0..255>;
		buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
		message.setNonce(opaque);
		// opaque ticket<1..2^16-1>;
		buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
		message.setTicket(opaque);
		// Extension extensions<0..2^16-2>;
		ExtensionCoder.decode(message, buffer);
		return message;
	}

	private static void encode(EndOfEarlyData message, DataBuffer buffer) throws IOException {
		// EMPTY content
	}

	private static EndOfEarlyData decodeEndOfEarlyData(DataBuffer buffer) throws IOException {
		return EndOfEarlyData.INSTANCE;
	}

	private static void encode(EncryptedExtensions message, DataBuffer buffer) throws IOException {
		// Extensions <6..2^16-1>
		ExtensionCoder.encode(message, buffer);
	}

	private static EncryptedExtensions decodeEncryptedExtensions(DataBuffer buffer) throws IOException {
		final EncryptedExtensions message = new EncryptedExtensions();
		// Extensions <6..2^16-1>
		ExtensionCoder.decode(message, buffer);
		return message;
	}

	private static void encode(Certificate message, DataBuffer buffer) throws IOException {
		if (message.getContext() != null) {
			buffer.writeShort(message.getContext().length);
			buffer.write(message.getContext());
		}
		CertificateEntry entry;
		for (int index = 0; index < message.getCertificates().length; index++) {
			entry = message.getCertificates()[index];
			buffer.writeShort(entry.getData().length);
			buffer.write(entry.getData());
		}
		// Extensions <6..2^16-1>
		ExtensionCoder.encode(message, buffer);
	}

	private static Certificate decodeCertificate(DataBuffer buffer) throws IOException {
		final Certificate message = new Certificate();
		// TODO
		// Extensions <6..2^16-1>
		ExtensionCoder.decode(message, buffer);
		return message;
	}

	private static void encode(ServerKeyExchange message, DataBuffer buffer) throws IOException {
		// TODO
	}

	private static ServerKeyExchange decodeServerKeyExchange(DataBuffer buffer) throws IOException {
		final ServerKeyExchange message = new ServerKeyExchange();
		// TODO

		return message;
	}

	private static void encode(CertificateRequest message, DataBuffer buffer) throws IOException {
		// opaque certificate_request_context<0..2^8-1>;
		buffer.writeShort(message.getCertificateRequestContext().length);
		buffer.write(message.getCertificateRequestContext());
		// Extension extensions<2..2^16-1>;
		ExtensionCoder.encode(message, buffer);
	}

	private static CertificateRequest decodeCertificateRequest(DataBuffer buffer) throws IOException {
		final CertificateRequest message = new CertificateRequest();
		byte[] opaque;
		// opaque certificate_request_context<0..2^8-1>;
		buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
		message.setCertificateRequestContext(opaque);
		// Extensions <6..2^16-1>
		ExtensionCoder.decode(message, buffer);
		return message;
	}

	private static void encode(ServerHelloDone message, DataBuffer buffer) throws IOException {
		// TODO
	}

	private static ServerHelloDone decodeServerHelloDone(DataBuffer buffer) throws IOException {
		final ServerHelloDone message = new ServerHelloDone();
		// TODO
		return message;
	}

	private static void encode(CertificateVerify message, DataBuffer buffer) throws IOException {
		// SignatureScheme algorithm;
		buffer.writeShort(message.getAlgorithm().code());
		// opaque signature<0..2^16-1>;
		buffer.writeShort(message.getSignature().length);
		buffer.write(message.getSignature());
	}

	private static CertificateVerify decodeCertificateVerify(DataBuffer buffer) throws IOException {
		final CertificateVerify message = new CertificateVerify();
		byte[] opaque;
		// SignatureScheme algorithm;
		message.setAlgorithm(buffer.readShort());
		// opaque signature<0..2^16-1>;
		buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
		message.setSignature(opaque);
		return message;
	}

	private static void encode(ClientKeyExchange message, DataBuffer buffer) throws IOException {
	}

	private static ClientKeyExchange decodeClientKeyExchange(DataBuffer buffer) throws IOException {
		final ClientKeyExchange message = new ClientKeyExchange();
		// TODO
		return message;
	}

	private static void encode(Finished message, DataBuffer buffer) throws IOException {
		buffer.writeShort(message.getData().length);
		buffer.write(message.getData());
	}

	private static Finished decodeFinished(DataBuffer buffer) throws IOException {
		final Finished message = new Finished();
		byte[] opaque;
		buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
		message.setData(opaque);
		return message;
	}

	private static void encode(CertificateURL message, DataBuffer buffer) throws IOException {
		// TODO
	}

	private static CertificateURL decodeCertificateURL(DataBuffer buffer) throws IOException {
		final CertificateURL message = new CertificateURL();
		// TODO
		return message;
	}

	private static void encode(CertificateStatus message, DataBuffer buffer) throws IOException {
		// TODO
	}

	private static CertificateStatus decodeCertificateStatus(DataBuffer buffer) throws IOException {
		final CertificateStatus message = new CertificateStatus();
		// TODO
		return message;
	}

	private static void encode(KeyUpdate message, DataBuffer buffer) throws IOException {
		buffer.writeByte(message.getRequest().code());
	}

	private static KeyUpdate decodeKeyUpdate(DataBuffer buffer) throws IOException {
		final KeyUpdate message = new KeyUpdate();
		message.setRequest(buffer.readUnsignedByte());
		return message;
	}

	private static void encode(MessageHash message, DataBuffer buffer) throws IOException {
		// TODO
	}

	private static MessageHash decodeMessageHash(DataBuffer buffer) throws IOException {
		final MessageHash message = new MessageHash();
		// TODO
		return message;
	}
}