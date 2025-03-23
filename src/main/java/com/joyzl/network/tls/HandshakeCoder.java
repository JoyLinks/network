package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.tls.Certificate.CertificateEntry;
import com.joyzl.network.tls.CertificateURL.URLAndHash;
import com.joyzl.network.tls.ServerKeyExchange.ServerDHParams;

class HandshakeCoder extends TLS {

	public static void encode(Handshake message, DataBuffer buffer) throws IOException {
		int position, length;

		// HandshakeType 1Byte
		buffer.writeByte(message.msgType());
		// Length 3Byte(uint24)
		position = buffer.readable();
		buffer.writeMedium(0);
		// SUB Handshake
		if (message.msgType() == Handshake.CLIENT_HELLO) {
			encode((ClientHello) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_HELLO) {
			encode((ServerHello) message, buffer);
		} else if (message.msgType() == Handshake.ENCRYPTED_EXTENSIONS) {
			// EMPTY
		} else if (message.msgType() == Handshake.CERTIFICATE_REQUEST) {
			encode((CertificateRequest) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE) {
			encodeV3((Certificate) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {
			encode((CertificateVerify) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_URL) {
			encode((CertificateURL) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_STATUS) {
			encode((CertificateStatus) message, buffer);
		} else if (message.msgType() == Handshake.NEW_SESSION_TICKET) {
			encode((NewSessionTicket) message, buffer);
		} else if (message.msgType() == Handshake.CLIENT_KEY_EXCHANGE) {
			encode((ClientKeyExchange) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_KEY_EXCHANGE) {
			encode((ServerKeyExchange) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_HELLO_DONE) {
			// EMPTY
		} else if (message.msgType() == Handshake.END_OF_EARLY_DATA) {
			// EMPTY
		} else if (message.msgType() == Handshake.HELLO_REQUEST) {
			// EMPTY
		} else if (message.msgType() == Handshake.KEY_UPDATE) {
			encode((KeyUpdate) message, buffer);
		} else if (message.msgType() == Handshake.FINISHED) {
			encode((Finished) message, buffer);
		} else {
			throw new UnsupportedOperationException("TLS:未知的握手消息类型" + message.msgType());
		}
		if (message.hasExtensions()) {
			// Extensions <8..2^16-1>
			ExtensionCoder.encode((HandshakeExtensions) message, buffer);
		}

		// SET LENGTH
		length = buffer.readable() - position - 3;
		buffer.set(position++, (byte) (length >>> 16));
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) (length));
	}

	public static Handshake decode(short version, DataBuffer buffer) throws IOException {
		// HandshakeType 1Byte
		int type = buffer.readByte();
		// uint24 length 3Byte
		int length = buffer.readUnsignedMedium();

		Handshake message;
		if (buffer.readable() >= length) {
			// 消息主体解码后剩余字节
			final int excess = buffer.readable() - length;

			if (type == Handshake.CLIENT_HELLO) {
				message = decodeClientHello(buffer);
			} else if (type == Handshake.SERVER_HELLO) {
				message = decodeServerHello(buffer);
			} else if (type == Handshake.ENCRYPTED_EXTENSIONS) {
				message = new EncryptedExtensions();
			} else if (type == Handshake.CERTIFICATE_REQUEST) {
				message = decodeCertificateRequest(buffer);
			} else if (type == Handshake.CERTIFICATE) {
				if (version == TLS.V13) {
					message = decodeCertificateV3(buffer);
				} else {
					message = decodeCertificateV2(buffer);
				}
			} else if (type == Handshake.CERTIFICATE_VERIFY) {
				message = decodeCertificateVerify(buffer);
			} else if (type == Handshake.CERTIFICATE_URL) {
				message = decodeCertificateURL(buffer);
			} else if (type == Handshake.CERTIFICATE_STATUS) {
				message = decodeCertificateStatus(buffer);
			} else if (type == Handshake.NEW_SESSION_TICKET) {
				message = decodeNewSessionTicket(buffer);
			} else if (type == Handshake.END_OF_EARLY_DATA) {
				message = EndOfEarlyData.INSTANCE;
			} else if (type == Handshake.SERVER_HELLO_DONE) {
				message = ServerHelloDone.INSTANCE;
			} else if (type == Handshake.SERVER_KEY_EXCHANGE) {
				message = decodeServerKeyExchange(buffer, length);
			} else if (type == Handshake.CLIENT_KEY_EXCHANGE) {
				message = decodeClientKeyExchange(buffer);
			} else if (type == Handshake.HELLO_REQUEST) {
				message = HelloRequest.INSTANCE;
			} else if (type == Handshake.KEY_UPDATE) {
				message = decodeKeyUpdate(buffer);
			} else if (type == Handshake.FINISHED) {
				message = decodeFinished(buffer, length);
			} else {
				throw new UnsupportedOperationException("TLS:未知的握手消息类型" + type);
			}

			// 旧版本应监测主体解码后是否剩余
			// 如果有剩余在尝试解码扩展
			if (buffer.readable() > excess) {
				if (message instanceof HandshakeExtensions) {
					// Extensions <8..2^16-1>
					ExtensionCoder.decode((HandshakeExtensions) message, buffer);
				}
			}
		} else {
			throw new IllegalArgumentException("TLS:数据缺失");
		}
		return message;
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
		message.setRandom(new byte[32]);
		buffer.readFully(message.getRandom());
		// SessionID <0..32>
		int size = buffer.readUnsignedByte();
		if (size > 0) {
			message.setSessionId(new byte[size]);
			buffer.readFully(message.getSessionId());
		}
		// CipherSuites <2..2^16-2>
		size = buffer.readUnsignedShort() / 2;
		message.setCipherSuites(new short[size]);
		for (int index = 0; index < size; index++) {
			message.getCipherSuites()[index] = buffer.readShort();
		}
		// Compression Methods <1..2^8-1>
		size = buffer.readUnsignedByte();
		if (size > 0) {
			message.setCompressionMethods(new byte[size]);
			buffer.readFully(message.getCompressionMethods());
		}
		return message;
	}

	private static void encode(ServerHello message, DataBuffer buffer) throws IOException {
		// ProtocolVersion 2Byte
		buffer.writeShort(message.getVersion());
		// Random 32Byte
		buffer.write(message.getRandom());
		// SessionID <0..32>
		buffer.writeByte(message.getSessionId().length);
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
		// Random 32Byte
		message.setRandom(new byte[32]);
		buffer.readFully(message.getRandom());
		// SessionID
		int size = buffer.readUnsignedByte();
		if (size > 0 && size <= 32) {
			message.setSessionId(new byte[size]);
			buffer.readFully(message.getSessionId());
		}
		// CipherSuite
		message.setCipherSuite(buffer.readShort());
		// Compression Method
		message.setCompressionMethod(buffer.readByte());
		return message;
	}

	/**
	 * 检查待解码握手消息是否HelloRetryRequest
	 */
	static boolean isHelloRetryRequest(DataBuffer buffer) {
		// type 1byte + length uint24 + legacy_version 2byte | random[32]

		for (int i = 0; i < ServerHello.HELLO_RETRY_REQUEST_RANDOM.length; i++) {
			if (buffer.get(6 + i) != ServerHello.HELLO_RETRY_REQUEST_RANDOM[i]) {
				return false;
			}
		}
		return true;
	}

	private static void encode(NewSessionTicket message, DataBuffer buffer) throws IOException {
		// uint32 ticket_lifetime;
		buffer.writeInt(message.getLifetime());
		// uint32 ticket_age_add;
		buffer.writeInt(message.getAgeAdd());
		// opaque ticket_nonce<0..255>;
		buffer.writeByte(message.getNonce().length);
		buffer.write(message.getNonce());
		// opaque ticket<1..2^16-1>;
		buffer.writeShort(message.getTicket().length);
		buffer.write(message.getTicket());
	}

	private static NewSessionTicket decodeNewSessionTicket(DataBuffer buffer) throws IOException {
		final NewSessionTicket message = new NewSessionTicket();
		// uint32 ticket_lifetime;
		message.setLifetime(buffer.readInt());
		// uint32 ticket_age_add;
		message.setAgeAdd(buffer.readInt());
		// opaque ticket_nonce<0..255>;
		message.setNonce(new byte[buffer.readUnsignedByte()]);
		buffer.readFully(message.getNonce());
		// opaque ticket<1..2^16-1>;
		message.setTicket(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.getTicket());
		return message;
	}

	private static void encodeV3(Certificate message, DataBuffer buffer) throws IOException {
		// opaque certificate_request_context<0..2^8-1>;
		buffer.writeByte(message.getContext().length);
		buffer.write(message.getContext());

		// CertificateEntry certificate_list<0..2^24-1>;
		int position = buffer.readable();
		buffer.writeMedium(0);

		CertificateEntry entry;
		for (int index = 0; index < message.get().length; index++) {
			entry = message.get()[index];

			// opaque cert_data<1..2^24-1>;
			buffer.writeMedium(entry.getData().length);
			buffer.write(entry.getData());
			ExtensionCoder.encode(entry, buffer);
		}

		// SET LENGTH
		int length = buffer.readable() - position - 3;
		buffer.set(position++, (byte) (length >>> 16));
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) (length));
	}

	private static Certificate decodeCertificateV3(DataBuffer buffer) throws IOException {
		final Certificate message = new Certificate();
		// opaque certificate_request_context<0..2^8-1>;
		int length = buffer.readUnsignedByte();
		if (length > 0) {
			message.setContext(new byte[length]);
			buffer.readFully(message.getContext());
		}
		// CertificateEntry certificate_list<0..2^24-1>;
		length = buffer.readUnsignedMedium();
		length = buffer.readable() - length;

		CertificateEntry entry;
		while (buffer.readable() > length) {
			entry = new CertificateEntry();
			// opaque cert_data<1..2^24-1>;
			entry.setData(new byte[buffer.readUnsignedMedium()]);
			buffer.readFully(entry.getData());
			ExtensionCoder.decode(entry, buffer);
			message.add(entry);
		}
		return message;
	}

	private static void encodeV2(Certificate message, DataBuffer buffer) throws IOException {
		// ASN.1Cert certificate_list<0..2^24-1>;
		int position = buffer.readable();
		buffer.writeMedium(0);

		CertificateEntry entry;
		for (int index = 0; index < message.get().length; index++) {
			entry = message.get()[index];
			// opaque ASN.1Cert<1..2^24-1>;
			buffer.writeMedium(entry.getData().length);
			buffer.write(entry.getData());
			ExtensionCoder.encode(entry, buffer);
		}

		// SET LENGTH
		int length = buffer.readable() - position - 3;
		buffer.set(position++, (byte) (length >>> 16));
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) (length));
	}

	private static Certificate decodeCertificateV2(DataBuffer buffer) throws IOException {
		final Certificate message = new Certificate();

		// CertificateEntry certificate_list<0..2^24-1>;
		int length = buffer.readUnsignedMedium();
		length = buffer.readable() - length;

		CertificateEntry entry;
		while (buffer.readable() > length) {
			entry = new CertificateEntry();
			// opaque ASN.1Cert<1..2^24-1>;
			entry.setData(new byte[buffer.readUnsignedMedium()]);
			buffer.readFully(entry.getData());
			message.add(entry);
		}
		return message;
	}

	private static void encode(CertificateRequest message, DataBuffer buffer) throws IOException {
		// opaque certificate_request_context<0..2^8-1>;
		buffer.writeShort(message.getContext().length);
		buffer.write(message.getContext());
	}

	private static CertificateRequest decodeCertificateRequest(DataBuffer buffer) throws IOException {
		final CertificateRequest message = new CertificateRequest();
		// opaque certificate_request_context<0..2^8-1>;
		message.setContext(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.getContext());
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
		// SignatureScheme algorithm;
		message.setAlgorithm(buffer.readShort());
		// opaque signature<0..2^16-1>;
		message.setSignature(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.getSignature());
		return message;
	}

	private static void encode(Finished message, DataBuffer buffer) throws IOException {
		// buffer.writeShort(message.getVerifyData().length);
		buffer.write(message.getVerifyData());
	}

	private static Finished decodeFinished(DataBuffer buffer, int length) throws IOException {
		final Finished message = new Finished();
		if (length > 0) {
			message.setVerifyData(new byte[length]);
			buffer.readFully(message.getVerifyData());
		}
		return message;
	}

	private static void encode(CertificateURL message, DataBuffer buffer) throws IOException {
		// CertChainType type;
		buffer.writeByte(message.getCertChainType());

		// URLAndHash url_and_hash_list<1..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);

		URLAndHash url;
		for (int u = 0; u < message.size(); u++) {
			url = message.getURL(u);
			// opaque url<1..2^16-1>;
			buffer.writeShort(url.getURL().length);
			buffer.write(url.getURL());
			// unint8 padding(0x01);
			buffer.writeByte(0x01);
			// opaque SHA1Hash[20];
			// TODO LENGTH?
			buffer.write(url.getHash());
		}

		// SET LENGTH
		int length = buffer.readable() - position - 3;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) (length));
	}

