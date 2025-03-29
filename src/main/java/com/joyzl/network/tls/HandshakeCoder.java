package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.tls.CertificateURL.URLAndHash;
import com.joyzl.network.tls.CertificateV3.CertificateEntry;

class HandshakeCoder extends TLS {

	/*-
	 * 1.0 1.1 1.2
	 * enum {
	 *       hello_request(0), 
	 *       client_hello(1), 
	 *       server_hello(2),
	 *       certificate(11), 
	 *       server_key_exchange (12),
	 *       certificate_request(13), 
	 *       server_hello_done(14),
	 *       certificate_verify(15), 
	 *       client_key_exchange(16),
	 *       finished(20), 
	 *       (255)
	 * } HandshakeType;
	 * 
	 * struct {
	 *       HandshakeType msg_type;    // handshake type 
	 *       uint24 length;             // bytes in message 
	 *       select (HandshakeType) {
	 *             case hello_request:       HelloRequest;
	 *             case client_hello:        ClientHello;
	 *             case server_hello:        ServerHello;
	 *             case certificate:         Certificate;
	 *             case server_key_exchange: ServerKeyExchange;
	 *             case certificate_request: CertificateRequest;
	 *             case server_hello_done:   ServerHelloDone;
	 *             case certificate_verify:  CertificateVerify;
	 *             case client_key_exchange: ClientKeyExchange;
	 *             case finished:            Finished;
	 *       } body;
	 * } Handshake;
	 * 
	 * NewSessionTicket1 适用此三个版本
	 */

