package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.tls.Certificate.CertificateEntry;
import com.joyzl.network.tls.CertificateStatusRequest.OCSPResponse;
import com.joyzl.network.tls.CertificateStatusRequest.OCSPStatusRequest;
import com.joyzl.network.tls.CertificateStatusRequestListV2.CertificateStatusRequestItemV2;
import com.joyzl.network.tls.CertificateType.ClientCertificateType;
import com.joyzl.network.tls.CertificateType.ServerCertificateType;
import com.joyzl.network.tls.CertificateTypes.ClientCertificateTypes;
import com.joyzl.network.tls.CertificateTypes.ServerCertificateTypes;
import com.joyzl.network.tls.KeyShare.KeyShareEntry;
import com.joyzl.network.tls.PreSharedKey.PskIdentity;
import com.joyzl.network.tls.TrustedAuthorities.TrustedAuthority;

/**
 * 扩展字段编解码
 * 
 * <pre>
 * RFC 7685 A Transport Layer Security (TLS) ClientHello Padding Extension
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class ExtensionCoder {

	static void encode(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		int p1, p2, length;

		// Length 2Byte
		p1 = buffer.readable();
		buffer.writeShort(0);

		// SUB Extensions
		Extension extension;
		for (int index = 0; index < extensions.extensionSize(); index++) {
			extension = extensions.getExtension(index);

			// Type 2Byte
			buffer.writeShort(extension.type());

			// opaque extension_data<0..2^16-1>;
			p2 = buffer.readable();
			buffer.writeShort(0);

			// SUB Extensions
			if (extension.type() == Extension.SERVER_NAME) {
				encode((ServerNames) extension, buffer);
			} else if (extension.type() == Extension.MAX_FRAGMENT_LENGTH) {
				encode((MaxFragmentLength) extension, buffer);
			} else if (extension.type() == Extension.CLIENT_CERTIFICATE_URL) {
				// EMPTY
			} else if (extension.type() == Extension.TRUSTED_CA_KEYS) {
				encode((TrustedAuthorities) extension, buffer);
			} else if (extension.type() == Extension.TRUNCATED_HMAC) {
				// EMPTY
			} else if (extension.type() == Extension.STATUS_REQUEST) {
				encode((OCSPStatusRequest) extension, buffer);
			} else if (extension.type() == Extension.STATUS_REQUEST_V2) {
				encode((CertificateStatusRequestListV2) extension, buffer);
			} else if (extension.type() == Extension.SUPPORTED_GROUPS) {
				encode((SupportedGroups) extension, buffer);
			} else if (extension.type() == Extension.EC_POINT_FORMATS) {
				encode((ECPointFormats) extension, buffer);
			} else if (extension.type() == Extension.SIGNATURE_ALGORITHMS) {
				encode((SignatureAlgorithms) extension, buffer);
			} else if (extension.type() == Extension.USE_SRTP) {
				encode((UseSRTP) extension, buffer);
			} else if (extension.type() == Extension.HEARTBEAT) {
				encode((Heartbeat) extension, buffer);
			} else if (extension.type() == Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION) {
				encode((ApplicationLayerProtocolNegotiation) extension, buffer);
			} else if (extension.type() == Extension.SIGNED_CERTIFICATE_TIMESTAMP) {
				encode((SignedCertificateTimestamp) extension, buffer);
			} else if (extension.type() == Extension.CLIENT_CERTIFICATE_TYPE) {
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((CertificateTypes) extension, buffer);
				} else//
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					encode((CertificateType) extension, buffer);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (extension.type() == Extension.SERVER_CERTIFICATE_TYPE) {
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((CertificateTypes) extension, buffer);
				} else//
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					encode((CertificateType) extension, buffer);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (extension.type() == Extension.EXTENDED_MASTER_SECRET) {
				// EMPTY
			} else if (extension.type() == Extension.COMPRESS_CERTIFICATE) {
				encode((CompressCertificate) extension, buffer);
			} else if (extension.type() == Extension.SESSION_TICKET) {
				encode((SessionTicket) extension, buffer);
			} else if (extension.type() == Extension.PRE_SHARED_KEY) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					encode((PreSharedKeySelected) extension, buffer);
				} else //
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((PreSharedKeys) extension, buffer);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (extension.type() == Extension.EARLY_DATA) {
				encode((EarlyDataIndication) extension, buffer);
			} else if (extension.type() == Extension.SUPPORTED_VERSIONS) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					encode((SelectedVersion) extension, buffer);
				} else //
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((SupportedVersions) extension, buffer);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (extension.type() == Extension.COOKIE) {
				encode((Cookie) extension, buffer);
			} else if (extension.type() == Extension.PSK_KEY_EXCHANGE_MODES) {
				encode((PskKeyExchangeModes) extension, buffer);
			} else if (extension.type() == Extension.CERTIFICATE_AUTHORITIES) {
				encode((CertificateAuthorities) extension, buffer);
			} else if (extension.type() == Extension.OID_FILTERS) {
				encode((OIDFilters) extension, buffer);
			} else if (extension.type() == Extension.POST_HANDSHAKE_AUTH) {
				// EMPTY
			} else if (extension.type() == Extension.SIGNATURE_ALGORITHMS_CERT) {
				encode((SignatureAlgorithmsCert) extension, buffer);
			} else if (extension.type() == Extension.KEY_SHARE) {
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((KeyShareClientHello) extension, buffer);
				} else//
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					if (extensions.isHelloRetryRequest()) {
						encode((KeyShareHelloRetryRequest) extension, buffer);
					} else {
						encode((KeyShareServerHello) extension, buffer);
					}
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (extension.type() == Extension.RENEGOTIATION_INFO) {
				encode((RenegotiationInfo) extension, buffer);
			} else if (extension.type() == Extension.APPLICATION_SETTINGS) {
				encode((ApplicationSettings) extension, buffer);
			} else if (extension.type() == Extension.ENCRYPTED_CLIENT_HELLO) {
				encode((EncryptedClientHello) extension, buffer);
			} else {
				throw new IOException("TLS:混乱的扩展");
			}
			// SET Length
			length = buffer.readable() - p2 - 2;
			buffer.set(p2++, (byte) (length >>> 8));
			buffer.set(p2, (byte) length);
		}
		if (extensions.msgType() == Handshake.CLIENT_HELLO) {
			// RFC7685
			// AUTO Padding ClientHello >= 512
			if (buffer.readable() < 512) {
				length = 512 - buffer.readable();
				if (length < 4) {
					// Padding type + length = 4 Byte
					length = 0;
				}
				// Type 2Byte
				buffer.writeShort(Extension.PADDING);
				// opaque extension_data<0..2^16-1>;
				buffer.writeShort(length);
				while (length-- > 0) {
					buffer.writeByte(0);
				}
			}
		}
		// SET Length
		length = buffer.readable() - p1 - 2;
		buffer.set(p1++, (byte) (length >>> 8));
		buffer.set(p1, (byte) length);
	}

	static void decode(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int length = buffer.readUnsignedShort();
		// 计算扩展字段全部解码后的剩余字节
		length = buffer.readable() - length;

		int size;
		short type;
		Extension extension;
		while (buffer.readable() > length) {
			// Type 2Byte
			type = buffer.readShort();
			// opaque extension_data<0..2^16-1>;
			size = buffer.readUnsignedShort();
			// SUB Extension
			if (type == Extension.SERVER_NAME) {
				extension = decodeServerNames(buffer, size);
			} else if (type == Extension.MAX_FRAGMENT_LENGTH) {
				extension = decodeMaxFragmentLength(buffer, size);
			} else if (type == Extension.CLIENT_CERTIFICATE_URL) {
				extension = ClientCertificateURL.INSTANCE;
			} else if (type == Extension.TRUSTED_CA_KEYS) {
				extension = decodeTrustedCAKeys(buffer, size);
			} else if (type == Extension.TRUNCATED_HMAC) {
				extension = TruncatedHMAC.INSTANCE;
			} else if (type == Extension.STATUS_REQUEST) {
				extension = decodeOCSPStatusRequest(buffer, size);
			} else if (type == Extension.STATUS_REQUEST_V2) {
				extension = decodeCertificateStatusRequestListV2(buffer, size);
			} else if (type == Extension.SUPPORTED_GROUPS) {
				extension = decodeSupportedGroups(buffer, size);
			} else if (type == Extension.EC_POINT_FORMATS) {
				extension = decodeECPointFormats(buffer, size);
			} else if (type == Extension.SIGNATURE_ALGORITHMS) {
				extension = decodeSignatureAlgorithms(buffer, size);
			} else if (type == Extension.USE_SRTP) {
				extension = decodeUseSRTP(buffer, size);
			} else if (type == Extension.HEARTBEAT) {
				extension = decodeHeartbeat(buffer, size);
			} else if (type == Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION) {
				extension = decodeApplicationLayerProtocolNegotiation(buffer, size);
			} else if (type == Extension.SIGNED_CERTIFICATE_TIMESTAMP) {
				extension = decodeSignedCertificateTimestamp(buffer, size);
			} else if (type == Extension.CLIENT_CERTIFICATE_TYPE) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					extension = decode(new ClientCertificateTypes(), buffer, size);
				} else //
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					extension = decode(new ClientCertificateType(), buffer, size);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (type == Extension.SERVER_CERTIFICATE_TYPE) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					extension = decode(new ServerCertificateTypes(), buffer, size);
				} else //
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					extension = decode(new ServerCertificateType(), buffer, size);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (type == Extension.EXTENDED_MASTER_SECRET) {
				extension = ExtendedMasterSecret.INSTANCE;
			} else if (type == Extension.COMPRESS_CERTIFICATE) {
				extension = decodeCompressCertificate(buffer, size);
			} else if (type == Extension.SESSION_TICKET) {
				extension = decodeSessionTicket(buffer, size);
			} else if (type == Extension.PRE_SHARED_KEY) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					extension = decodePreSharedKeySelected(buffer, size);
				} else //
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					extension = decodeOfferedPsks(buffer, size);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (type == Extension.EARLY_DATA) {
				extension = decodeEarlyDataIndication(buffer, size);
			} else if (type == Extension.SUPPORTED_VERSIONS) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					extension = decodeSelectedVersion(buffer, size);
				} else //
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					extension = decodeSupportedVersions(buffer, size);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (type == Extension.COOKIE) {
				extension = decodeCookie(buffer, size);
			} else if (type == Extension.PSK_KEY_EXCHANGE_MODES) {
				extension = decodePskKeyExchangeModes(buffer, size);
			} else if (type == Extension.CERTIFICATE_AUTHORITIES) {
				extension = decodeCertificateAuthorities(buffer, size);
			} else if (type == Extension.OID_FILTERS) {
				extension = decodeOIDFilters(buffer, size);
			} else if (type == Extension.POST_HANDSHAKE_AUTH) {
				extension = PostHandshakeAuth.INSTANCE;
			} else if (type == Extension.SIGNATURE_ALGORITHMS_CERT) {
				extension = decodeSignatureAlgorithmsCert(buffer, size);
			} else if (type == Extension.KEY_SHARE) {
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					extension = decodeKeyShareClientHello(buffer, size);
				} else //
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					if (extensions.isHelloRetryRequest()) {
						extension = decodeKeyShareHelloRetryRequest(buffer, size);
					} else {
						extension = decodeKeyShareServerHello(buffer, size);
					}
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (type == Extension.RENEGOTIATION_INFO) {
				extension = decodeRenegotiationInfo(buffer, size);
			} else if (type == Extension.APPLICATION_SETTINGS) {
				extension = decodeApplicationSettings(buffer, size);
			} else if (type == Extension.ENCRYPTED_CLIENT_HELLO) {
				extension = decodeEncryptedClientHello(buffer, size);
			} else {
				buffer.skipBytes(size);
				continue;
			}
			extensions.addExtension(extension);
		}
	}

	static void encode(CertificateEntry extensions, DataBuffer buffer) throws IOException {
		int p1, p2, length;

		// Length 2Byte
		p1 = buffer.readable();
		buffer.writeShort(0);

		// SUB Extensions
		Extension extension;
		for (int index = 0; index < extensions.extensionSize(); index++) {
			extension = extensions.getExtension(index);

			// Type 2Byte
			buffer.writeShort(extension.type());

			// opaque extension_data<0..2^16-1>;
			p2 = buffer.readable();
			buffer.writeShort(0);

			// SUB Extensions
			if (extension.type() == Extension.CLIENT_CERTIFICATE_URL) {
				// EMPTY
			} else if (extension.type() == Extension.TRUSTED_CA_KEYS) {
				encode((TrustedAuthorities) extension, buffer);
			} else if (extension.type() == Extension.STATUS_REQUEST) {
				encode((OCSPResponse) extension, buffer);
			} else if (extension.type() == Extension.STATUS_REQUEST_V2) {
				encode((CertificateStatusRequestListV2) extension, buffer);
			} else if (extension.type() == Extension.SIGNED_CERTIFICATE_TIMESTAMP) {
				encode((SignedCertificateTimestamp) extension, buffer);
			} else if (extension.type() == Extension.COMPRESS_CERTIFICATE) {
				encode((CompressCertificate) extension, buffer);
			} else if (extension.type() == Extension.CERTIFICATE_AUTHORITIES) {
				encode((CertificateAuthorities) extension, buffer);
			} else if (extension.type() == Extension.SIGNATURE_ALGORITHMS_CERT) {
				encode((SignatureAlgorithmsCert) extension, buffer);
			} else {
				throw new IOException("TLS:混乱的扩展");
			}
			// SET Length
			length = buffer.readable() - p2 - 2;
			buffer.set(p2++, (byte) (length >>> 8));
			buffer.set(p2, (byte) length);
		}

		// SET Length
		length = buffer.readable() - p1 - 2;
		buffer.set(p1++, (byte) (length >>> 8));
		buffer.set(p1, (byte) length);
	}

	static void decode(CertificateEntry extensions, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int length = buffer.readUnsignedShort();
		// 计算扩展字段全部解码后的剩余字节
		length = buffer.readable() - length;

		int size;
		short type;
		Extension extension;
		while (buffer.readable() > length) {
			// Type 2Byte
			type = buffer.readShort();
			// opaque extension_data<0..2^16-1>;
			size = buffer.readUnsignedShort();
			// SUB Extension
			if (type == Extension.CLIENT_CERTIFICATE_URL) {
				extension = ClientCertificateURL.INSTANCE;
			} else if (type == Extension.TRUSTED_CA_KEYS) {
				extension = decodeTrustedCAKeys(buffer, size);
			} else if (type == Extension.STATUS_REQUEST) {
				extension = decodeOCSPResponse(buffer, size);
			} else if (type == Extension.STATUS_REQUEST_V2) {
				extension = decodeCertificateStatusRequestListV2(buffer, size);
			} else if (type == Extension.SIGNATURE_ALGORITHMS) {
				extension = decodeSignatureAlgorithms(buffer, size);
			} else if (type == Extension.SIGNED_CERTIFICATE_TIMESTAMP) {
				extension = decodeSignedCertificateTimestamp(buffer, size);
			} else if (type == Extension.COMPRESS_CERTIFICATE) {
				extension = decodeCompressCertificate(buffer, size);
			} else if (type == Extension.CERTIFICATE_AUTHORITIES) {
				extension = decodeCertificateAuthorities(buffer, size);
			} else if (type == Extension.OID_FILTERS) {
				extension = decodeOIDFilters(buffer, size);
			} else if (type == Extension.SIGNATURE_ALGORITHMS_CERT) {
				extension = decodeSignatureAlgorithmsCert(buffer, size);
			} else {
				buffer.skipBytes(size);
				continue;
			}
			extensions.addExtension(extension);
		}
	}

	/** RFC 6066 */
	static void encode(ServerNames serverNames, DataBuffer buffer) throws IOException {
		// ServerName server_name_list<1..2^16-1>
		int position = buffer.readable();
		// Length 2Byte
		buffer.writeShort(0);

		ServerName name;
		for (int n = 0; n < serverNames.size(); n++) {
			name = serverNames.get(n);
			// name_type
			buffer.writeByte(name.getType());
			// opaque HostName<1..2^16-1>;
			buffer.writeShort(name.getName().length);
			buffer.write(name.getName());
		}

		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 6066 */
	static ServerNames decodeServerNames(DataBuffer buffer, int length) throws IOException {
		final ServerNames serverNames = new ServerNames();
		if (length > 0) {
			// ServerName server_name_list<1..2^16-1>
			length = buffer.readUnsignedShort();

			ServerName name;
			while (length > 0) {
				name = new ServerName();
				// name_type
				name.setType(buffer.readByte());
				// opaque HostName<1..2^16-1>;
				name.setName(new byte[buffer.readUnsignedShort()]);
				buffer.readFully(name.getName());

				length -= name.getName().length + 3;
				serverNames.add(name);
			}
		}
		return serverNames;
	}

	/** RFC 6066 */
	static void encode(MaxFragmentLength extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getType());
	}

	/** RFC 6066 */
	static MaxFragmentLength decodeMaxFragmentLength(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			return new MaxFragmentLength(buffer.readByte());
		} else {
			return new MaxFragmentLength((byte) 0);
		}
	}

	/** RFC 6066 */
	static void encode(TrustedAuthorities extension, DataBuffer buffer) throws IOException {
		// TrustedAuthority trusted_authorities_list<0..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);

		TrustedAuthority a;
		for (int index = 0; index < extension.size(); index++) {
			a = extension.get(index);
			// IdentifierType identifier_type;
			buffer.writeByte(a.getType());
			if (a.getType() == TrustedAuthorities.PRE_AGREED) {
				// EMPTY
			} else if (a.getType() == TrustedAuthorities.KEY_SHA1_HASH) {
				// SHA1Hash[20]
				// TODO LENGTH?
				buffer.write(a.getData());
			} else if (a.getType() == TrustedAuthorities.X509_NAME) {
				// opaque DistinguishedName<1..2^16-1>;
				buffer.writeShort(a.getData().length);
				buffer.write(a.getData());
			} else if (a.getType() == TrustedAuthorities.CERT_SHA1_HASH) {
				// SHA1Hash[20]
				// TODO LENGTH?
				buffer.write(a.getData());
			}
		}

		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 6066 */
	static TrustedAuthorities decodeTrustedCAKeys(DataBuffer buffer, int length) throws IOException {
		final TrustedAuthorities tas = new TrustedAuthorities();
		if (length > 0) {
			// TrustedAuthority trusted_authorities_list<0..2^16-1>;
			length = buffer.readUnsignedShort();

			TrustedAuthority a;
			while (length > 0) {
				a = new TrustedAuthority();
				// IdentifierType identifier_type;
				a.setType(buffer.readByte());
				if (a.getType() == TrustedAuthorities.PRE_AGREED) {
					// EMPTY
				} else if (a.getType() == TrustedAuthorities.KEY_SHA1_HASH) {
					// SHA1Hash[20]
					// TODO LENGTH?
					a.setData(new byte[20]);
					buffer.readFully(a.getData());
				} else if (a.getType() == TrustedAuthorities.X509_NAME) {
					// opaque DistinguishedName<1..2^16-1>;
					a.setData(new byte[buffer.readUnsignedShort()]);
					buffer.readFully(a.getData());
					length -= 2;
				} else if (a.getType() == TrustedAuthorities.CERT_SHA1_HASH) {
					// SHA1Hash[20]
					// TODO LENGTH?
					a.setData(new byte[20]);
					buffer.readFully(a.getData());
				}
				length -= a.getData().length + 1;
				tas.add(a);
			}
		}
		return tas;
	}

	/** RFC 6066 */
	static void encode(OCSPStatusRequest request, DataBuffer buffer) throws IOException {
		// CertificateStatusType status_type;
		buffer.writeByte(request.getStatusType());
		if (request.getStatusType() == CertificateStatusRequest.OCSP) {
			final OCSPStatusRequest oscp = (OCSPStatusRequest) request;
			// ResponderID responder_id_list<0..2^16-1>;
			int position = buffer.readable();
			buffer.writeShort(0);

			for (int i = 0; i < oscp.size(); i++) {
				// opaque ResponderID<1..2^16-1>;
				buffer.writeShort(oscp.getResponderID(i).length);
				buffer.write(oscp.getResponderID(i));
			}

			// SET Length
			int length = buffer.readable() - position - 2;
			buffer.set(position++, (byte) (length >>> 8));
			buffer.set(position, (byte) length);

			// Extensions request_extensions;
			// opaque Extensions<0..2^16-1>;
			buffer.writeShort(oscp.getRequestExtensions().length);
			buffer.write(oscp.getRequestExtensions());
		} else {
			// 不支持的请求类型
			throw new UnsupportedOperationException(request.getClass().getName());
		}
	}

	/** RFC 6066 */
	static OCSPStatusRequest decodeOCSPStatusRequest(DataBuffer buffer, int length) throws IOException {
		final OCSPStatusRequest oscp = new OCSPStatusRequest();
		if (length > 0) {
			// CertificateStatusType status_type;
			oscp.setStatusType(buffer.readByte());
			if (oscp.getStatusType() == CertificateStatusRequest.OCSP) {
				// ResponderID responder_id_list<0..2^16-1>;
				length = buffer.readUnsignedShort();
				byte[] opaque;
				while (length > 0) {
					// opaque ResponderID<1..2^16-1>;
					buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
					oscp.addResponderID(opaque);
					length -= opaque.length + 2;
				}

				// opaque Extensions<0..2^16-1>;
				buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
				oscp.setRequestExtensions(opaque);
			} else {
				buffer.skipBytes(length - 1);
			}
		}
		return oscp;
	}

	/** RFC 6066 特殊情况1.3结构为 CertificateStatus */
	static void encode(OCSPResponse response, DataBuffer buffer) throws IOException {
		// CertificateStatusType status_type;
		buffer.writeByte(response.getStatusType());

		// opaque OCSPResponse<1..2^24-1>;
		buffer.writeMedium(response.get().length);
		buffer.write(response.get());
	}

	/** RFC 6066 特殊情况1.3结构为 CertificateStatus */
	static OCSPResponse decodeOCSPResponse(DataBuffer buffer, int length) throws IOException {
		final OCSPResponse response = new OCSPResponse();
		if (length > 0) {
			// CertificateStatusType status_type;
			response.setStatusType(buffer.readByte());
			if (response.getStatusType() == CertificateStatusRequest.OCSP) {
				// opaque OCSPResponse<1..2^24-1>;
				response.set(new byte[buffer.readUnsignedMedium()]);
				buffer.readFully(response.get());
			}
		}
		return response;
	}

	/** RFC 6961 */
	static void encode(CertificateStatusRequestListV2 request, DataBuffer buffer) throws IOException {
		// certificate_status_req_list<1..2^16-1>;
		int p1 = buffer.readable();
		buffer.writeShort(0);

		CertificateStatusRequestItemV2 item;
		for (int i = 0; i < request.size(); i++) {
			item = request.get(i);

			// CertificateStatusType status_type;
			buffer.writeByte(item.getStatusType());

			// uint16 request_length;
			int p2 = buffer.readable();
			buffer.writeShort(0);
			{
				// ResponderID responder_id_list<0..2^16-1>;
				int p3 = buffer.readable();
				buffer.writeShort(0);

				for (int r = 0; r < item.size(); r++) {
					// opaque ResponderID<1..2^16-1>;
					buffer.writeShort(item.getResponderID(r).length);
					buffer.write(item.getResponderID(r));
				}

				// SET Length
				int length = buffer.readable() - p3 - 2;
				buffer.set(p3++, (byte) (length >>> 8));
				buffer.set(p3, (byte) length);

				// Extensions request_extensions;
				// opaque Extensions<0..2^16-1>;
				buffer.writeShort(item.getRequestExtensions().length);
				buffer.write(item.getRequestExtensions());
			}
			// SET Length
			int length = buffer.readable() - p2 - 2;
			buffer.set(p2++, (byte) (length >>> 8));
			buffer.set(p2, (byte) length);
		}

		// SET Length
		int length = buffer.readable() - p1 - 2;
		buffer.set(p1++, (byte) (length >>> 8));
		buffer.set(p1, (byte) length);
	}

	/** RFC 6961 */
	static CertificateStatusRequestListV2 decodeCertificateStatusRequestListV2(DataBuffer buffer, int length) throws IOException {
		final CertificateStatusRequestListV2 request = new CertificateStatusRequestListV2();
		if (length > 0) {
			// certificate_status_req_list<1..2^16-1>;
			length = buffer.readUnsignedShort();
			while (length > 0) {
				final CertificateStatusRequestItemV2 item = new CertificateStatusRequestItemV2();

				// CertificateStatusType status_type;
				item.setStatusType(buffer.readByte());

				// uint16 request_length;
				int length2 = buffer.readUnsignedShort();
				if (length2 > 0) {

					// ResponderID responder_id_list<0..2^16-1>;
					int length3 = buffer.readUnsignedShort();
					byte[] opaque;
					while (length3 > 0) {
						// opaque ResponderID<1..2^16-1>;
						buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
						item.addResponderID(opaque);
						length3 -= opaque.length + 2;
					}

					// opaque Extensions<0..2^16-1>;
					buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
					item.setRequestExtensions(opaque);
				}
			}
		}
		return request;
	}

	/** RFC 7919 */
	static void encode(SupportedGroups extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC 7919 */
	static SupportedGroups decodeSupportedGroups(DataBuffer buffer, int length) throws IOException {
		final SupportedGroups groups = new SupportedGroups();
		if (length > 0) {
			length = buffer.readShort() / 2;
			while (length-- > 0) {
				groups.add(buffer.readShort());
			}
		}
		return groups;
	}

	/** RFC 8422 */
	static void encode(ECPointFormats extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size());
		buffer.write(extension.get());
	}

	/** RFC 8442 */
	static ECPointFormats decodeECPointFormats(DataBuffer buffer, int length) throws IOException {
		final ECPointFormats formats = new ECPointFormats();
		if (length > 0) {
			length = buffer.readUnsignedByte();
			formats.set(new byte[length]);
			buffer.readFully(formats.get());
		}
		return formats;
	}

	/** RFC 8446 */
	static void encode(SignatureAlgorithms extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC 8446 */
	static SignatureAlgorithms decodeSignatureAlgorithms(DataBuffer buffer, int length) throws IOException {
		final SignatureAlgorithms algorithms = new SignatureAlgorithms();
		if (length > 0) {
			length = buffer.readShort() / 2;
			while (length-- > 0) {
				algorithms.add(buffer.readShort());
			}
		}
		return algorithms;
	}

	/** RFC 5764 */
	static void encode(UseSRTP srtp, DataBuffer buffer) throws IOException {
		// SRTPProtectionProfiles<2..2^16-1>;
		// uint8 SRTPProtectionProfile[2];
		buffer.writeShort(srtp.size() * 2);
		for (int index = 0; index < srtp.size(); index++) {
			buffer.writeShort(srtp.get(index));
		}
		// opaque srtp_mki<0..255>;
		buffer.writeShort(srtp.getMKI().length);
		buffer.write(srtp.getMKI());
	}

	/** RFC 5764 */
	static UseSRTP decodeUseSRTP(DataBuffer buffer, int length) throws IOException {
		final UseSRTP srtp = new UseSRTP();
		if (length > 0) {
			// SRTPProtectionProfiles SRTPProtectionProfiles;
			length = buffer.readUnsignedShort() / 2;
			while (length-- > 0) {
				srtp.add(buffer.readShort());
			}
			// opaque srtp_mki<0..255>;
			srtp.setMKI(new byte[buffer.readUnsignedByte()]);
			buffer.readFully(srtp.getMKI());
		}
		return srtp;
	}

	/** RFC 6520 */
	static void encode(Heartbeat heartbeat, DataBuffer buffer) throws IOException {
		buffer.writeByte(heartbeat.getMode());
	}

	/** RFC 6520 */
	static Heartbeat decodeHeartbeat(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			return new Heartbeat(buffer.readByte());
		} else {
			return new Heartbeat();
		}
	}

	/** RFC 7301 */
	static void encode(ApplicationLayerProtocolNegotiation alpn, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int position = buffer.readable();
		buffer.writeShort(0);
		for (int index = 0; index < alpn.size(); index++) {
			buffer.writeByte(alpn.get(index).length);
			buffer.write(alpn.get(index));
		}
		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 7301 */
	static ApplicationLayerProtocolNegotiation decodeApplicationLayerProtocolNegotiation(DataBuffer buffer, int length) throws IOException {
		final ApplicationLayerProtocolNegotiation alpn = new ApplicationLayerProtocolNegotiation();
		if (length > 0) {
			length = buffer.readShort();
			byte[] opaque;
			while (length > 0) {
				buffer.readFully(opaque = new byte[buffer.readByte()]);
				alpn.add(opaque);
				length -= opaque.length + 1;
			}
		}
		return alpn;
	}

	/** RFC 6962 */
	static void encode(SignedCertificateTimestamp timestamp, DataBuffer buffer) throws IOException {
		// SerializedSCT sct_list <1..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);

		for (int index = 0; index < timestamp.size(); index++) {
			// opaque SerializedSCT<1..2^16-1>;
			buffer.writeShort(timestamp.get(index).length);
			buffer.write(timestamp.get(index));
		}

		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 6962 */
	static SignedCertificateTimestamp decodeSignedCertificateTimestamp(DataBuffer buffer, int length) throws IOException {
		final SignedCertificateTimestamp timestamp = new SignedCertificateTimestamp();
		if (length > 0) {
			// SerializedSCT sct_list <1..2^16-1>;
			length = buffer.readUnsignedShort();
			byte[] opaque;
			while (length > 0) {
				// opaque SerializedSCT<1..2^16-1>;
				buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
				timestamp.add(opaque);
				length -= opaque.length + 2;
			}
		}
		return timestamp;
	}

	/** RFC 7250 */
	static void encode(CertificateTypes types, DataBuffer buffer) throws IOException {
		// server_certificate_types<1..2^8-1>;
		// client_certificate_types<1..2^8-1>;
		buffer.writeByte(types.size());
		for (int index = 0; index < types.size(); index++) {
			buffer.writeByte(types.get(index));
		}
	}

	/** RFC 7250 */
	static CertificateTypes decode(CertificateTypes types, DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			// server_certificate_types<1..2^8-1>;
			// client_certificate_types<1..2^8-1>;
			length = buffer.readUnsignedByte();
			while (length-- > 0) {
				types.add(buffer.readByte());
			}
		}
		return types;
	}

	/** RFC 7250 */
	static void encode(CertificateType type, DataBuffer buffer) throws IOException {
		buffer.writeByte(type.get());
	}

	/** RFC 7250 */
	static CertificateType decode(CertificateType type, DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			type.set(buffer.readByte());
		}
		return type;
	}

	/** RFC 8879 */
	static void encode(CompressCertificate extension, DataBuffer buffer) throws IOException {
		// algorithms<2..2^8-2>;
		buffer.writeByte(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC 8879 */
	static CompressCertificate decodeCompressCertificate(DataBuffer buffer, int length) throws IOException {
		final CompressCertificate algorithms = new CompressCertificate();
		if (length > 0) {
			// CertificateCompressionAlgorithm algorithms<2..2^8-2>;
			length = buffer.readByte() / 2;
			while (length-- > 0) {
				algorithms.add(buffer.readShort());
			}
		}
		return algorithms;
	}

	/** RFC 5077 */
	static void encode(SessionTicket extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getTicket().length);
		buffer.write(extension.getTicket());
	}

	/** RFC 5077 */
	static SessionTicket decodeSessionTicket(DataBuffer buffer, int length) throws IOException {
		final SessionTicket ticket = new SessionTicket();
		if (length > 0) {
			ticket.setTicket(new byte[length]);
			buffer.readFully(ticket.getTicket());
		}
		return ticket;
	}

	/** RFC 8446 */
	static void encode(KeyShareClientHello extension, DataBuffer buffer) throws IOException {
		// KeyShareEntry client_shares<0..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);

		KeyShareEntry item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.getGroup());
			buffer.writeShort(item.getKeyExchange().length);
			buffer.write(item.getKeyExchange());
		}

		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 8446 */
	static KeyShare decodeKeyShareClientHello(DataBuffer buffer, int length) throws IOException {
		final KeyShareClientHello share = new KeyShareClientHello();
		if (length > 0) {
			// client_shares<0..2^16-1>;
			length = buffer.readShort();

			KeyShareEntry entry;
			while (length > 0) {
				entry = new KeyShareEntry();
				// NamedGroup group;
				entry.setGroup(buffer.readShort());
				// opaque key_exchange<1..2^16-1>;
				entry.setKeyExchange(new byte[buffer.readShort()]);
				buffer.readFully(entry.getKeyExchange());

				length -= entry.getKeyExchange().length + 4;
				share.add(entry);
			}
		}
		return share;
	}

	/** RFC 8446 */
	static void encode(KeyShareServerHello extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getServerShare().getGroup());
		buffer.writeShort(extension.getServerShare().getKeyExchange().length);
		buffer.write(extension.getServerShare().getKeyExchange());
	}

	/** RFC 8446 */
	static KeyShare decodeKeyShareServerHello(DataBuffer buffer, int length) throws IOException {
		final KeyShareServerHello share = new KeyShareServerHello();
		if (length > 0) {
			final KeyShareEntry entry = new KeyShareEntry();
			entry.setGroup(buffer.readShort());
			entry.setKeyExchange(new byte[buffer.readShort()]);
			buffer.readFully(entry.getKeyExchange());
			share.setServerShare(entry);
		}
		return share;
	}

	/** RFC 8446 */
	static void encode(KeyShareHelloRetryRequest extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getSelectedGroup());
	}

	/** RFC 8446 */
	static KeyShare decodeKeyShareHelloRetryRequest(DataBuffer buffer, int length) throws IOException {
		final KeyShareHelloRetryRequest selected = new KeyShareHelloRetryRequest();
		if (length > 0) {
			selected.setSelectedGroup(buffer.readShort());
		}
		return selected;
	}

	/** RFC 8446 client_hello pre_shared_key */
	static void encode(PreSharedKeys extension, DataBuffer buffer) throws IOException {
		// PskIdentity identities<7..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);
		PskIdentity item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.getIdentity().length);
			buffer.write(item.getIdentity());
			buffer.writeInt(item.getTicketAge());
		}
		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);

		// PskBinderEntry binders<33..2^16-1>;
		// 此时还未设置BinderKey哈希值以零代替
		// 输出完整的binders列表确保握手消息长度正确
		buffer.writeShort(extension.bindersLength());
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.getHashLength());
			for (length = 0; length < extension.getHashLength(); length++) {
				buffer.writeByte(0);
			}
		}
	}

	/** RFC 8446 client_hello PskBinderEntry */
	static void encodeBinders(PreSharedKeys extension, DataBuffer buffer) throws IOException {
		// PskBinderEntry binders<33..2^16-1>;
		PskIdentity binder;
		buffer.writeShort(extension.bindersLength());
		for (int index = 0; index < extension.size(); index++) {
			binder = extension.get(index);
			buffer.writeByte(extension.getHashLength());
			buffer.write(binder.getBinder());
		}
	}

	/** RFC 8446 client_hello */
	static PreSharedKey decodeOfferedPsks(DataBuffer buffer, int length) throws IOException {
		final PreSharedKeys psks = new PreSharedKeys();
		if (length > 0) {
			// PskIdentity identities<7..2^16-1>;
			length = buffer.readUnsignedShort();
			PskIdentity identity;
			while (length > 0) {
				identity = new PskIdentity();
				identity.setIdentity(new byte[buffer.readUnsignedShort()]);
				buffer.readFully(identity.getIdentity());
				identity.setTicketAge(buffer.readInt());
				psks.add(identity);
				length -= identity.getIdentity().length + 6;
			}

			// PskBinderEntry binders<33..2^16-1>;
			int index = 0;
			length = buffer.readUnsignedShort();
			while (length > 0) {
				identity = psks.get(index++);
				psks.setHashLength(buffer.readUnsignedByte());
				identity.setBinder(new byte[psks.getHashLength()]);
				buffer.readFully(identity.getBinder());
				length -= identity.getBinder().length + 1;
			}
		}
		return psks;
	}

	/** RFC 8446 server_hello */
	static void encode(PreSharedKeySelected extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getSelected());
	}

	/** RFC 8446 server_hello */
	static PreSharedKey decodePreSharedKeySelected(DataBuffer buffer, int length) throws IOException {
		final PreSharedKeySelected selected = new PreSharedKeySelected();
		if (length > 0) {
			// uint16 selected_identity_index;
			selected.setSelected(buffer.readShort());
		}
		return selected;
	}

	/** RFC 8446 */
	static void encode(PskKeyExchangeModes extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size());
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index));
		}
	}

	/** RFC 8446 */
	static PskKeyExchangeModes decodePskKeyExchangeModes(DataBuffer buffer, int length) throws IOException {
		final PskKeyExchangeModes extension = new PskKeyExchangeModes();
		if (length > 0) {
			length = buffer.readByte();
			while (length-- > 0) {
				extension.add(buffer.readByte());
			}
		}
		return extension;
	}

	/** RFC 8446 */
	static void encode(SupportedVersions versions, DataBuffer buffer) throws IOException {
		buffer.writeByte(versions.size() * 2);
		for (int index = 0; index < versions.size(); index++) {
			buffer.writeShort(versions.get(index));
		}
	}

	/** RFC 8446 */
	static SupportedVersions decodeSupportedVersions(DataBuffer buffer, int length) throws IOException {
		final SupportedVersions versions = new SupportedVersions();
		if (length > 0) {
			length = buffer.readUnsignedByte() / 2;
			while (length-- > 0) {
				versions.add(buffer.readShort());
			}
		}
		return versions;
	}

	/** RFC 8446 */
	static void encode(SelectedVersion selected, DataBuffer buffer) throws IOException {
		buffer.writeShort(selected.get());
	}

	/** RFC 8446 */
	static SelectedVersion decodeSelectedVersion(DataBuffer buffer, int length) throws IOException {
		final SelectedVersion selected = new SelectedVersion();
		if (length > 0) {
			selected.set(buffer.readShort());
		}
		return selected;
	}

	/** RFC 8446 */
	static void encode(EarlyDataIndication earlyData, DataBuffer buffer) throws IOException {
		if (earlyData.getMaxSize() > 0) {
			buffer.writeInt(earlyData.getMaxSize());
		} else {
			// EMPTY
		}
	}

	/** RFC 8446 */
	static EarlyDataIndication decodeEarlyDataIndication(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			final EarlyDataIndication max = new EarlyDataIndication();
			max.setMaxSize(buffer.readInt());
			return max;
		} else {
			return EarlyDataIndication.EMPTY;
		}
	}

	/** RFC 8446 */
	static void encode(SignatureAlgorithmsCert algorithms, DataBuffer buffer) throws IOException {
		// supported_signature_algorithms<2..2^16-2>;
		buffer.writeShort(algorithms.size() * 2);
		for (int index = 0; index < algorithms.size(); index++) {
			buffer.writeShort(algorithms.get(index));
		}
	}

	/** RFC 8446 */
	static SignatureAlgorithmsCert decodeSignatureAlgorithmsCert(DataBuffer buffer, int length) throws IOException {
		final SignatureAlgorithmsCert algorithms = new SignatureAlgorithmsCert();
		if (length > 0) {
			// supported_signature_algorithms<2..2^16-2>;
			length = buffer.readUnsignedShort() / 2;
			while (length-- > 1) {
				algorithms.add(buffer.readShort());
			}
		}
		return algorithms;
	}

	/** RFC 8446 */
	static void encode(CertificateAuthorities cas, DataBuffer buffer) throws IOException {
		// DistinguishedName authorities<3..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);

		for (int c = 0; c < cas.size(); c++) {
			buffer.writeShort(cas.get(c).length);
			buffer.write(cas.get(c));
		}

		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 8446 */
	static CertificateAuthorities decodeCertificateAuthorities(DataBuffer buffer, int length) throws IOException {
		final CertificateAuthorities cas = new CertificateAuthorities();
		if (length > 0) {
			// DistinguishedName authorities<3..2^16-1>;
			length = buffer.readUnsignedShort();

			byte[] dname;
			while (length > 0) {
				// opaque DistinguishedName<1..2^16-1>;
				buffer.readFully(dname = new byte[buffer.readUnsignedShort()]);
				length -= dname.length + 2;
				cas.add(dname);
			}
		}
		return cas;
	}

	/** RFC 8446 */
	static void encode(OIDFilters filters, DataBuffer buffer) throws IOException {
		// OIDFilter filters<0..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);

		int p, length;
		OIDFilter filter;
		for (int i = 0; i < filters.size(); i++) {
			filter = filters.get(i);

			// opaque certificate_extension_oid<1..2^8-1>;
			buffer.writeByte(filter.getOID().length);
			buffer.write(filter.getOID());

			// opaque certificate_extension_values<0..2^16-1>;
			p = buffer.readable();
			buffer.writeShort(0);
			for (int v = 0; v < filter.valueSize(); v++) {
				buffer.writeByte(filter.getValues(v).length);
				buffer.write(filter.getValues(v));
			}
			// SET Length
			length = buffer.readable() - p - 2;
			buffer.set(p++, (byte) (length >>> 8));
			buffer.set(p, (byte) length);
		}

		// SET Length
		length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 8446 */
	static OIDFilters decodeOIDFilters(DataBuffer buffer, int length) throws IOException {
		final OIDFilters filters = new OIDFilters();
		if (length > 0) {
			OIDFilter filter;
			byte[] value;

			// OIDFilter filters<0..2^16-1>;
			length = buffer.readUnsignedShort();
			while (length > 0) {
				filter = new OIDFilter();

				// opaque certificate_extension_oid<1..2^8-1>;
				filter.setOID(new byte[buffer.readUnsignedByte()]);
				buffer.readFully(filter.getOID());
				length -= filter.getOID().length + 1;

				// opaque certificate_extension_values<0..2^16-1>;
				int len = buffer.readUnsignedShort();
				length -= len;
				while (len > 0) {
					buffer.readFully(value = new byte[buffer.readUnsignedByte()]);
					filter.addValue(value);
					len -= value.length + 1;
				}
			}
		}
		return filters;
	}

	/** RFC 8446 */
	static void encode(Cookie cookie, DataBuffer buffer) throws IOException {
		buffer.writeShort(cookie.get().length);
		buffer.write(cookie.get());
	}

	/** RFC 8446 */
	static Cookie decodeCookie(DataBuffer buffer, int length) throws IOException {
		final Cookie cookie = new Cookie();
		if (length > 0) {
			cookie.set(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(cookie.get());
		}
		return cookie;
	}

	/** RFC 5746 */
	static void encode(RenegotiationInfo extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.get().length);
		buffer.write(extension.get());
	}

	/** RFC 5746 */
	static RenegotiationInfo decodeRenegotiationInfo(DataBuffer buffer, int length) throws IOException {
		final RenegotiationInfo info = new RenegotiationInfo();
		if (length > 0) {
			// opaque renegotiated_connection<0..255>;
			info.set(new byte[buffer.readUnsignedByte()]);
			buffer.readFully(info.get());
		}
		return info;
	}

	static ApplicationSettings decodeApplicationSettings(DataBuffer buffer, int length) throws IOException {
		final ApplicationSettings settings = new ApplicationSettings();
		if (length > 0) {
			// ProtocolName supported_protocols<2..2^16-1>;
			length = buffer.readUnsignedShort();
			byte[] opaque;
			while (length > 0) {
				buffer.readFully(opaque = new byte[buffer.readByte()]);
				settings.add(opaque);
				length -= opaque.length + 1;
			}
		}
		return settings;
	}

	/** RFC ??? */
	static void encode(EncryptedClientHello extension, DataBuffer buffer) throws IOException {
		// TODO
	}

	static EncryptedClientHello decodeEncryptedClientHello(DataBuffer buffer, int length) throws IOException {
		final EncryptedClientHello extension = new EncryptedClientHello();
		// TODO
		return extension;
	}
}