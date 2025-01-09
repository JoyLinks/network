package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 特定扩展字段编解码
 * 
 * @author ZhangXi 2024年12月19日
 */
public class ExtensionCoder {

	// 这些定义的扩展如此混乱不堪
	/*-
	 * server_name 				C-S-C
	 * max_fragment_length		C-S-C
	 * client_certificate_url	C-S-C
	 * trusted_ca_keys			C-S-C
	 * truncated_hmac			C-S-C
	 * status_request			C-S-C
	 * psk_key_exchange_modes	C-S
	 */

	public static void encodeByServer(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		int p1, p2, length;
		// Length 2Byte
		p1 = buffer.readable();
		buffer.writeShort(0);
		// SUB Extensions
		Extension extension;
		for (int index = 0; index < extensions.getExtensions().size(); index++) {
			extension = extensions.getExtensions().get(index);

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
				encode((ClientCertificateURL) extension, buffer);
			} else if (extension.type() == Extension.TRUSTED_CA_KEYS) {
				encode((TrustedAuthorities) extension, buffer);
			} else if (extension.type() == Extension.TRUNCATED_HMAC) {
				encode((TruncatedHMAC) extension, buffer);
			} else if (extension.type() == Extension.STATUS_REQUEST) {
				encode((StatusRequest) extension, buffer);
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
				encode((ClientCertificateType) extension, buffer);
			} else if (extension.type() == Extension.SERVER_CERTIFICATE_TYPE) {
				encode((ServerCertificateType) extension, buffer);
			} else if (extension.type() == Extension.PADDING) {
				encode((Padding) extension, buffer);
			} else if (extension.type() == Extension.EXTENDED_MASTER_SECRET) {
				encode((ExtendedMasterSecret) extension, buffer);
			} else if (extension.type() == Extension.COMPRESS_CERTIFICATE) {
				encode((CompressCertificate) extension, buffer);
			} else if (extension.type() == Extension.SESSION_TICKET) {
				encode((SessionTicket) extension, buffer);
			} else if (extension.type() == Extension.PRE_SHARED_KEY) {
				encodePreSharedKey2((PreSharedKey) extension, buffer);
			} else if (extension.type() == Extension.EARLY_DATA) {
				encode((EarlyData) extension, buffer);
			} else if (extension.type() == Extension.SUPPORTED_VERSIONS) {
				encode((SupportedVersions) extension, buffer);
			} else if (extension.type() == Extension.COOKIE) {
				encode((Cookie) extension, buffer);
			} else if (extension.type() == Extension.PSK_KEY_EXCHANGE_MODES) {
				encode((PskKeyExchangeModes) extension, buffer);
			} else if (extension.type() == Extension.CERTIFICATE_AUTHORITIES) {
				encode((CertificateAuthorities) extension, buffer);
			} else if (extension.type() == Extension.OID_FILTERS) {
				encode((OIDFilters) extension, buffer);
			} else if (extension.type() == Extension.POST_HANDSHAKE_AUTH) {
				encode((PostHandshakeAuth) extension, buffer);
			} else if (extension.type() == Extension.SIGNATURE_ALGORITHMS_CERT) {
				encode((SignatureAlgorithmsCert) extension, buffer);
			} else if (extension.type() == Extension.KEY_SHARE) {
				encodeKeyShare2((KeyShare) extension, buffer);
				// encodeKeyShare3((KeyShare) extension, buffer);
			} else if (extension.type() == Extension.RENEGOTIATION_INFO) {
				encode((RenegotiationInfo) extension, buffer);
			} else if (extension.type() == Extension.APPLICATION_SETTINGS) {
				encode((ApplicationSettings) extension, buffer);
			} else if (extension.type() == Extension.ENCRYPTED_CLIENT_HELLO) {
				encode((EncryptedClientHello) extension, buffer);
			} else {
				encode((Reserved) extension, buffer);
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

	public static void encodeByClient(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		int p1, p2, length;
		// Length 2Byte
		p1 = buffer.readable();
		buffer.writeShort(0);
		// SUB Extensions
		Extension extension;
		for (int index = 0; index < extensions.getExtensions().size(); index++) {
			extension = extensions.getExtensions().get(index);

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
				encode((ClientCertificateURL) extension, buffer);
			} else if (extension.type() == Extension.TRUSTED_CA_KEYS) {
				encode((TrustedAuthorities) extension, buffer);
			} else if (extension.type() == Extension.TRUNCATED_HMAC) {
				encode((TruncatedHMAC) extension, buffer);
			} else if (extension.type() == Extension.STATUS_REQUEST) {
				encode((StatusRequest) extension, buffer);
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
				encode((ClientCertificateType) extension, buffer);
			} else if (extension.type() == Extension.SERVER_CERTIFICATE_TYPE) {
				encode((ServerCertificateType) extension, buffer);
			} else if (extension.type() == Extension.PADDING) {
				encode((Padding) extension, buffer);
			} else if (extension.type() == Extension.EXTENDED_MASTER_SECRET) {
				encode((ExtendedMasterSecret) extension, buffer);
			} else if (extension.type() == Extension.COMPRESS_CERTIFICATE) {
				encode((CompressCertificate) extension, buffer);
			} else if (extension.type() == Extension.SESSION_TICKET) {
				encode((SessionTicket) extension, buffer);
			} else if (extension.type() == Extension.PRE_SHARED_KEY) {
				encodePreSharedKey1((PreSharedKey) extension, buffer);
			} else if (extension.type() == Extension.EARLY_DATA) {
				encode((EarlyData) extension, buffer);
			} else if (extension.type() == Extension.SUPPORTED_VERSIONS) {
				encode((SupportedVersions) extension, buffer);
			} else if (extension.type() == Extension.COOKIE) {
				encode((Cookie) extension, buffer);
			} else if (extension.type() == Extension.PSK_KEY_EXCHANGE_MODES) {
				encode((PskKeyExchangeModes) extension, buffer);
			} else if (extension.type() == Extension.CERTIFICATE_AUTHORITIES) {
				encode((CertificateAuthorities) extension, buffer);
			} else if (extension.type() == Extension.OID_FILTERS) {
				encode((OIDFilters) extension, buffer);
			} else if (extension.type() == Extension.POST_HANDSHAKE_AUTH) {
				encode((PostHandshakeAuth) extension, buffer);
			} else if (extension.type() == Extension.SIGNATURE_ALGORITHMS_CERT) {
				encode((SignatureAlgorithmsCert) extension, buffer);
			} else if (extension.type() == Extension.KEY_SHARE) {
				encodeKeyShare1((KeyShare) extension, buffer);
			} else if (extension.type() == Extension.RENEGOTIATION_INFO) {
				encode((RenegotiationInfo) extension, buffer);
			} else if (extension.type() == Extension.APPLICATION_SETTINGS) {
				encode((ApplicationSettings) extension, buffer);
			} else if (extension.type() == Extension.ENCRYPTED_CLIENT_HELLO) {
				encode((EncryptedClientHello) extension, buffer);
			} else {
				encode((Reserved) extension, buffer);
			}
			// SET Length
			length = buffer.readable() - p2 - 2;
			buffer.set(p2++, (byte) (length >>> 8));
			buffer.set(p2, (byte) length);
		}
		if (extensions.msgType() == Handshake.CLIENT_HELLO) {
			// AUTO Padding ClientHello >= 512
			// Record 5Byte , Handshake 4Byte
			if (buffer.readable() < 512 + 5) {
				final Padding padding = new Padding(517 - buffer.readable());
				// Type 2Byte
				buffer.writeShort(padding.type());
				// opaque extension_data<0..2^16-1>;
				buffer.writeShort(padding.getSiez() - 4);
				encode(padding, buffer);
			}
		}
		// SET Length
		length = buffer.readable() - p1 - 2;
		buffer.set(p1++, (byte) (length >>> 8));
		buffer.set(p1, (byte) length);
	}

	public static void decodeByClient(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int length = buffer.readUnsignedShort();
		// 计算扩展字段全部解码后的剩余字节
		length = buffer.readable() - length;

		int len;
		short type;
		Extension extension;
		while (buffer.readable() > length) {
			// Type 2Byte
			type = buffer.readShort();
			// opaque extension_data<0..2^16-1>;
			len = buffer.readUnsignedShort();
			// SUB Extension
			if (type == Extension.SERVER_NAME) {
				extension = decodeServerNames(buffer);
			} else if (type == Extension.MAX_FRAGMENT_LENGTH) {
				extension = decodeMaxFragmentLength(buffer);
			} else if (type == Extension.CLIENT_CERTIFICATE_URL) {
				extension = decodeClientCertificateURL(buffer);
			} else if (type == Extension.TRUSTED_CA_KEYS) {
				extension = decodeTrustedCAKeys(buffer);
			} else if (type == Extension.TRUNCATED_HMAC) {
				extension = decodeTruncatedHMAC(buffer);
			} else if (type == Extension.STATUS_REQUEST) {
				extension = decodeStatusRequest(buffer, len);
			} else if (type == Extension.SUPPORTED_GROUPS) {
				extension = decodeSupportedGroups(buffer);
			} else if (type == Extension.EC_POINT_FORMATS) {
				extension = decodeECPointFormats(buffer);
			} else if (type == Extension.SIGNATURE_ALGORITHMS) {
				extension = decodeSignatureAlgorithms(buffer);
			} else if (type == Extension.USE_SRTP) {
				extension = decodeUseSrtp(buffer);
			} else if (type == Extension.HEARTBEAT) {
				extension = decodeHeartbeat(buffer);
			} else if (type == Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION) {
				extension = decodeApplicationLayerProtocolNegotiation(buffer);
			} else if (type == Extension.SIGNED_CERTIFICATE_TIMESTAMP) {
				extension = decodeSignedCertificateTimestamp(buffer);
			} else if (type == Extension.CLIENT_CERTIFICATE_TYPE) {
				extension = decodeClientCertificateType(buffer, len);
			} else if (type == Extension.SERVER_CERTIFICATE_TYPE) {
				extension = decodeServerCertificateType(buffer, len);
			} else if (type == Extension.PADDING) {
				extension = decodePadding(buffer, len);
			} else if (type == Extension.EXTENDED_MASTER_SECRET) {
				extension = decodeExtendedMasterSecret(buffer);
			} else if (type == Extension.COMPRESS_CERTIFICATE) {
				extension = decodeCompressCertificate(buffer);
			} else if (type == Extension.SESSION_TICKET) {
				extension = decodeSessionTicket(buffer);
			} else if (type == Extension.PRE_SHARED_KEY) {
				extension = decodePreSharedKey2(buffer);
			} else if (type == Extension.EARLY_DATA) {
				extension = decodeEarlyData1(buffer);
			} else if (type == Extension.SUPPORTED_VERSIONS) {
				extension = decodeSupportedVersion(buffer);
			} else if (type == Extension.COOKIE) {
				extension = decodeCookie(buffer);
			} else if (type == Extension.PSK_KEY_EXCHANGE_MODES) {
				extension = decodePskKeyExchangeModes(buffer);
			} else if (type == Extension.CERTIFICATE_AUTHORITIES) {
				extension = decodeCertificateAuthorities(buffer);
			} else if (type == Extension.OID_FILTERS) {
				extension = decodeOIDFilters(buffer);
			} else if (type == Extension.POST_HANDSHAKE_AUTH) {
				extension = decodePostHandshakeAuth(buffer);
			} else if (type == Extension.SIGNATURE_ALGORITHMS_CERT) {
				extension = decodeSignatureAlgorithmsCert(buffer);
			} else if (type == Extension.KEY_SHARE) {
				if (extensions.isHelloRetryRequest()) {
					extension = decodeKeyShare2(buffer);
				} else {
					extension = decodeKeyShare3(buffer);
				}
			} else if (type == Extension.RENEGOTIATION_INFO) {
				extension = decodeRenegotiationInfo(buffer);
			} else if (type == Extension.RENEGOTIATION_INFO) {
				extension = decodeApplicationSettings(buffer);
			} else if (type == Extension.ENCRYPTED_CLIENT_HELLO) {
				extension = decodeEncryptedClientHello(buffer);
			} else {
				extension = decodeReserved(buffer, len);
			}
			extensions.getExtensions().add(extension);
		}
	}

	public static void decodeByServer(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int length = buffer.readUnsignedShort();
		// 计算扩展字段全部解码后的剩余字节
		length = buffer.readable() - length;

		int len;
		short type;
		Extension extension;
		while (buffer.readable() > length) {
			// Type 2Byte
			type = buffer.readShort();
			// opaque extension_data<0..2^16-1>;
			len = buffer.readUnsignedShort();
			// SUB Extension
			if (type == Extension.SERVER_NAME) {
				extension = decodeServerNames(buffer);
			} else if (type == Extension.MAX_FRAGMENT_LENGTH) {
				extension = decodeMaxFragmentLength(buffer);
			} else if (type == Extension.CLIENT_CERTIFICATE_URL) {
				extension = decodeClientCertificateURL(buffer);
			} else if (type == Extension.TRUSTED_CA_KEYS) {
				extension = decodeTrustedCAKeys(buffer);
			} else if (type == Extension.TRUNCATED_HMAC) {
				extension = decodeTruncatedHMAC(buffer);
			} else if (type == Extension.STATUS_REQUEST) {
				extension = decodeStatusRequest(buffer, len);
			} else if (type == Extension.SUPPORTED_GROUPS) {
				extension = decodeSupportedGroups(buffer);
			} else if (type == Extension.EC_POINT_FORMATS) {
				extension = decodeECPointFormats(buffer);
			} else if (type == Extension.SIGNATURE_ALGORITHMS) {
				extension = decodeSignatureAlgorithms(buffer);
			} else if (type == Extension.USE_SRTP) {
				extension = decodeUseSrtp(buffer);
			} else if (type == Extension.HEARTBEAT) {
				extension = decodeHeartbeat(buffer);
			} else if (type == Extension.APPLICATION_LAYER_PROTOCOL_NEGOTIATION) {
				extension = decodeApplicationLayerProtocolNegotiation(buffer);
			} else if (type == Extension.SIGNED_CERTIFICATE_TIMESTAMP) {
				extension = decodeSignedCertificateTimestamp(buffer);
			} else if (type == Extension.CLIENT_CERTIFICATE_TYPE) {
				extension = decodeClientCertificateType(buffer, len);
			} else if (type == Extension.SERVER_CERTIFICATE_TYPE) {
				extension = decodeServerCertificateType(buffer, len);
			} else if (type == Extension.PADDING) {
				extension = decodePadding(buffer, len);
			} else if (type == Extension.EXTENDED_MASTER_SECRET) {
				extension = decodeExtendedMasterSecret(buffer);
			} else if (type == Extension.COMPRESS_CERTIFICATE) {
				extension = decodeCompressCertificate(buffer);
			} else if (type == Extension.SESSION_TICKET) {
				extension = decodeSessionTicket(buffer);
			} else if (type == Extension.PRE_SHARED_KEY) {
				extension = decodePreSharedKey1(buffer);
			} else if (type == Extension.EARLY_DATA) {
				extension = decodeEarlyData2(buffer);
			} else if (type == Extension.SUPPORTED_VERSIONS) {
				extension = decodeSupportedVersions(buffer);
			} else if (type == Extension.COOKIE) {
				extension = decodeCookie(buffer);
			} else if (type == Extension.PSK_KEY_EXCHANGE_MODES) {
				extension = decodePskKeyExchangeModes(buffer);
			} else if (type == Extension.CERTIFICATE_AUTHORITIES) {
				extension = decodeCertificateAuthorities(buffer);
			} else if (type == Extension.OID_FILTERS) {
				extension = decodeOIDFilters(buffer);
			} else if (type == Extension.POST_HANDSHAKE_AUTH) {
				extension = decodePostHandshakeAuth(buffer);
			} else if (type == Extension.SIGNATURE_ALGORITHMS_CERT) {
				extension = decodeSignatureAlgorithmsCert(buffer);
			} else if (type == Extension.KEY_SHARE) {
				extension = decodeKeyShare1(buffer);
			} else if (type == Extension.RENEGOTIATION_INFO) {
				extension = decodeRenegotiationInfo(buffer);
			} else if (type == Extension.RENEGOTIATION_INFO) {
				extension = decodeApplicationSettings(buffer);
			} else if (type == Extension.ENCRYPTED_CLIENT_HELLO) {
				extension = decodeEncryptedClientHello(buffer);
			} else {
				extension = decodeReserved(buffer, len);
			}
			extensions.getExtensions().add(extension);
		}
	}

	/** RFC 6066 */
	private static void encode(ServerNames extension, DataBuffer buffer) throws IOException {
		int length = 0;
		ServerName item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			length += item.getName().length;
			length += 3;
		}
		// Length 2Byte
		buffer.writeShort(length);
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeByte(item.type());
			buffer.writeShort(item.getName().length);
			buffer.write(item.getName());
		}
	}

	/** RFC 6066 */
	private static void encode(MaxFragmentLength extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getType());
	}

	/** RFC 6066 */
	private static void encode(ClientCertificateURL extension, DataBuffer buffer) throws IOException {
		// EMPTY
	}

	/** RFC 6066 */
	private static void encode(TrustedAuthorities extension, DataBuffer buffer) throws IOException {
		TrustedAuthority item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeByte(item.type());
			buffer.writeShort(item.getData().length);
			buffer.write(item.getData());
		}
	}

	/** RFC 6066 */
	private static void encode(TruncatedHMAC extension, DataBuffer buffer) throws IOException {
		// EMPTY
	}

	/** RFC 6066 */
	private static void encode(StatusRequest extension, DataBuffer buffer) throws IOException {
		if (extension.getRequest() != null) {
			buffer.writeByte(extension.getRequest().type());
			if (extension.getRequest().type() == CertificateStatusRequest.OCSP) {
				final OCSPStatusRequest request = (OCSPStatusRequest) extension.getRequest();
				buffer.writeShort(request.getResponderID().length);
				buffer.write(request.getResponderID());
				buffer.writeShort(request.getExtensions().length);
				buffer.write(request.getExtensions());
			}
		}
	}

	/** RFC 7919 */
	private static void encode(SupportedGroups extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC 8422 */
	private static void encode(ECPointFormats extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size());
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index));
		}
	}

	/** RFC 8446 */
	private static void encode(SignatureAlgorithms extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC 5764 */
	private static void encode(UseSRTP extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
		buffer.writeShort(extension.getMKI().length);
		buffer.write(extension.getMKI());
	}

	/** RFC 6520 */
	private static void encode(Heartbeat extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getMode());
	}

	/** RFC 7301 */
	private static void encode(ApplicationLayerProtocolNegotiation extension, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int position = buffer.readable();
		buffer.writeShort(0);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index).length);
			buffer.write(extension.get(index));
		}
		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length >>> 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 6962 */
	private static void encode(SignedCertificateTimestamp extension, DataBuffer buffer) throws IOException {
		if (extension.size() > 0) {
			// Length 2Byte
			int position = buffer.readable();
			buffer.writeShort(0);
			for (int index = 0; index < extension.size(); index++) {
				buffer.writeShort(extension.get(index).length);
				buffer.write(extension.get(index));
			}
			// SET Length
			int length = buffer.readable() - position - 2;
			buffer.set(position++, (byte) (length >>> 8));
			buffer.set(position, (byte) length);
		}
	}

	/** RFC 7250 */
	private static void encode(ClientCertificateType extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index));
		}
		// if (client) {
		// buffer.writeShort(extension.size());
		// for (int index = 0; index < extension.size(); index++) {
		// buffer.writeByte(extension.get(index));
		// }
		// } else {
		// buffer.writeByte(extension.get(0));
		// }
	}

	/** RFC 7250 */
	private static void encode(ServerCertificateType extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index));
		}
		// if (client) {
		// buffer.writeShort(extension.size());
		// for (int index = 0; index < extension.size(); index++) {
		// buffer.writeByte(extension.get(index));
		// }
		// } else {
		// buffer.writeByte(extension.get(0));
		// }
	}

	/** RFC 7685 */
	private static void encode(Padding extension, DataBuffer buffer) throws IOException {
		int size = extension.getSiez() - 4;
		while (size-- > 0) {
			buffer.writeByte(0);
		}
	}

	/** RFC 7627 */
	private static void encode(ExtendedMasterSecret extension, DataBuffer buffer) throws IOException {
		// EMPTY
	}

	/** RFC 8879 */
	private static void encode(CompressCertificate extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC ???? */
	private static void encode(SessionTicket extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getTicket().length);
		buffer.write(extension.getTicket());
	}

	/** RFC 8446 client_shares TO Server */
	private static void encodeKeyShare1(KeyShare extension, DataBuffer buffer) throws IOException {
		int length = 0;
		KeyShareEntry item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			length += item.getKeyExchange().length;
			length += 4;
		}
		// Length 2Byte
		buffer.writeShort(length);
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.group());
			buffer.writeShort(item.getKeyExchange().length);
			buffer.write(item.getKeyExchange());
		}
	}

	/** RFC 8446 selected_group TO Client */
	private static void encodeKeyShare2(KeyShare extension, DataBuffer buffer) throws IOException {
		final KeyShareEntry item = extension.get(0);
		buffer.writeShort(item.group());
	}

	/** RFC 8446 server_share TO Client */
	private static void encodeKeyShare3(KeyShare extension, DataBuffer buffer) throws IOException {
		final KeyShareEntry item = extension.get(0);
		buffer.writeShort(item.group());
		buffer.writeShort(item.getKeyExchange().length);
		buffer.write(item.getKeyExchange());
	}

	/** RFC 5746 */
	private static void encode(RenegotiationInfo extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getValue().length);
		buffer.write(extension.getValue());
	}

	/** RFC 8446 client_hello */
	private static void encodePreSharedKey1(PreSharedKey extension, DataBuffer buffer) throws IOException {
		PskIdentity item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.getIdentity().length);
			buffer.write(item.getIdentity());
			buffer.writeInt(item.getTicket_age());
		}
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.getBinder().length);
			buffer.write(item.getBinder());
		}
	}

	/** RFC 8446 server_hello */
	private static void encodePreSharedKey2(PreSharedKey extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getSelected());
	}

	/** RFC 8446 */
	private static void encode(PskKeyExchangeModes extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size());
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index));
		}
	}

	/** RFC 8446 */
	private static void encode(EarlyData extension, DataBuffer buffer) throws IOException {
		if (extension.getMaxSize() > 0) {
			buffer.writeInt(extension.getMaxSize());
		}
	}

	/** RFC 8446 */
	private static void encode(Cookie extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getCookie().length);
		buffer.write(extension.getCookie());
	}

	/** RFC 8446 */
	private static void encode(SupportedVersions extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC 8446 */
	private static void encode(CertificateAuthorities extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).length);
			buffer.write(extension.get(index));
		}
	}

	/** RFC 8446 */
	private static void encode(OIDFilters extension, DataBuffer buffer) throws IOException {
		OIDFilter item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.getOID().length);
			buffer.write(item.getOID());
			buffer.writeShort(item.getValues().length);
			buffer.write(item.getValues());
		}
	}

	/** RFC 8446 */
	private static void encode(PostHandshakeAuth extension, DataBuffer buffer) throws IOException {
		// EMPTY
	}

	/** RFC 8446 */
	private static void encode(SignatureAlgorithmsCert extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC ??? */
	private static void encode(EncryptedClientHello extension, DataBuffer buffer) throws IOException {
		// TODO
	}

	private static void encode(Reserved extension, DataBuffer buffer) throws IOException {
		buffer.write(extension.getData());
	}

	private static Reserved decodeReserved(DataBuffer buffer, int length) throws IOException {
		final Reserved extension = new Reserved();
		buffer.skipBytes(length);
		return extension;
	}

	/** RFC 6066 */
	private static ServerNames decodeServerNames(DataBuffer buffer) throws IOException {
		final ServerNames extension = new ServerNames();
		byte type;
		byte[] name;
		while (buffer.readable() > 3) {
			type = buffer.readByte();
			buffer.readFully(name = new byte[buffer.readUnsignedShort()]);
			extension.add(new ServerName(type, name));
		}
		return extension;
	}

	/** RFC 6066 */
	private static MaxFragmentLength decodeMaxFragmentLength(DataBuffer buffer) throws IOException {
		return new MaxFragmentLength(buffer.readByte());
	}

	/** RFC 6066 */
	private static ClientCertificateURL decodeClientCertificateURL(DataBuffer buffer) throws IOException {
		return ClientCertificateURL.INSTANCE;
	}

	/** RFC 6066 */
	private static TrustedAuthorities decodeTrustedCAKeys(DataBuffer buffer) throws IOException {
		final TrustedAuthorities extension = new TrustedAuthorities();
		byte type;
		byte[] data;
		while (buffer.readable() > 3) {
			type = buffer.readByte();
			buffer.readFully(data = new byte[buffer.readUnsignedShort()]);
			extension.add(new TrustedAuthority(type, data));
		}
		return extension;
	}

	/** RFC 6066 */
	private static TruncatedHMAC decodeTruncatedHMAC(DataBuffer buffer) throws IOException {
		return TruncatedHMAC.INSTANCE;
	}

	/** RFC 6066 */
	private static StatusRequest decodeStatusRequest(DataBuffer buffer, int length) throws IOException {
		final StatusRequest extension = new StatusRequest();
		if (buffer.readByte() == CertificateStatusRequest.OCSP) {
			final OCSPStatusRequest request = new OCSPStatusRequest();
			byte[] opaque;
			buffer.readFully(opaque = new byte[buffer.readShort()]);
			request.setResponderID(opaque);
			buffer.readFully(opaque = new byte[buffer.readShort()]);
			request.setResponderID(opaque);
			extension.setRequest(request);
		} else {
			buffer.skipBytes(length - 1);
		}
		return extension;
	}

	/** RFC 7919 */
	private static SupportedGroups decodeSupportedGroups(DataBuffer buffer) throws IOException {
		final SupportedGroups extension = new SupportedGroups();
		int size = buffer.readShort() / 2;
		while (size-- > 0) {
			extension.add(buffer.readShort());
		}
		return extension;
	}

	/** RFC 8442 */
	private static ECPointFormats decodeECPointFormats(DataBuffer buffer) throws IOException {
		final ECPointFormats extension = new ECPointFormats();
		int size = buffer.readByte();
		while (size-- > 0) {
			extension.add(buffer.readByte());
		}
		return extension;
	}

	/** RFC 8446 */
	private static SignatureAlgorithms decodeSignatureAlgorithms(DataBuffer buffer) throws IOException {
		final SignatureAlgorithms extension = new SignatureAlgorithms();
		int size = buffer.readShort() / 2;
		while (size-- > 0) {
			extension.add(buffer.readShort());
		}
		return extension;
	}

	/** RFC 5764 */
	private static UseSRTP decodeUseSrtp(DataBuffer buffer) throws IOException {
		final UseSRTP extension = new UseSRTP();
		while (buffer.readable() > 1) {
			extension.add(buffer.readShort());
		}
		final byte[] opaque = new byte[buffer.readUnsignedShort()];
		buffer.readFully(opaque);
		extension.setMKI(opaque);
		return extension;
	}

	/** RFC 6520 */
	private static Heartbeat decodeHeartbeat(DataBuffer buffer) throws IOException {
		return new Heartbeat(buffer.readByte());
	}

	/** RFC 7301 */
	private static ApplicationLayerProtocolNegotiation decodeApplicationLayerProtocolNegotiation(DataBuffer buffer) throws IOException {
		final ApplicationLayerProtocolNegotiation extension = new ApplicationLayerProtocolNegotiation();
		int length = buffer.readShort();
		if (length > 0) {
			byte[] opaque;
			while (length > 0) {
				buffer.readFully(opaque = new byte[buffer.readByte()]);
				extension.add(opaque);
				length -= opaque.length;
			}
		}
		return extension;
	}

	/** RFC 6962 */
	private static SignedCertificateTimestamp decodeSignedCertificateTimestamp(DataBuffer buffer) throws IOException {
		final SignedCertificateTimestamp extension = new SignedCertificateTimestamp();
		int length = buffer.readShort();
		byte[] opaque;
		int size = buffer.readShort();
		while (size-- > 0) {
			buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
			extension.add(opaque);
		}
		return extension;
	}

	/** RFC 7250 */
	private static ClientCertificateType decodeClientCertificateType(DataBuffer buffer, int length) throws IOException {
		final ClientCertificateType extension = new ClientCertificateType();
		while (length-- > 0) {
			extension.add(buffer.readByte());
		}
		return extension;
	}

	/** RFC 7250 */
	private static ServerCertificateType decodeServerCertificateType(DataBuffer buffer, int length) throws IOException {
		final ServerCertificateType extension = new ServerCertificateType();
		while (length-- > 0) {
			extension.add(buffer.readByte());
		}
		return extension;
	}

	/** RFC 7685 */
	private static Padding decodePadding(DataBuffer buffer, int length) throws IOException {
		final Padding extension = new Padding();
		buffer.skipBytes(length);
		extension.setSiez(length + 4);
		return extension;
	}

	/** RFC 7627 */
	private static ExtendedMasterSecret decodeExtendedMasterSecret(DataBuffer buffer) throws IOException {
		return ExtendedMasterSecret.INSTANCE;
	}

	/** RFC 8879 */
	private static CompressCertificate decodeCompressCertificate(DataBuffer buffer) throws IOException {
		final CompressCertificate extension = new CompressCertificate();
		int size = buffer.readByte() / 2;
		while (size-- > 0) {
			extension.add(buffer.readShort());
		}
		return extension;
	}

	/** RFC ???? */
	private static SessionTicket decodeSessionTicket(DataBuffer buffer) throws IOException {
		final SessionTicket extension = new SessionTicket();
		extension.setTicket(new byte[buffer.readShort()]);
		buffer.readFully(extension.getTicket());
		return extension;
	}

	/** RFC 8446 client_shares TO Server */
	private static KeyShare decodeKeyShare1(DataBuffer buffer) throws IOException {
		final KeyShare extension = new KeyShare();
		int length = buffer.readShort();
		if (length > 0) {
			short group;
			byte[] keyExchange;
			while (length > 0) {
				group = buffer.readShort();
				buffer.readFully(keyExchange = new byte[buffer.readShort()]);
				extension.add(new KeyShareEntry(group, keyExchange));
				length -= keyExchange.length + 4;
			}
		}
		return extension;
	}

	/** RFC 8446 selected_group TO Client */
	private static KeyShare decodeKeyShare2(DataBuffer buffer) throws IOException {
		final KeyShare extension = new KeyShare();
		final short group = buffer.readShort();
		extension.add(new KeyShareEntry(group));
		return extension;
	}

	/** RFC 8446 server_share TO Client */
	private static KeyShare decodeKeyShare3(DataBuffer buffer) throws IOException {
		final KeyShare extension = new KeyShare();
		final short group = buffer.readShort();
		final byte[] keyExchange = new byte[buffer.readShort()];
		buffer.readFully(keyExchange);
		extension.add(new KeyShareEntry(group, keyExchange));
		return extension;
	}

	/** RFC 5746 */
	private static RenegotiationInfo decodeRenegotiationInfo(DataBuffer buffer) throws IOException {
		final RenegotiationInfo extension = new RenegotiationInfo();
		byte[] opaque;
		buffer.readFully(opaque = new byte[buffer.readByte()]);
		extension.setValue(opaque);
		return extension;
	}

	private static ApplicationSettings decodeApplicationSettings(DataBuffer buffer) throws IOException {
		final ApplicationSettings extension = new ApplicationSettings();
		int length = buffer.readShort();
		if (length > 0) {
			byte[] opaque;
			while (length > 0) {
				buffer.readFully(opaque = new byte[buffer.readByte()]);
				extension.add(opaque);
				length -= opaque.length;
			}
		}
		return extension;
	}

	private static EncryptedClientHello decodeEncryptedClientHello(DataBuffer buffer) throws IOException {
		final EncryptedClientHello extension = new EncryptedClientHello();
		// TODO
		return extension;
	}

	/** RFC 8446 client_hello */
	private static PreSharedKey decodePreSharedKey1(DataBuffer buffer) throws IOException {
		final PreSharedKey extension = new PreSharedKey();
		byte[] data;
		while (buffer.readable() > 6) {
			buffer.readFully(data = new byte[buffer.readUnsignedShort()]);
			extension.add(new PskIdentity(buffer.readInt(), data));
		}
		int index = 0;
		while (buffer.readable() > 32) {
			buffer.readFully(data = new byte[buffer.readUnsignedShort()]);
			extension.get(index++).setBinder(data);
		}
		return extension;
	}

	/** RFC 8446 server_hello */
	private static PreSharedKey decodePreSharedKey2(DataBuffer buffer) throws IOException {
		final PreSharedKey extension = new PreSharedKey();
		extension.setSelected(buffer.readShort());
		return extension;
	}

	/** RFC 8446 */
	private static PskKeyExchangeModes decodePskKeyExchangeModes(DataBuffer buffer) throws IOException {
		final PskKeyExchangeModes extension = new PskKeyExchangeModes();
		int size = buffer.readByte();
		while (size-- > 0) {
			extension.add(buffer.readByte());
		}
		return extension;
	}

	/** RFC 8446 */
	private static EarlyData decodeEarlyData1(DataBuffer buffer) throws IOException {
		final EarlyData extension = new EarlyData();
		extension.setMaxSize(buffer.readInt());
		return extension;
	}

	/** RFC 8446 */
	private static EarlyData decodeEarlyData2(DataBuffer buffer) throws IOException {
		return EarlyData.EMPTY;
	}

	/** RFC 8446 */
	private static Cookie decodeCookie(DataBuffer buffer) throws IOException {
		final Cookie extension = new Cookie();
		final byte[] value = new byte[buffer.readUnsignedShort()];
		buffer.readFully(value);
		extension.setCookie(value);
		return extension;
	}

	/** RFC 8446 */
	private static SupportedVersions decodeSupportedVersion(DataBuffer buffer) throws IOException {
		final SupportedVersions extension = new SupportedVersions();
		extension.add(buffer.readShort());
		return extension;
	}

	/** RFC 8446 */
	private static SupportedVersions decodeSupportedVersions(DataBuffer buffer) throws IOException {
		final SupportedVersions extension = new SupportedVersions();
		int size = buffer.readUnsignedByte() / 2;
		while (size-- > 0) {
			extension.add(buffer.readShort());
		}
		return extension;
	}

	/** RFC 8446 */
	private static CertificateAuthorities decodeCertificateAuthorities(DataBuffer buffer) throws IOException {
		final CertificateAuthorities extension = new CertificateAuthorities();
		byte[] dname;
		while (buffer.readable() > 0) {
			buffer.readFully(dname = new byte[buffer.readUnsignedShort()]);
			extension.add(dname);
		}
		return extension;
	}

	/** RFC 8446 */
	private static OIDFilters decodeOIDFilters(DataBuffer buffer) throws IOException {
		final OIDFilters extension = new OIDFilters();
		OIDFilter item;
		byte[] opaque;
		while (buffer.readable() > 0) {
			buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
			item = new OIDFilter(opaque);
			buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
			item.setValues(opaque);
		}
		return extension;
	}

	/** RFC 8446 */
	private static PostHandshakeAuth decodePostHandshakeAuth(DataBuffer buffer) throws IOException {
		return PostHandshakeAuth.INSTANCE;
	}

	/** RFC 8446 */
	private static SignatureAlgorithmsCert decodeSignatureAlgorithmsCert(DataBuffer buffer) throws IOException {
		final SignatureAlgorithmsCert extension = new SignatureAlgorithmsCert();
		while (buffer.readable() > 1) {
			extension.add(buffer.readShort());
		}
		return extension;
	}
}