	public static void encodeV0(Handshake message, DataBuffer buffer) throws IOException {
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
		} else if (message.msgType() == Handshake.CERTIFICATE) {
			encode((CertificateV0) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_REQUEST) {
			encode((CertificateRequestV0) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {
			encode((CertificateVerifyV0) message, buffer);
		} else if (message.msgType() == Handshake.NEW_SESSION_TICKET) {
			encode((NewSessionTicket1) message, buffer);
		} else if (message.msgType() == Handshake.CLIENT_KEY_EXCHANGE) {
			encode((ClientKeyExchange) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_KEY_EXCHANGE) {
			encode((ServerKeyExchangeV0) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_HELLO_DONE) {
			// EMPTY
		} else if (message.msgType() == Handshake.HELLO_REQUEST) {
			// EMPTY
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

	public static void encodeV2(Handshake message, DataBuffer buffer) throws IOException {
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
		} else if (message.msgType() == Handshake.CERTIFICATE) {
			encode((CertificateV0) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_REQUEST) {
			encode((CertificateRequestV2) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {
			encode((CertificateVerifyV2) message, buffer);
		} else if (message.msgType() == Handshake.NEW_SESSION_TICKET) {
			encode((NewSessionTicket1) message, buffer);
		} else if (message.msgType() == Handshake.CLIENT_KEY_EXCHANGE) {
			encode((ClientKeyExchange) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_KEY_EXCHANGE) {
			encode((ServerKeyExchangeV2) message, buffer);
		} else if (message.msgType() == Handshake.SERVER_HELLO_DONE) {
			// EMPTY
		} else if (message.msgType() == Handshake.HELLO_REQUEST) {
			// EMPTY
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

	public static Handshake decodeV0(DataBuffer buffer) throws IOException {
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
			} else if (type == Handshake.CERTIFICATE) {
				message = decodeCertificateV0(buffer);
			} else if (type == Handshake.CERTIFICATE_REQUEST) {
				message = decodeCertificateRequestV0(buffer);
			} else if (type == Handshake.CERTIFICATE_VERIFY) {
				message = decodeCertificateVerifyV0(buffer, length);
			} else if (type == Handshake.NEW_SESSION_TICKET) {
				message = decodeNewSessionTicket1(buffer);
			} else if (type == Handshake.SERVER_KEY_EXCHANGE) {
				message = decodeServerKeyExchangeV0(buffer, length);
			} else if (type == Handshake.CLIENT_KEY_EXCHANGE) {
				message = decodeClientKeyExchange(buffer);
			} else if (type == Handshake.SERVER_HELLO_DONE) {
				message = ServerHelloDone.INSTANCE;
			} else if (type == Handshake.HELLO_REQUEST) {
				message = HelloRequest.INSTANCE;
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

	public static Handshake decodeV2(DataBuffer buffer) throws IOException {
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
			} else if (type == Handshake.CERTIFICATE) {
				message = decodeCertificateV0(buffer);
			} else if (type == Handshake.CERTIFICATE_REQUEST) {
				message = decodeCertificateRequestV2(buffer);
			} else if (type == Handshake.CERTIFICATE_VERIFY) {
				message = decodeCertificateVerifyV2(buffer);
			} else if (type == Handshake.NEW_SESSION_TICKET) {
				message = decodeNewSessionTicket1(buffer);
			} else if (type == Handshake.SERVER_KEY_EXCHANGE) {
				message = decodeServerKeyExchangeV2(buffer, length);
			} else if (type == Handshake.CLIENT_KEY_EXCHANGE) {
				message = decodeClientKeyExchange(buffer);
			} else if (type == Handshake.SERVER_HELLO_DONE) {
				message = ServerHelloDone.INSTANCE;
			} else if (type == Handshake.HELLO_REQUEST) {
				message = HelloRequest.INSTANCE;
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

	/*-
	 * 1.3
	 * enum {
	 *       hello_request_RESERVED(0),
	 *       client_hello(1),
	 *       server_hello(2),
	 *       hello_verify_request_RESERVED(3),
	 *       new_session_ticket(4),
	 *       end_of_early_data(5),
	 *       hello_retry_request_RESERVED(6),
	 *       encrypted_extensions(8),
	 *       certificate(11),
	 *       server_key_exchange_RESERVED(12),
	 *       certificate_request(13),
	 *       server_hello_done_RESERVED(14),
	 *       certificate_verify(15),
	 *       client_key_exchange_RESERVED(16),
	 *       finished(20),
	 *       certificate_url_RESERVED(21),
	 *       certificate_status_RESERVED(22),
	 *       supplemental_data_RESERVED(23),
	 *       key_update(24),
	 *       message_hash(254),
	 *       (255)
	 * } HandshakeType;
	 * 
	 * struct {
	 *       HandshakeType msg_type;    // handshake type 
	 *       uint24 length;             // bytes in message 
	 *       select (Handshake.msg_type) {
	 *             case client_hello:          ClientHello;
	 *             case server_hello:          ServerHello;
	 *             case end_of_early_data:     EndOfEarlyData;
	 *             case encrypted_extensions:  EncryptedExtensions;
	 *             case certificate_request:   CertificateRequest;
	 *             case certificate:           Certificate;
	 *             case certificate_verify:    CertificateVerify;
	 *             case finished:              Finished;
	 *             case new_session_ticket:    NewSessionTicket;
	 *             case key_update:            KeyUpdate;
	 *       };
	 * } Handshake;
	 * 
	 * NewSessionTicket2 适用此版本
	 */

	public static void encodeV3(Handshake message, DataBuffer buffer) throws IOException {
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
			encode((CertificateRequestV3) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE) {
			encode((CertificateV3) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_VERIFY) {
			encode((CertificateVerifyV3) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_URL) {
			encode((CertificateURL) message, buffer);
		} else if (message.msgType() == Handshake.CERTIFICATE_STATUS) {
			encode((CertificateStatus) message, buffer);
		} else if (message.msgType() == Handshake.NEW_SESSION_TICKET) {
			encode((NewSessionTicket2) message, buffer);
		} else if (message.msgType() == Handshake.END_OF_EARLY_DATA) {
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

	public static Handshake decodeV3(DataBuffer buffer) throws IOException {
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
				message = decodeCertificateRequestV3(buffer);
			} else if (type == Handshake.CERTIFICATE) {
				message = decodeCertificateV3(buffer);
			} else if (type == Handshake.CERTIFICATE_VERIFY) {
				message = decodeCertificateVerifyV3(buffer);
			} else if (type == Handshake.CERTIFICATE_URL) {
				message = decodeCertificateURL(buffer);
			} else if (type == Handshake.CERTIFICATE_STATUS) {
				message = decodeCertificateStatus(buffer);
			} else if (type == Handshake.NEW_SESSION_TICKET) {
				message = decodeNewSessionTicket2(buffer);
			} else if (type == Handshake.END_OF_EARLY_DATA) {
				message = EndOfEarlyData.INSTANCE;
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

	static void encode(ClientHello message, DataBuffer buffer) throws IOException {
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

	static ClientHello decodeClientHello(DataBuffer buffer) throws IOException {
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

	static void encode(ServerHello message, DataBuffer buffer) throws IOException {
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

	static ServerHello decodeServerHello(DataBuffer buffer) throws IOException {
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

	static void encode(CertificateV0 certificate, DataBuffer buffer) throws IOException {
		// ASN.1Cert certificate_list<0..2^24-1>;
		int length = 0, c;
		for (c = 0; c < certificate.size(); c++) {
			length += certificate.get(c).length;
			length += 3;
		}
		buffer.writeMedium(length);

		for (c = 0; c < certificate.size(); c++) {
			// opaque ASN.1Cert<1..2^24-1>;
			buffer.writeMedium(certificate.get(c).length);
			buffer.write(certificate.get(c));
		}
	}

	static CertificateV0 decodeCertificateV0(DataBuffer buffer) throws IOException {
		final CertificateV0 certificate = new CertificateV0();

		// ASN.1Cert certificate_list<0..2^24-1>;
		int length = buffer.readUnsignedMedium();
		length = buffer.readable() - length;
		byte[] opaque;
		while (buffer.readable() > length) {
			// opaque ASN.1Cert<1..2^24-1>;
			opaque = new byte[buffer.readUnsignedMedium()];
			buffer.readFully(opaque);
			certificate.add(opaque);
		}
		return certificate;
	}

	static void encode(CertificateV3 message, DataBuffer buffer) throws IOException {
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

	static CertificateV3 decodeCertificateV3(DataBuffer buffer) throws IOException {
		final CertificateV3 message = new CertificateV3();
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

	static void encode(CertificateRequestV0 request, DataBuffer buffer) throws IOException {
		// ClientCertificateType certificate_types<1..2^8-1>;
		buffer.writeByte(request.typeSize());
		buffer.write(request.getTypes());

		// DistinguishedName certificate_authorities<3..2^16-1>;
		int length = 0, n;
		for (n = 0; n < request.nameSize(); n++) {
			length += request.getName(n).length;
			length += 2;
		}
		buffer.writeShort(length);
		for (n = 0; n < request.nameSize(); n++) {
			// opaque DistinguishedName<1..2^16-1>;
			buffer.writeShort(request.getName(n).length);
			buffer.write(request.getName(n));
		}
	}

	static CertificateRequestV0 decodeCertificateRequestV0(DataBuffer buffer) throws IOException {
		final CertificateRequestV0 request = new CertificateRequestV0();

		// ClientCertificateType certificate_types<1..2^8-1>;
		request.setTypes(new byte[buffer.readUnsignedByte()]);
		buffer.readFully(request.getTypes());

		// DistinguishedName certificate_authorities<3..2^16-1>;
		int length = buffer.readUnsignedShort();
		if (length > 0) {
			byte[] dname;
			while (length > 0) {
				// opaque DistinguishedName<1..2^16-1>;
				buffer.readFully(dname = new byte[buffer.readUnsignedShort()]);
				length -= dname.length + 2;
				request.addName(dname);
			}
		}
		return request;
	}

	static void encode(CertificateRequestV2 request, DataBuffer buffer) throws IOException {
		// ClientCertificateType certificate_types<1..2^8-1>;
		buffer.writeByte(request.typeSize());
		buffer.write(request.getTypes());

		// SignatureAndHashAlgorithm supported_signature_algorithms<2^16-1>;
		buffer.writeShort(request.algorithmSize() * 2);
		int n;
		for (n = 0; n < request.algorithmSize(); n++) {
			buffer.writeShort(request.getAlgorithm(n));
		}

		// DistinguishedName certificate_authorities<3..2^16-1>;
		int length = 0;
		for (n = 0; n < request.nameSize(); n++) {
			length += request.getName(n).length;
			length += 2;
		}
		buffer.writeShort(length);
		for (n = 0; n < request.nameSize(); n++) {
			// opaque DistinguishedName<1..2^16-1>;
			buffer.writeShort(request.getName(n).length);
			buffer.write(request.getName(n));
		}
	}

	static CertificateRequestV2 decodeCertificateRequestV2(DataBuffer buffer) throws IOException {
		final CertificateRequestV2 request = new CertificateRequestV2();

		// ClientCertificateType certificate_types<1..2^8-1>;
		request.setTypes(new byte[buffer.readUnsignedByte()]);
		buffer.readFully(request.getTypes());

		// SignatureAndHashAlgorithm supported_signature_algorithms<2^16-1>;
		int length = buffer.readUnsignedShort();
		if (length > 0) {
			length = length / 2;
			request.setAlgorithms(new short[length]);
			for (int a = 0; a < length; a++) {
				request.getAlgorithms()[a] = buffer.readShort();
			}
		}

		// DistinguishedName certificate_authorities<3..2^16-1>;
		length = buffer.readUnsignedShort();
		if (length > 0) {
			byte[] dname;
			while (length > 0) {
				// opaque DistinguishedName<1..2^16-1>;
				buffer.readFully(dname = new byte[buffer.readUnsignedShort()]);
				length -= dname.length + 2;
				request.addName(dname);
			}
		}
		return request;
	}

	static void encode(CertificateRequestV3 message, DataBuffer buffer) throws IOException {
		// opaque certificate_request_context<0..2^8-1>;
		buffer.writeShort(message.getContext().length);
		buffer.write(message.getContext());
	}

	static CertificateRequestV3 decodeCertificateRequestV3(DataBuffer buffer) throws IOException {
		final CertificateRequestV3 message = new CertificateRequestV3();
		// opaque certificate_request_context<0..2^8-1>;
		message.setContext(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.getContext());
		return message;
	}

	static void encode(CertificateVerifyV0 verify, DataBuffer buffer) throws IOException {
		// Signature digitally-signed
		// opaque md5_hash[16];
		if (verify.getSignedMD5().length > 0) {
			buffer.write(verify.getSignedMD5());
		}
		// opaque sha_hash[20];
		if (verify.getSignedSHA().length > 0) {
			buffer.write(verify.getSignedSHA());
		}
	}

	static CertificateVerifyV0 decodeCertificateVerifyV0(DataBuffer buffer, int length) throws IOException {
		final CertificateVerifyV0 verify = new CertificateVerifyV0();
		// Signature digitally-signed
		// opaque md5_hash[16];
		// opaque sha_hash[20];
		if (length == 20) {
			verify.setSignedSHA(new byte[20]);
			buffer.readFully(verify.getSignedSHA());
		} else if (length == 36) {
			verify.setSignedMD5(new byte[16]);
			buffer.readFully(verify.getSignedMD5());

			verify.setSignedSHA(new byte[20]);
			buffer.readFully(verify.getSignedSHA());
		} else {
			buffer.skipBytes(length);
		}
		return verify;
	}

	static void encode(CertificateVerifyV2 verify, DataBuffer buffer) throws IOException {
		// opaque signature<0..2^16-1>;
		buffer.writeShort(verify.getSignature().length);
		buffer.write(verify.getSignature());
	}

	static CertificateVerifyV2 decodeCertificateVerifyV2(DataBuffer buffer) throws IOException {
		final CertificateVerifyV2 verify = new CertificateVerifyV2();
		// opaque signature<0..2^16-1>;
		verify.setSignature(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(verify.getSignature());
		return verify;
	}

	static void encode(CertificateVerifyV3 message, DataBuffer buffer) throws IOException {
		// SignatureScheme algorithm;
		buffer.writeShort(message.getAlgorithm());
		// opaque signature<0..2^16-1>;
		buffer.writeShort(message.getSignature().length);
		buffer.write(message.getSignature());
	}

	static CertificateVerifyV3 decodeCertificateVerifyV3(DataBuffer buffer) throws IOException {
		final CertificateVerifyV3 message = new CertificateVerifyV3();
		// SignatureScheme algorithm;
		message.setAlgorithm(buffer.readShort());
		// opaque signature<0..2^16-1>;
		message.setSignature(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.getSignature());
		return message;
	}

	static void encode(NewSessionTicket1 message, DataBuffer buffer) throws IOException {
		// uint32 ticket_lifetime;
		buffer.writeInt(message.getLifetime());
		// opaque ticket<1..2^16-1>;
		buffer.writeShort(message.getTicket().length);
		buffer.write(message.getTicket());
	}

	static NewSessionTicket1 decodeNewSessionTicket1(DataBuffer buffer) throws IOException {
		final NewSessionTicket1 message = new NewSessionTicket1();
		// uint32 ticket_lifetime;
		message.setLifetime(buffer.readInt());
		// opaque ticket<1..2^16-1>;
		message.setTicket(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.getTicket());
		return message;
	}

	static void encode(NewSessionTicket2 message, DataBuffer buffer) throws IOException {
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

	static NewSessionTicket2 decodeNewSessionTicket2(DataBuffer buffer) throws IOException {
		final NewSessionTicket2 message = new NewSessionTicket2();
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

	static void encode(ServerKeyExchangeV0 message, DataBuffer buffer) throws IOException {
		if (message instanceof ServerKeyExchangeV0.ServerDHParams) {
			encode((ServerKeyExchangeV0.ServerDHParams) message, buffer);
		} else if (message instanceof ServerKeyExchangeV0.ServerRSAParams) {
			encode((ServerKeyExchangeV0.ServerRSAParams) message, buffer);
		} else {
			// EMPTY
		}
	}

	static void encode(ServerKeyExchangeV0.ServerDHParams message, DataBuffer buffer) throws IOException {
		// opaque dh_p<1..2^16-1>;
		buffer.writeShort(message.getP().length);
		buffer.write(message.getP());
		// opaque dh_g<1..2^16-1>;
		buffer.writeShort(message.getG().length);
		buffer.write(message.getG());
		// opaque dh_Ys<1..2^16-1>;
		buffer.writeShort(message.getYs().length);
		buffer.write(message.getYs());

		// digitally-signed
		// opaque md5_hash[16];
		if (message.getSignedMD5().length > 0) {
			buffer.write(message.getSignedMD5());
		}
		// opaque sha_hash[20];
		if (message.getSignedSHA().length > 0) {
			buffer.write(message.getSignedSHA());
		}
	}

	static void encode(ServerKeyExchangeV0.ServerRSAParams message, DataBuffer buffer) throws IOException {
		// opaque rsa_modulus<1..2^16-1>;
		buffer.writeShort(message.getModulus().length);
		buffer.write(message.getModulus());
		// opaque rsa_exponent<1..2^16-1>;
		buffer.writeShort(message.getExponent().length);
		buffer.write(message.getExponent());

		// digitally-signed
		// opaque md5_hash[16];
		if (message.getSignedMD5().length > 0) {
			buffer.write(message.getSignedMD5());
		}
		// opaque sha_hash[20];
		if (message.getSignedSHA().length > 0) {
			buffer.write(message.getSignedSHA());
		}
	}

	static ServerKeyExchangeV0 decodeServerKeyExchangeV0(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			final ServerKeyExchangeV0.ServerParams params = new ServerKeyExchangeV0.ServerParams();
			params.setParams(new byte[length]);
			buffer.readFully(params.getParams());
			return params;
		} else {
			return ServerKeyExchangeV0.EMPTY;
		}
	}

	static ServerKeyExchangeV0 decodeServerDHParams(DataBuffer buffer, int length) throws IOException {
		final ServerKeyExchangeV0.ServerDHParams params = new ServerKeyExchangeV0.ServerDHParams();
		if (length > 0) {
			params.setP(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getP());
			params.setG(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getG());
			params.setYs(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getYs());
			length -= params.paramsLength() + 6;

			// digitally-signed
			// opaque md5_hash[16];
			// opaque sha_hash[20];
			if (length == 20) {
				params.setSignedSHA(new byte[20]);
				buffer.readFully(params.getSignedSHA());
			} else if (length == 36) {
				params.setSignedMD5(new byte[16]);
				buffer.readFully(params.getSignedMD5());

				params.setSignedSHA(new byte[20]);
				buffer.readFully(params.getSignedSHA());
			} else {
				buffer.skipBytes(length);
			}
		}
		return params;
	}

	static ServerKeyExchangeV0 decodeServerRSAParams(DataBuffer buffer, int length) throws IOException {
		final ServerKeyExchangeV0.ServerRSAParams params = new ServerKeyExchangeV0.ServerRSAParams();
		if (length > 0) {
			params.setModulus(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getModulus());
			params.setExponent(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getExponent());
			length -= params.paramsLength() + 4;

			// digitally-signed
			// opaque md5_hash[16];
			// opaque sha_hash[20];
			if (length == 20) {
				params.setSignedSHA(new byte[20]);
				buffer.readFully(params.getSignedSHA());
			} else if (length == 36) {
				params.setSignedMD5(new byte[16]);
				buffer.readFully(params.getSignedMD5());

				params.setSignedSHA(new byte[20]);
				buffer.readFully(params.getSignedSHA());
			} else {
				buffer.skipBytes(length);
			}
		}
		return params;
	}

	static void encode(ServerKeyExchangeV2 message, DataBuffer buffer) throws IOException {
		if (message != ServerKeyExchangeV2.EMPTY) {
			encode((ServerKeyExchangeV2.ServerDHParams) message, buffer);
		} else {
			// EMPTY
		}
	}

	static void encode(ServerKeyExchangeV2.ServerDHParams params, DataBuffer buffer) throws IOException {
		// opaque dh_p<1..2^16-1>;
		buffer.writeShort(params.getP().length);
		buffer.write(params.getP());
		// opaque dh_g<1..2^16-1>;
		buffer.writeShort(params.getG().length);
		buffer.write(params.getG());
		// opaque dh_Ys<1..2^16-1>;
		buffer.writeShort(params.getYs().length);
		buffer.write(params.getYs());

		// signed_params
		if (params.getClientRandom().length > 0) {
			buffer.write(params.getClientRandom());
		}
		if (params.getServerRandom().length > 0) {
			buffer.write(params.getServerRandom());
		}
	}

	static ServerKeyExchangeV2 decodeServerKeyExchangeV2(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			final ServerKeyExchangeV2.ServerDHParams params = new ServerKeyExchangeV2.ServerDHParams();
			params.setP(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getP());
			params.setG(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getG());
			params.setYs(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(params.getYs());

			length -= params.paramsLength() + 6;
			if (length > 0) {
				params.setClientRandom(new byte[32]);
				buffer.readFully(params.getClientRandom());

				params.setServerRandom(new byte[32]);
				buffer.readFully(params.getServerRandom());
			}
			return params;
		} else {
			return ServerKeyExchangeV2.EMPTY;
		}
	}

	static void encode(ClientKeyExchange message, DataBuffer buffer) throws IOException {
		buffer.writeShort(message.get().length);
		buffer.write(message.get());
	}

	static ClientKeyExchange decodeClientKeyExchange(DataBuffer buffer) throws IOException {
		final ClientKeyExchange message = new ClientKeyExchange();
		message.set(new byte[buffer.readUnsignedShort()]);
		buffer.readFully(message.get());
		return message;
	}

	static void encode(Finished message, DataBuffer buffer) throws IOException {
		// buffer.writeShort(message.getVerifyData().length);
		buffer.write(message.getVerifyData());
	}

	static Finished decodeFinished(DataBuffer buffer, int length) throws IOException {
		final Finished message = new Finished();
		if (length > 0) {
			message.setVerifyData(new byte[length]);
			buffer.readFully(message.getVerifyData());
		}
		return message;
	}

	static void encode(CertificateURL message, DataBuffer buffer) throws IOException {
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

	static CertificateURL decodeCertificateURL(DataBuffer buffer) throws IOException {
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

	static void encode(CertificateStatus message, DataBuffer buffer) throws IOException {
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

	static CertificateStatus decodeCertificateStatus(DataBuffer buffer) throws IOException {
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

	static void encode(KeyUpdate message, DataBuffer buffer) throws IOException {
		buffer.writeByte(message.get());
	}

	static KeyUpdate decodeKeyUpdate(DataBuffer buffer) throws IOException {
		return new KeyUpdate(buffer.readByte());
	}
}