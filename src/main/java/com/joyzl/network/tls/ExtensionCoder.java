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

	/** RFC 6066 */
	public static void encode(ServerNames extension, DataBuffer buffer) throws IOException {
		ServerName item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeByte(item.type().code());
			buffer.writeShort(item.getName().length);
			buffer.write(item.getName());
		}
	}

	/** RFC 6066 */
	public static void encode(MaxFragmentLength extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getType().code());
	}

	/** RFC 6066 */
	public static void encode(ClientCertificateUrl extension, DataBuffer buffer) throws IOException {
		// EMPTY
	}

	/** RFC 6066 */
	public static void encode(TrustedAuthorities extension, DataBuffer buffer) throws IOException {
		TrustedAuthority item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeByte(item.type().code());
			buffer.writeShort(item.getData().length);
			buffer.write(item.getData());
		}
	}

	/** RFC 6066 */
	public static void encode(TruncatedHMAC extension, DataBuffer buffer) throws IOException {
		// EMPTY
	}

	/** RFC 6066 */
	public static void encode(StatusRequest extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getRequest().type().code());
		if (extension.getRequest().type() == CertificateStatusType.OCSP) {
			final OCSPStatusRequest request = (OCSPStatusRequest) extension.getRequest();
			buffer.writeShort(request.getResponderID().length);
			buffer.write(request.getResponderID());
			buffer.writeShort(request.getExtensions().length);
			buffer.write(request.getExtensions());
		}
	}

	/** RFC 7919 */
	public static void encode(SupportedGroups extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).code());
		}
	}

	/** RFC 8446 */
	public static void encode(SignatureAlgorithms extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).code());
		}
	}

	/** RFC 5764 */
	public static void encode(UseSRTP extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).code());
		}
		buffer.writeShort(extension.getMKI().length);
		buffer.write(extension.getMKI());
	}

	/** RFC 6520 */
	public static void encode(Heartbeat extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getMode().code());
	}

	/** RFC 7301 */
	public static void encode(ApplicationLayerProtocolNegotiation extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).length);
			buffer.write(extension.get(index));
		}
	}

	/** RFC 6962 */
	public static void encode(SignedCertificateTimestamp extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).length);
			buffer.write(extension.get(index));
		}
	}

	/** RFC 7250 */
	public static void encode(ClientCertificateType extension, DataBuffer buffer, boolean client) throws IOException {
		if (client) {
			buffer.writeShort(extension.size());
			for (int index = 0; index < extension.size(); index++) {
				buffer.writeByte(extension.get(index).code());
			}
		} else {
			buffer.writeByte(extension.get(0).code());
		}
	}

	/** RFC 7250 */
	public static void encode(ServerCertificateType extension, DataBuffer buffer, boolean client) throws IOException {
		if (client) {
			buffer.writeShort(extension.size());
			for (int index = 0; index < extension.size(); index++) {
				buffer.writeByte(extension.get(index).code());
			}
		} else {
			buffer.writeByte(extension.get(0).code());
		}
	}

	/** RFC 7685 */
	public static void encode(Padding extension, DataBuffer buffer) throws IOException {
		int size = extension.getSiez() - 4;
		buffer.writeShort(size);
		while (size-- > 0) {
			buffer.writeByte(0);
		}
	}

	/** RFC 8446 */
	public static void encode(KeyShare extension, DataBuffer buffer) throws IOException {
		KeyShareEntry item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.group().code());
			buffer.writeShort(item.getKeyExchange().length);
			buffer.write(item.getKeyExchange());
		}
	}

	/** RFC 8446 */
	public static void encode(PreSharedKey extension, HandshakeType type, DataBuffer buffer) throws IOException {
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
	public static void encode(PskKeyExchangeModes extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index).code());
		}
	}

	/** RFC 8446 */
	public static void encode(EarlyData extension, DataBuffer buffer) throws IOException {
		if (extension.getMaxSize() > 0) {
			buffer.writeInt(extension.getMaxSize());
		}
	}

	/** RFC 8446 */
	public static void encode(Cookie extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getCookie().length);
		buffer.write(extension.getCookie());
	}

	/** RFC 8446 */
	public static void encode(SupportedVersions extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index));
		}
	}

	/** RFC 8446 */
	public static void encode(CertificateAuthorities extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).length);
			buffer.write(extension.get(index));
		}
	}

	/** RFC 8446 */
	public static void encode(OIDFilters extension, DataBuffer buffer) throws IOException {
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
	public static void encode(PostHandshakeAuth extension, DataBuffer buffer) throws IOException {
		// EMPTY
	}

	/** RFC 8446 */
	public static void encode(SignatureAlgorithmsCert extension, DataBuffer buffer) throws IOException {
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeShort(extension.get(index).code());
		}
	}

	/** RFC 6066 */
	public static ServerNames decodeServerName(DataBuffer buffer) throws IOException {
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
	public static MaxFragmentLength decodeMaxFragmentLength(DataBuffer buffer) throws IOException {
		final int code = buffer.readUnsignedByte();
		return new MaxFragmentLength(MaxFragmentType.code(code));
	}

	/** RFC 6066 */
	public static ClientCertificateUrl decodeClientCertificateUrl(DataBuffer buffer) throws IOException {
		return ClientCertificateUrl.INSTANCE;
	}

	/** RFC 6066 */
	public static TrustedAuthorities decodeTrustedCAKeys(DataBuffer buffer) throws IOException {
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
	public static TruncatedHMAC decodeTruncatedHMAC(DataBuffer buffer) throws IOException {
		return TruncatedHMAC.INSTANCE;
	}

	/** RFC 6066 */
	public static StatusRequest decodeStatusRequest(DataBuffer buffer) throws IOException {
		final StatusRequest extension = new StatusRequest();
		final CertificateStatusType type = CertificateStatusType.code(buffer.readUnsignedByte());
		if (type == CertificateStatusType.OCSP) {
			final OCSPStatusRequest request = new OCSPStatusRequest();
			byte[] opaque;
			buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
			request.setResponderID(opaque);
			buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
			request.setResponderID(opaque);
		}
		return extension;
	}

	/** RFC 7919 */
	public static SupportedGroups decodeSupportedGroups(DataBuffer buffer) throws IOException {
		final SupportedGroups extension = new SupportedGroups();
		while (buffer.readable() > 1) {
			extension.add(NamedGroup.code(buffer.readUnsignedShort()));
		}
		return extension;
	}

	/** RFC 8446 */
	public static SignatureAlgorithms decodeSignatureAlgorithms(DataBuffer buffer) throws IOException {
		final SignatureAlgorithms extension = new SignatureAlgorithms();
		while (buffer.readable() > 1) {
			extension.add(SignatureScheme.code(buffer.readUnsignedShort()));
		}
		return extension;
	}

	/** RFC 5764 */
	public static UseSRTP decodeUseSrtp(DataBuffer buffer) throws IOException {
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
	public static Heartbeat decodeHeartbeat(DataBuffer buffer) throws IOException {
		final Heartbeat extension = new Heartbeat();
		extension.setMode(HeartbeatMode.code(buffer.readUnsignedShort()));
		return extension;
	}

	/** RFC 7301 */
	public static ApplicationLayerProtocolNegotiation decodeApplicationLayerProtocolNegotiation(DataBuffer buffer) throws IOException {
		final ApplicationLayerProtocolNegotiation extension = new ApplicationLayerProtocolNegotiation();
		byte[] opaque;
		while (buffer.readable() > 2) {
			buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
			extension.add(opaque);
		}
		return extension;
	}

	/** RFC 6962 */
	public static SignedCertificateTimestamp decodeSignedCertificateTimestamp(DataBuffer buffer) throws IOException {
		final SignedCertificateTimestamp extension = new SignedCertificateTimestamp();
		byte[] opaque;
		while (buffer.readable() > 2) {
			buffer.readFully(opaque = new byte[buffer.readUnsignedShort()]);
			extension.add(opaque);
		}
		return extension;
	}

	/** RFC 7250 */
	public static ClientCertificateType decodeClientCertificateType(DataBuffer buffer, boolean client) throws IOException {
		final ClientCertificateType extension = new ClientCertificateType();
		if (client) {
			int size = buffer.readUnsignedShort();
			while (size-- > 0) {
				extension.add(CertificateType.code(buffer.readUnsignedByte()));
			}
		} else {
			extension.set(CertificateType.code(buffer.readUnsignedByte()));
		}
		return extension;
	}

	/** RFC 7250 */
	public static ServerCertificateType decodeServerCertificateType(DataBuffer buffer, boolean client) throws IOException {
		final ServerCertificateType extension = new ServerCertificateType();
		if (client) {
			int size = buffer.readUnsignedShort();
			while (size-- > 0) {
				extension.add(CertificateType.code(buffer.readUnsignedByte()));
			}
		} else {
			extension.set(CertificateType.code(buffer.readUnsignedByte()));
		}
		return extension;
	}

	/** RFC 7685 */
	public static Padding decodePadding(DataBuffer buffer) throws IOException {
		final Padding extension = new Padding();
		extension.setSiez(buffer.readUnsignedShort());
		buffer.skipBytes(extension.getSiez());
		extension.setSiez(extension.getSiez() + 4);
		return extension;
	}

	/** RFC 8446 */
	public static KeyShare decodeKeyShare(DataBuffer buffer) throws IOException {
		final KeyShare extension = new KeyShare();
		NamedGroup group;
		byte[] keyExchange;
		while (buffer.readable() > 2) {
			group = NamedGroup.code(buffer.readUnsignedShort());
			buffer.readFully(keyExchange = new byte[buffer.readUnsignedShort()]);
			extension.add(new KeyShareEntry(group, keyExchange));
		}
		return extension;
	}

	/** RFC 8446 */
	public static PreSharedKey decodePreSharedKey(HandshakeType type, DataBuffer buffer) throws IOException {
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
	public static PskKeyExchangeModes decodePskKeyExchangeModes(DataBuffer buffer) throws IOException {
		final PskKeyExchangeModes extension = new PskKeyExchangeModes();
		while (buffer.readable() > 1) {
			extension.add(PskKeyExchangeMode.code(buffer.readUnsignedByte()));
		}
		return extension;
	}

	/** RFC 8446 */
	public static EarlyData decodeEarlyData(HandshakeType type, DataBuffer buffer) throws IOException {
		if (type == HandshakeType.NEW_SESSION_TICKET) {
			final EarlyData extension = new EarlyData();
			extension.setMaxSize(buffer.readInt());
			return extension;
		}
		return EarlyData.EMPTY;
	}

	/** RFC 8446 */
	public static Cookie decodeCookie(DataBuffer buffer) throws IOException {
		final Cookie extension = new Cookie();
		final byte[] value = new byte[buffer.readUnsignedShort()];
		buffer.readFully(value);
		extension.setCookie(value);
		return extension;
	}

	/** RFC 8446 */
	public static SupportedVersions decodeSupportedVersions(DataBuffer buffer) throws IOException {
		final SupportedVersions extension = new SupportedVersions();
		while (buffer.readable() > 1) {
			extension.add(buffer.readShort());
		}
		return extension;
	}

	/** RFC 8446 */
	public static CertificateAuthorities decodeCertificateAuthorities(DataBuffer buffer) throws IOException {
		final CertificateAuthorities extension = new CertificateAuthorities();
		byte[] dname;
		while (buffer.readable() > 0) {
			buffer.readFully(dname = new byte[buffer.readUnsignedShort()]);
			extension.add(dname);
		}
		return extension;
	}

	/** RFC 8446 */
	public static OIDFilters decodeOIDFilters(DataBuffer buffer) throws IOException {
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
	public static PostHandshakeAuth decodePostHandshakeAuth(DataBuffer buffer) throws IOException {
		return PostHandshakeAuth.INSTANCE;
	}

	/** RFC 8446 */
	public static SignatureAlgorithmsCert decodeSignatureAlgorithmsCert(DataBuffer buffer) throws IOException {
		final SignatureAlgorithmsCert extension = new SignatureAlgorithmsCert();
		while (buffer.readable() > 1) {
			extension.add(SignatureScheme.code(buffer.readUnsignedShort()));
		}
		return extension;
	}
}