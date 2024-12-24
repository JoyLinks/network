package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

public class HandshakeCoder extends TLS {

	/**
	 * <pre>
	 * ClientHello
	 * Certificate
	 * CertificateURL
	 * ClientKeyExchange
	 * CertificateVerify
	 * EndOfEarlyData
	 * Finished
	 * KeyUpdate
	 * </pre>
	 */
	public static void encodeByClient(Handshake message, DataBuffer buffer) throws IOException {
		// HandshakeType 1Byte
		buffer.writeByte(message.msgType());
		// Length 3Byte(uint24)
		int position = buffer.readable();
		buffer.writeMedium(0);
		// SUB Handshake
		if (message.msgType() == Handshake.CLIENT_HELLO) {
			encode((ClientHello) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE) {
			encode((Certificate) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_URL) {
			encode((CertificateURL) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {
			encode((CertificateVerify) message, buffer);
		} else if (message.msgType() == Handshake.CLIENT_KEY_EXCHANGE) {
			encode((ClientKeyExchange) message, buffer);
		} else if (message.msgType() == Handshake.END_OF_EARLY_DATA) {
			encode((EndOfEarlyData) message, buffer);
		} else if (message.msgType() == Handshake.FINISHED) {
			encode((Finished) message, buffer);
		} else if (message.msgType() == Handshake.KEY_UPDATE) {
			encode((KeyUpdate) message, buffer);
		} else {
			throw new UnsupportedOperationException("TLS 不支持的握手消息类型:" + message.msgType());
		}
		if (message.hasExtensions()) {
			// Extensions <8..2^16-1>
			ExtensionCoder.encodeByClient((HandshakeExtensions) message, buffer);
		}
		// SET LENGTH
		int length = buffer.readable() - position - 3;
		buffer.set(position++, (byte) (length >>> 16));
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) (length));
	}

	/**
	 * <pre>
	 * ServerHello
	 * HelloRetryRequest
	 * EncryptedExtensions
	 * Certificate
	 * ServerKeyExchange
	 * CertificateRequest
	 * CertificateVerify
	 * ServerHelloDone
	 * NewSessionTicket
	 * Finished
	 * HelloRequest
	 * KeyUpdate
	 * </pre>
	 */
	public static void encodeByServer(Handshake message, DataBuffer buffer) throws IOException {
		// HandshakeType 1Byte
		buffer.writeByte(message.msgType());
		// Length 3Byte(uint24)
		int position = buffer.readable();
		buffer.writeMedium(0);
		// SUB Handshake
		if (message.msgType() == Handshake.SERVER_HELLO) {
			encode((ServerHello) message, buffer);
		} else if (message.msgType() == Handshake.NEW_SESSION_TICKET) {
			encode((NewSessionTicket) message, buffer);
		} else if (message.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
			encode((EncryptedExtensions) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE) {
			encode((Certificate) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_KEY_EXCHANGE) {
			encode((ServerKeyExchange) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_REQUEST) {
			encode((CertificateRequest) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_HELLO_DONE) {
			encode((ServerHelloDone) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {
			encode((CertificateVerify) message, buffer);
		} else if (message.msgType() == Handshake.FINISHED) {
			encode((Finished) message, buffer);
		} else if (message.msgType() == Handshake.HELLO_REQUEST) {
			encode((HelloRequest) message, buffer);
		} else if (message.msgType() == Handshake.KEY_UPDATE) {
			encode((KeyUpdate) message, buffer);
		} else {
			throw new UnsupportedOperationException("TLS 不支持的握手消息类型:" + message.msgType());
		}
		if (message.hasExtensions()) {
			// Extensions <8..2^16-1>
			ExtensionCoder.encodeByServer((HandshakeExtensions) message, buffer);
		}
		// SET LENGTH
		int length = buffer.readable() - position - 3;
		buffer.set(position++, (byte) (length >>> 16));
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) (length));
	}

	/**
	 * <pre>
	 * ServerHello
	 * HelloRetryRequest
	 * EncryptedExtensions
	 * Certificate
	 * ServerKeyExchange
	 * CertificateRequest
	 * CertificateVerify
	 * ServerHelloDone
	 * NewSessionTicket
	 * Finished
	 * HelloRequest
	 * KeyUpdate
	 * </pre>
	 */
	public static Handshake decodeByClient(DataBuffer buffer) throws IOException {
		buffer.mark();
		// HandshakeType 1Byte
		final int type = buffer.readByte();
		// uint24 length
		int length = buffer.readUnsignedMedium();
		if (buffer.readable() >= length) {
			final Handshake message;
			if (type == Handshake.SERVER_HELLO) {
				message = decodeServerHello(buffer);
			} else if (type == Handshake.NEW_SESSION_TICKET) {
				message = decodeNewSessionTicket(buffer);
			} else if (type == Handshake.ENCRYPTED_EXTENSIONS) {
				message = decodeEncryptedExtensions(buffer);
			} else if (type == Handshake.CERTIFICATE) {
				message = decodeCertificate(buffer);
			} else if (type == Handshake.SERVER_KEY_EXCHANGE) {
				message = decodeServerKeyExchange(buffer);
			} else if (type == Handshake.CERTIFICATE_REQUEST) {
				message = decodeCertificateRequest(buffer);
			} else if (type == Handshake.SERVER_HELLO_DONE) {
				message = decodeServerHelloDone(buffer);
			} else if (type == Handshake.CERTIFICATE_VERIFY) {
				message = decodeCertificateVerify(buffer);
			} else if (type == Handshake.FINISHED) {
				message = decodeFinished(buffer);
			} else if (type == Handshake.HELLO_REQUEST) {
				message = decodeHelloRequest(buffer);
			} else if (type == Handshake.KEY_UPDATE) {
				message = decodeKeyUpdate(buffer);
			} else {
				throw new UnsupportedOperationException("TLS 不支持的握手消息类型:" + type);
			}
			if (message instanceof HandshakeExtensions) {
				// Extensions <8..2^16-1>
				ExtensionCoder.decodeByClient((HandshakeExtensions) message, buffer);
			}
			return message;
		}
		buffer.reset();
		return null;
	}

	/**
	 * <pre>
	 * ClientHello
	 * Certificate
	 * CertificateURL
	 * ClientKeyExchange
	 * CertificateVerify
	 * EndOfEarlyData
	 * Finished
	 * KeyUpdate
	 * </pre>
	 */
	public static Handshake decodeByServer(DataBuffer buffer) throws IOException {
		buffer.mark();
		// HandshakeType 1Byte
		final int type = buffer.readByte();
		// uint24 length
		int length = buffer.readUnsignedMedium();
		if (buffer.readable() >= length) {
			final Handshake message;
			if (type == Handshake.CLIENT_HELLO) {
				message = decodeClientHello(buffer);
			} else if (type == Handshake.END_OF_EARLY_DATA) {
				message = decodeEndOfEarlyData(buffer);
			} else if (type == Handshake.CERTIFICATE) {
				message = decodeCertificate(buffer);
			} else if (type == Handshake.CERTIFICATE_URL) {
				message = decodeCertificateURL(buffer);
			} else if (type == Handshake.CERTIFICATE_VERIFY) {
				message = decodeCertificateVerify(buffer);
			} else if (type == Handshake.CLIENT_KEY_EXCHANGE) {
				message = decodeClientKeyExchange(buffer);
			} else if (type == Handshake.FINISHED) {
				message = decodeFinished(buffer);
			} else if (type == Handshake.KEY_UPDATE) {
				message = decodeKeyUpdate(buffer);
			} else {
				throw new UnsupportedOperationException("TLS 不支持的握手消息类型:" + type);
			}
			if (message instanceof HandshakeExtensions) {
				// Extensions <8..2^16-1>
				ExtensionCoder.decodeByServer((HandshakeExtensions) message, buffer);
			}
			return message;
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
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getVersion());
		// Random 32Byte
		buffer.write(message.getRandom());
		// SessionID <0..32>
		buffer.writeByte(message.getSessionId().length);
		buffer.write(message.getSessionId());
		// CipherSuites <2..2^16-2>
		buffer.writeShort(message.getCipherSuites().length * 2);
		for (int index = 0; index < message.getCipherSuites().length; index++) {
			buffer.writeShort(message.getCipherSuites()[index]);
		}
		// Compression Methods <1..2^8-1>
		buffer.writeByte(message.getCompressionMethods().length);
		buffer.write(message.getCompressionMethods());
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
	}

	private static ServerHello decodeServerHello(DataBuffer buffer) throws IOException {
		final ServerHello message = new ServerHello();
		// Version 2Byte
		message.setVersion(buffer.readShort());
		byte[] opaque;
		// Random 32Byte
		buffer.readFully(opaque = new byte[32]);
		message.setRandom(opaque);
		// SessionID
		buffer.readFully(opaque = new byte[buffer.readUnsignedByte()]);
		message.setSessionId(opaque);
		// CipherSuite
		message.setCipherSuite(buffer.readShort());
		// Compression Method
		message.setCompressionMethod(buffer.readByte());
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
		return message;
	}

	private static void encode(EndOfEarlyData message, DataBuffer buffer) throws IOException {
		// EMPTY content
	}

	private static EndOfEarlyData decodeEndOfEarlyData(DataBuffer buffer) throws IOException {
		return EndOfEarlyData.INSTANCE;
	}

	private static void encode(EncryptedExtensions message, DataBuffer buffer) throws IOException {
		// TODO
	}

	private static EncryptedExtensions decodeEncryptedExtensions(DataBuffer buffer) throws IOException {
		final EncryptedExtensions message = new EncryptedExtensions();
		// TODO
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
	}

	private static Certificate decodeCertificate(DataBuffer buffer) throws IOException {
		final Certificate message = new Certificate();
		// TODO
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
	}

	private static CertificateRequest decodeCertificateRequest(DataBuffer buffer) throws IOException {
		final CertificateRequest message = new CertificateRequest();
		byte[] opaque;
		// opaque certificate_request_context<0..2^8-1>;
		buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
		message.setCertificateRequestContext(opaque);
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
		buffer.writeShort(message.getAlgorithm());
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
		buffer.writeShort(message.getVerifyData().length);
		buffer.write(message.getVerifyData());
	}

	private static Finished decodeFinished(DataBuffer buffer) throws IOException {
		final Finished message = new Finished();
		byte[] opaque;
		buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
		message.setVerifyData(opaque);
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
		buffer.writeByte(message.getRequest());
	}

	private static KeyUpdate decodeKeyUpdate(DataBuffer buffer) throws IOException {
		return new KeyUpdate(buffer.readByte());
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