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

	public static void encode(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int position = buffer.readable();
		buffer.writeShort(0);
		// SUB Extensions
		for (int index = 0; index < extensions.getExtensions().size(); index++) {
			encode(extensions.getMsgType(), extensions.getExtensions().get(index), buffer);
		}
		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length << 8));
		buffer.set(position, (byte) length);
	}

	public static void encode(HandshakeType type, Extension extension, DataBuffer buffer) throws IOException {
		// Type 2Byte
		buffer.writeShort(extension.type().code());
		// opaque extension_data<0..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);
		// SUB Extensions
		if (extension.type() == ExtensionType.SERVER_NAME) {
			encode((ServerNames) extension, buffer);
		} else if (extension.type() == ExtensionType.MAX_FRAGMENT_LENGTH) {
			encode((MaxFragmentLength) extension, buffer);
		} else if (extension.type() == ExtensionType.CLIENT_CERTIFICATE_URL) {
			encode((ClientCertificateURL) extension, buffer);
		} else if (extension.type() == ExtensionType.TRUSTED_CA_KEYS) {
			encode((TrustedAuthorities) extension, buffer);
		} else if (extension.type() == ExtensionType.TRUNCATED_HMAC) {
			encode((TruncatedHMAC) extension, buffer);
		} else if (extension.type() == ExtensionType.STATUS_REQUEST) {
			encode((StatusRequest) extension, buffer);
		} else if (extension.type() == ExtensionType.SUPPORTED_GROUPS) {
			encode((SupportedGroups) extension, buffer);
		} else if (extension.type() == ExtensionType.SIGNATURE_ALGORITHMS) {
			encode((SignatureAlgorithms) extension, buffer);
		} else if (extension.type() == ExtensionType.USE_SRTP) {
			encode((UseSRTP) extension, buffer);
		} else if (extension.type() == ExtensionType.HEARTBEAT) {
			encode((Heartbeat) extension, buffer);
		} else if (extension.type() == ExtensionType.APPLICATION_LAYER_PROTOCOL_NEGOTIATION) {
			encode((ApplicationLayerProtocolNegotiation) extension, buffer);
		} else if (extension.type() == ExtensionType.SIGNED_CERTIFICATE_TIMESTAMP) {
			encode((SignedCertificateTimestamp) extension, buffer);
		} else if (extension.type() == ExtensionType.CLIENT_CERTIFICATE_TYPE) {
			encode((ClientCertificateType) extension, buffer);
		} else if (extension.type() == ExtensionType.SERVER_CERTIFICATE_TYPE) {
			encode((ServerCertificateType) extension, buffer);
		} else if (extension.type() == ExtensionType.PADDING) {
			encode((Padding) extension, buffer);
		} else if (extension.type() == ExtensionType.PRE_SHARED_KEY) {
			encode(type, (PreSharedKey) extension, buffer);
		} else if (extension.type() == ExtensionType.EARLY_DATA) {
			encode((EarlyData) extension, buffer);
		} else if (extension.type() == ExtensionType.SUPPORTED_VERSIONS) {
			encode((SupportedVersions) extension, buffer);
		} else if (extension.type() == ExtensionType.COOKIE) {
			encode((Cookie) extension, buffer);
		} else if (extension.type() == ExtensionType.PSK_KEY_EXCHANGE_MODES) {
			encode((PskKeyExchangeModes) extension, buffer);
		} else if (extension.type() == ExtensionType.CERTIFICATE_AUTHORITIES) {
			encode((CertificateAuthorities) extension, buffer);
		} else if (extension.type() == ExtensionType.OID_FILTERS) {
			encode((OIDFilters) extension, buffer);
		} else if (extension.type() == ExtensionType.POST_HANDSHAKE_AUTH) {
			encode((PostHandshakeAuth) extension, buffer);
		} else if (extension.type() == ExtensionType.SIGNATURE_ALGORITHMS_CERT) {
			encode((SignatureAlgorithmsCert) extension, buffer);
		} else if (extension.type() == ExtensionType.KEY_SHARE) {
			encode((KeyShare) extension, buffer);
		} else {
			encode((Reserved) extension, buffer);
		}
		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length << 8));
		buffer.set(position, (byte) length);
	}

	public static void decode(HandshakeExtensions extensions, DataBuffer buffer) throws IOException {
		// Length 2Byte
		int length = buffer.readUnsignedShort();
		// 计算扩展字段全部解码后的剩余字节
		length = buffer.readable() - length;
		while (buffer.readable() > length) {
			extensions.getExtensions().add(decode(extensions.getMsgType(), buffer));
		}
	}

	public static Extension decode(HandshakeType hype, DataBuffer buffer) throws IOException {
		// Type 2Byte
		final short type = buffer.readShort();
		// opaque extension_data<0..2^16-1>;
		final int length = buffer.readUnsignedShort();
		// SUB Extension
		if (type == ExtensionType.SERVER_NAME.code()) {
			return decodeServerNames(buffer);
		} else if (type == ExtensionType.MAX_FRAGMENT_LENGTH.code()) {
			return decodeMaxFragmentLength(buffer);
		} else if (type == ExtensionType.CLIENT_CERTIFICATE_URL.code()) {
			return decodeClientCertificateURL(buffer);
		} else if (type == ExtensionType.TRUSTED_CA_KEYS.code()) {
			return decodeTrustedCAKeys(buffer);
		} else if (type == ExtensionType.TRUNCATED_HMAC.code()) {
			return decodeTruncatedHMAC(buffer);
		} else if (type == ExtensionType.STATUS_REQUEST.code()) {
			return decodeStatusRequest(buffer, length);
		} else if (type == ExtensionType.SUPPORTED_GROUPS.code()) {
			return decodeSupportedGroups(buffer);
		} else if (type == ExtensionType.SIGNATURE_ALGORITHMS.code()) {
			return decodeSignatureAlgorithms(buffer);
		} else if (type == ExtensionType.USE_SRTP.code()) {
			return decodeUseSrtp(buffer);
		} else if (type == ExtensionType.HEARTBEAT.code()) {
			return decodeHeartbeat(buffer);
		} else if (type == ExtensionType.APPLICATION_LAYER_PROTOCOL_NEGOTIATION.code()) {
			return decodeApplicationLayerProtocolNegotiation(buffer);
		} else if (type == ExtensionType.SIGNED_CERTIFICATE_TIMESTAMP.code()) {
			return decodeSignedCertificateTimestamp(buffer);
		} else if (type == ExtensionType.CLIENT_CERTIFICATE_TYPE.code()) {
			return decodeClientCertificateType(buffer, length);
		} else if (type == ExtensionType.SERVER_CERTIFICATE_TYPE.code()) {
			return decodeServerCertificateType(buffer, length);
		} else if (type == ExtensionType.PADDING.code()) {
			return decodePadding(buffer);
		} else if (type == ExtensionType.PRE_SHARED_KEY.code()) {
			return decodePreSharedKey(hype, buffer);
		} else if (type == ExtensionType.EARLY_DATA.code()) {
			return decodeEarlyData(hype, buffer);
		} else if (type == ExtensionType.SUPPORTED_VERSIONS.code()) {
			return decodeSupportedVersions(buffer);
		} else if (type == ExtensionType.COOKIE.code()) {
			return decodeCookie(buffer);
		} else if (type == ExtensionType.PSK_KEY_EXCHANGE_MODES.code()) {
			return decodePskKeyExchangeModes(buffer);
		} else if (type == ExtensionType.CERTIFICATE_AUTHORITIES.code()) {
			return decodeCertificateAuthorities(buffer);
		} else if (type == ExtensionType.OID_FILTERS.code()) {
			return decodeOIDFilters(buffer);
		} else if (type == ExtensionType.POST_HANDSHAKE_AUTH.code()) {
			return decodePostHandshakeAuth(buffer);
		} else if (type == ExtensionType.SIGNATURE_ALGORITHMS_CERT.code()) {
			return decodeSignatureAlgorithmsCert(buffer);
		} else if (type == ExtensionType.KEY_SHARE.code()) {
			return decodeKeyShare(buffer);
		} else {
			decodeReserved(buffer);
		}
		return null;
	}

	/** RFC 6066 */
	private static void encode(ServerNames extension, DataBuffer buffer) throws IOException {
		ServerName item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeByte(item.type().code());
			buffer.writeShort(item.getName().length);
			buffer.write(item.getName());
		}
	}

	/** RFC 6066 */
	private static void encode(MaxFragmentLength extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getType().code());
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
			buffer.writeByte(item.type().code());
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
			buffer.writeByte(extension.getRequest().type().code());
			if (extension.getRequest().type() == CertificateStatusType.OCSP) {
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
			buffer.writeShort(extension.get(index).code());
		}
	}

	/** RFC 8446 */
	private static void encode(SignatureAlgorithms extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.size() * 2);
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).code());
		}
	}

	/** RFC 5764 */
	private static void encode(UseSRTP extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).code());
		}
		buffer.writeShort(extension.getMKI().length);
		buffer.write(extension.getMKI());
	}

	/** RFC 6520 */
	private static void encode(Heartbeat extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getMode().code());
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
		buffer.set(position++, (byte) (length << 8));
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
			buffer.set(position++, (byte) (length << 8));
			buffer.set(position, (byte) length);
		}
	}

	/** RFC 7250 */
	private static void encode(ClientCertificateType extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index).code());
		}
		// if (client) {
		// buffer.writeShort(extension.size());
		// for (int index = 0; index < extension.size(); index++) {
		// buffer.writeByte(extension.get(index).code());
		// }
		// } else {
		// buffer.writeByte(extension.get(0).code());
		// }
	}

	/** RFC 7250 */
	private static void encode(ServerCertificateType extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index).code());
		}
		// if (client) {
		// buffer.writeShort(extension.size());
		// for (int index = 0; index < extension.size(); index++) {
		// buffer.writeByte(extension.get(index).code());
		// }
		// } else {
		// buffer.writeByte(extension.get(0).code());
		// }
	}

	/** RFC 7685 */
	private static void encode(Padding extension, DataBuffer buffer) throws IOException {
		int size = extension.getSiez() - 4;
		buffer.writeShort(size);
		while (size-- > 0) {
			buffer.writeByte(0);
		}
	}

	/** RFC 8446 */
	private static void encode(KeyShare extension, DataBuffer buffer) throws IOException {
		// KeyShareEntry Length 2Byte
		int position = buffer.readable();
		buffer.writeShort(0);
		KeyShareEntry item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.group().code());
			buffer.writeShort(item.getKeyExchange().length);
			buffer.write(item.getKeyExchange());
		}
		// SET Length
		int length = buffer.readable() - position - 2;
		buffer.set(position++, (byte) (length << 8));
		buffer.set(position, (byte) length);
	}

	/** RFC 8446 */
	private static void encode(HandshakeType type, PreSharedKey extension, DataBuffer buffer) throws IOException {
		if (type == HandshakeType.CLIENT_HELLO) {
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
		} else if (type == HandshakeType.SERVER_HELLO) {
			buffer.writeShort(extension.getSelected());
		}
	}

	/** RFC 8446 */
	private static void encode(PskKeyExchangeModes extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size());
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index).code());
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
			buffer.writeShort(extension.get(index).code());
		}
	}

	private static void encode(Reserved extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getType());
		buffer.writeShort(extension.getData().length);
		buffer.write(extension.getData());
	}

	private static Reserved decodeReserved(DataBuffer buffer) {
		final Reserved extension = new Reserved();
		return extension;
	}

	/** RFC 6066 */
	private static ServerNames decodeServerNames(DataBuffer buffer) throws IOException {
		final ServerNames extension = new ServerNames();
		NameType type;
		byte[] name;
		while (buffer.readable() > 3) {
			type = NameType.code(buffer.readUnsignedByte());
			buffer.readFully(name = new byte[buffer.readUnsignedShort()]);
			extension.add(new ServerName(type, name));
		}
		return extension;
	}

	/** RFC 6066 */
	private static MaxFragmentLength decodeMaxFragmentLength(DataBuffer buffer) throws IOException {
		final int code = buffer.readUnsignedByte();
		return new MaxFragmentLength(MaxFragmentType.code(code));
	}

	/** RFC 6066 */
	private static ClientCertificateURL decodeClientCertificateURL(DataBuffer buffer) throws IOException {
		return ClientCertificateURL.INSTANCE;
	}

	/** RFC 6066 */
	private static TrustedAuthorities decodeTrustedCAKeys(DataBuffer buffer) throws IOException {
		final TrustedAuthorities extension = new TrustedAuthorities();
		IdentifierType type;
		byte[] data;
		while (buffer.readable() > 3) {
			type = IdentifierType.code(buffer.readUnsignedByte());
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
		final CertificateStatusType type = CertificateStatusType.code(buffer.readByte());
		if (type == CertificateStatusType.OCSP) {
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
			extension.add(NamedGroup.code(buffer.readShort()));
		}
		return extension;
	}

	/** RFC 8446 */
	private static SignatureAlgorithms decodeSignatureAlgorithms(DataBuffer buffer) throws IOException {
		final SignatureAlgorithms extension = new SignatureAlgorithms();
		int size = buffer.readShort() / 2;
		while (size-- > 0) {
			extension.add(SignatureScheme.code(buffer.readShort()));
		}
		return extension;
	}

	/** RFC 5764 */
	private static UseSRTP decodeUseSrtp(DataBuffer buffer) throws IOException {
		final UseSRTP extension = new UseSRTP();
		while (buffer.readable() > 1) {
			extension.add(SRTPProtectionProfile.code(buffer.readUnsignedShort()));
		}
		final byte[] opaque = new byte[buffer.readUnsignedShort()];
		buffer.readFully(opaque);
		extension.setMKI(opaque);
		return extension;
	}

	/** RFC 6520 */
	private static Heartbeat decodeHeartbeat(DataBuffer buffer) throws IOException {
		final Heartbeat extension = new Heartbeat();
		extension.setMode(HeartbeatMode.code(buffer.readUnsignedShort()));
		return extension;
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
			extension.add(CertificateType.code(buffer.readUnsignedByte()));
		}
		// if (client) {
		// int size = buffer.readUnsignedShort();
		// while (size-- > 0) {
		// extension.add(CertificateType.code(buffer.readUnsignedByte()));
		// }
		// } else {
		// extension.set(CertificateType.code(buffer.readUnsignedByte()));
		// }
		return extension;
	}

	/** RFC 7250 */
	private static ServerCertificateType decodeServerCertificateType(DataBuffer buffer, int length) throws IOException {
		final ServerCertificateType extension = new ServerCertificateType();
		while (length-- > 0) {
			extension.add(CertificateType.code(buffer.readUnsignedByte()));
		}
		// if (client) {
		// int size = buffer.readUnsignedShort();
		// while (size-- > 0) {
		// extension.add(CertificateType.code(buffer.readUnsignedByte()));
		// }
		// } else {
		// extension.set(CertificateType.code(buffer.readUnsignedByte()));
		// }
		return extension;
	}

	/** RFC 7685 */
	private static Padding decodePadding(DataBuffer buffer) throws IOException {
		final Padding extension = new Padding();
		extension.setSiez(buffer.readUnsignedShort());
		buffer.skipBytes(extension.getSiez());
		extension.setSiez(extension.getSiez() + 4);
		return extension;
	}

	/** RFC 8446 */
	private static KeyShare decodeKeyShare(DataBuffer buffer) throws IOException {
		final KeyShare extension = new KeyShare();
		int length = buffer.readShort();
		if (length > 0) {
			NamedGroup group;
			byte[] keyExchange;
			while (length > 0) {
				group = NamedGroup.code(buffer.readShort());
				buffer.readFully(keyExchange = new byte[buffer.readShort()]);
				extension.add(new KeyShareEntry(group, keyExchange));
				length -= keyExchange.length + 2;
			}
		}
		return extension;
	}

	/** RFC 8446 */
	private static PreSharedKey decodePreSharedKey(HandshakeType type, DataBuffer buffer) throws IOException {
		final PreSharedKey extension = new PreSharedKey();
		if (type == HandshakeType.CLIENT_HELLO) {
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
		} else if (type == HandshakeType.SERVER_HELLO) {
			extension.setSelected(buffer.readShort());
		}
		return extension;
	}

	/** RFC 8446 */
	private static PskKeyExchangeModes decodePskKeyExchangeModes(DataBuffer buffer) throws IOException {
		final PskKeyExchangeModes extension = new PskKeyExchangeModes();
		int size = buffer.readByte();
		while (size-- > 0) {
			extension.add(PskKeyExchangeMode.code(buffer.readByte()));
		}
		return extension;
	}

	/** RFC 8446 */
	private static EarlyData decodeEarlyData(HandshakeType type, DataBuffer buffer) throws IOException {
		if (type == HandshakeType.NEW_SESSION_TICKET) {
			final EarlyData extension = new EarlyData();
			extension.setMaxSize(buffer.readInt());
			return extension;
		}
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
			extension.add(SignatureScheme.code(buffer.readUnsignedShort()));
		}
		return extension;
	}
}