	private static CertificateURL decodeCertificateURL(DataBuffer buffer) throws IOException {
		final CertificateURL message = new CertificateURL();
		// CertChainType type;
		message.setCertChainType(buffer.readByte());
		// URLAndHash url_and_hash_list<1..2^16-1>;
		int length = buffer.readUnsignedShort();
		URLAndHash url;
		while (length > 0) {
			url = new URLAndHash();
			// opaque url<1..2^16-1>;
			url.setURL(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(url.getURL());
			// unint8 padding(0x01);
			buffer.readByte();
			// opaque SHA1Hash[20];
			// TODO LENGTH?
			url.setHash(new byte[20]);
			buffer.readFully(url.getHash());

			length -= 2 + url.getURL().length + 1 + 20;
			message.addURL(url);
		}
		return message;
	}

	private static void encode(CertificateStatus message, DataBuffer buffer) throws IOException {
		// CertificateStatusType status_type;
		buffer.writeByte(message.getStatusType());

		if (message.getStatusType() == CertificateStatusRequest.OCSP) {
			// opaque OCSPResponse<1..2^24-1>;
			buffer.writeMedium(message.getResponse().length);
			buffer.write(message.getResponse());
		} else if (message.getStatusType() == CertificateStatusRequestListV2.OCSP_MULTI) {
			// ocsp_response_list<1..2^24-1>;
			int position = buffer.readable();
			buffer.writeMedium(0);

			for (int r = 0; r < message.size(); r++) {
				// opaque OCSPResponse<1..2^24-1>;
				buffer.writeMedium(message.getResponse(r).length);
				buffer.write(message.getResponse(r));
			}

			// SET LENGTH
			int length = buffer.readable() - position - 3;
			buffer.set(position++, (byte) (length >>> 16));
			buffer.set(position++, (byte) (length >>> 8));
			buffer.set(position, (byte) (length));
		}
	}

	private static CertificateStatus decodeCertificateStatus(DataBuffer buffer) throws IOException {
		final CertificateStatus message = new CertificateStatus();
		// CertificateStatusType status_type;
		message.setStatusType(buffer.readByte());

		if (message.getStatusType() == CertificateStatusRequest.OCSP) {
			// opaque OCSPResponse<1..2^24-1>;
			message.setResponse(new byte[buffer.readUnsignedMedium()]);
			buffer.readFully(message.getResponse());
		} else if (message.getStatusType() == CertificateStatusRequestListV2.OCSP_MULTI) {
			// ocsp_response_list<1..2^24-1>;
			int length = buffer.readUnsignedMedium();
			byte[] opaque;
			while (length > 0) {
				// opaque OCSPResponse<1..2^24-1>;
				message.addResponse(opaque = new byte[buffer.readUnsignedMedium()]);
				buffer.readFully(message.getResponse());
				length -= opaque.length + 3;
			}
		}
		return message;
	}

	private static void encode(KeyUpdate message, DataBuffer buffer) throws IOException {
		buffer.writeByte(message.get());
	}

	private static KeyUpdate decodeKeyUpdate(DataBuffer buffer) throws IOException {
		return new KeyUpdate(buffer.readByte());
	}

	private static void encode(ServerKeyExchange message, DataBuffer buffer) throws IOException {
		if (message.getParams() != null) {
			// opaque dh_p<1..2^16-1>;
			buffer.writeShort(message.getParams().getP().length);
			buffer.write(message.getParams().getP());
			// opaque dh_g<1..2^16-1>;
			buffer.writeShort(message.getParams().getG().length);
			buffer.write(message.getParams().getG());
			// opaque dh_Ys<1..2^16-1>;
			buffer.writeShort(message.getParams().getYs().length);
			buffer.write(message.getParams().getYs());

			// signed_params
			if (message.getSigned().length > 0) {
				buffer.writeShort(message.getSigned().length);
				buffer.write(message.getSigned());
			}
		} else {
			// EMPTY
		}
	}

	private static ServerKeyExchange decodeServerKeyExchange(DataBuffer buffer, int length) throws IOException {
		final ServerKeyExchange message = new ServerKeyExchange();
		if (length > 0) {
			final ServerDHParams params = new ServerDHParams();
			params.setP(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getP());
			params.setG(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getG());
			params.setYs(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getYs());

			length -= params.length() + 6;
			if (length > 0) {
				message.setSigned(new byte[buffer.readUnsignedShort()]);
				buffer.readFully(message.getSigned());
			}
			message.setParams(params);
		} else {
			// EMPTY
		}
		return message;
	}

	private static void encode(ClientKeyExchange message, DataBuffer buffer) throws IOException {
		buffer.writeShort(message.get().length);
		buffer.write(message.get());
	}

	private static ClientKeyExchange decodeClientKeyExchange(DataBuffer buffer) throws IOException {
		final ClientKeyExchange message = new ClientKeyExchange();
		message.set(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.get());
		return message;
	}
}