package com.joyzl.network.tls;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

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

	public static void encode(Extensions extensions, DataBuffer buffer) throws IOException {
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
			} else if (extension.type() == Extension.EXTENDED_MASTER_SECRET) {
				encode((ExtendedMasterSecret) extension, buffer);
			} else if (extension.type() == Extension.COMPRESS_CERTIFICATE) {
				encode((CompressCertificate) extension, buffer);
			} else if (extension.type() == Extension.SESSION_TICKET) {
				encode((SessionTicket) extension, buffer);
			} else if (extension.type() == Extension.PRE_SHARED_KEY) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					encode((PreSharedKeySelected) extension, buffer);
				} else if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((OfferedPsks) extension, buffer);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (extension.type() == Extension.EARLY_DATA) {
				encode((EarlyDataIndication) extension, buffer);
			} else if (extension.type() == Extension.SUPPORTED_VERSIONS) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					encode((SupportedVersions) extension, buffer, true);
				} else if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((SupportedVersions) extension, buffer, false);
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
				encode((PostHandshakeAuth) extension, buffer);
			} else if (extension.type() == Extension.SIGNATURE_ALGORITHMS_CERT) {
				encode((SignatureAlgorithmsCert) extension, buffer);
			} else if (extension.type() == Extension.KEY_SHARE) {
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					encode((KeyShareClientHello) extension, buffer);
				} else if (extensions.msgType() == Handshake.SERVER_HELLO) {
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

	public static void decode(Extensions extensions, DataBuffer buffer) throws IOException {
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
				extension = decodeClientCertificateURL(buffer, size);
			} else if (type == Extension.TRUSTED_CA_KEYS) {
				extension = decodeTrustedCAKeys(buffer, size);
			} else if (type == Extension.TRUNCATED_HMAC) {
				extension = decodeTruncatedHMAC(buffer, size);
			} else if (type == Extension.STATUS_REQUEST) {
				extension = decodeStatusRequest(buffer, size);
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
				extension = decodeClientCertificateType(buffer, size);
			} else if (type == Extension.SERVER_CERTIFICATE_TYPE) {
				extension = decodeServerCertificateType(buffer, size);
			} else if (type == Extension.EXTENDED_MASTER_SECRET) {
				extension = decodeExtendedMasterSecret(buffer, size);
			} else if (type == Extension.COMPRESS_CERTIFICATE) {
				extension = decodeCompressCertificate(buffer, size);
			} else if (type == Extension.SESSION_TICKET) {
				extension = decodeSessionTicket(buffer, size);
			} else if (type == Extension.PRE_SHARED_KEY) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					extension = decodePreSharedKeySelected(buffer, size);
				} else if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					extension = decodeOfferedPsks(buffer, size);
				} else {
					throw new IOException("TLS:混乱的扩展");
				}
			} else if (type == Extension.EARLY_DATA) {
				extension = decodeEarlyDataIndication(buffer, size);
			} else if (type == Extension.SUPPORTED_VERSIONS) {
				if (extensions.msgType() == Handshake.SERVER_HELLO) {
					extension = decodeSelectedVersion(buffer, size);
				} else if (extensions.msgType() == Handshake.CLIENT_HELLO) {
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
				extension = decodePostHandshakeAuth(buffer, size);
			} else if (type == Extension.SIGNATURE_ALGORITHMS_CERT) {
				extension = decodeSignatureAlgorithmsCert(buffer, size);
			} else if (type == Extension.KEY_SHARE) {
				if (extensions.msgType() == Handshake.CLIENT_HELLO) {
					extension = decodeKeyShareClientHello(buffer, size);
				} else if (extensions.msgType() == Handshake.SERVER_HELLO) {
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
			} else if (type == Extension.RENEGOTIATION_INFO) {
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

	/** RFC 6066 */
	private static void encode(ServerNames serverNames, DataBuffer buffer) throws IOException {
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
	private static ServerNames decodeServerNames(DataBuffer buffer, int length) throws IOException {
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

	/** RFC 5077 */
	private static void encode(SessionTicket extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getTicket().length);
		buffer.write(extension.getTicket());
	}

	/** RFC 8446 */
	private static void encode(KeyShareClientHello extension, DataBuffer buffer) throws IOException {
		// 计算长度
		int length = 0;
		KeyShareEntry item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			length += item.getKeyExchange().length;
			length += 4;
		}
		// Length 2Byte
		buffer.writeShort(length);
		// client_shares
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.getGroup());
			buffer.writeShort(item.getKeyExchange().length);
			buffer.write(item.getKeyExchange());
		}
	}

	/** RFC 8446 */
	private static void encode(KeyShareServerHello extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getServerShare().getGroup());
		buffer.writeShort(extension.getServerShare().getKeyExchange().length);
		buffer.write(extension.getServerShare().getKeyExchange());
	}

	/** RFC 8446 */
	private static void encode(KeyShareHelloRetryRequest extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getSelectedGroup());
	}

	/** RFC 8446 */
	private static KeyShare decodeKeyShareClientHello(DataBuffer buffer, int length) throws IOException {
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
	private static KeyShare decodeKeyShareServerHello(DataBuffer buffer, int length) throws IOException {
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
	private static KeyShare decodeKeyShareHelloRetryRequest(DataBuffer buffer, int length) throws IOException {
		final KeyShareHelloRetryRequest selected = new KeyShareHelloRetryRequest();
		if (length > 0) {
			selected.setSelectedGroup(buffer.readShort());
		}
		return selected;
	}

	/** RFC 5746 */
	private static void encode(RenegotiationInfo extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.getValue().length);
		buffer.write(extension.getValue());
	}

	/** RFC 8446 client_hello */
	private static void encode(OfferedPsks extension, DataBuffer buffer) throws IOException {
		// PskIdentity identities<7..2^16-1>;
		int length = 0;
		PskIdentity item;
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			length += item.getIdentity().length;
			length += 6;
		}
		// list Length 2Byte
		buffer.writeShort(length);
		for (int index = 0; index < extension.size(); index++) {
			item = extension.get(index);
			buffer.writeShort(item.getIdentity().length);
			buffer.write(item.getIdentity());
			buffer.writeInt(item.getTicketAge());
		}

		// PskBinderEntry binders<33..2^16-1>;
		// list Length 2Byte
		buffer.writeShort(extension.bindersLength());
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.getHashLength());
			for (length = 0; length < extension.getHashLength(); length++) {
				buffer.writeByte(0);
			}
		}
	}

	/** RFC 8446 client_hello PskBinderEntry */
	public static void encodeBinders(OfferedPsks extension, DataBuffer buffer) throws IOException {
		PskIdentity binder;
		buffer.writeShort(extension.bindersLength());
		for (int index = 0; index < extension.size(); index++) {
			binder = extension.get(index);
			buffer.writeByte(extension.getHashLength());
			buffer.write(binder.getBinder());
		}
	}

	/** RFC 8446 server_hello */
	private static void encode(PreSharedKeySelected extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getSelected());
	}

	/** RFC 8446 client_hello */
	private static PreSharedKey decodeOfferedPsks(DataBuffer buffer, int length) throws IOException {
		final OfferedPsks psks = new OfferedPsks();
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
	private static PreSharedKey decodePreSharedKeySelected(DataBuffer buffer, int length) throws IOException {
		final PreSharedKeySelected selected = new PreSharedKeySelected();
		if (length > 0) {
			// uint16 selected_identity_index;
			selected.setSelected(buffer.readShort());
		}
		return selected;
	}

	/** RFC 8446 */
	private static void encode(PskKeyExchangeModes extension, DataBuffer buffer) throws IOException {
		buffer.writeByte(extension.size());
		for (int index = 0; index < extension.size(); index++) {
			buffer.writeByte(extension.get(index));
		}
	}

	/** RFC 8446 */
	private static void encode(EarlyDataIndication extension, DataBuffer buffer) throws IOException {
		if (extension.getMaxSize() > 0) {
			buffer.writeInt(extension.getMaxSize());
		} else {
			// EMPTY
		}
	}

	/** RFC 8446 */
	private static void encode(Cookie extension, DataBuffer buffer) throws IOException {
		buffer.writeShort(extension.getCookie().length);
		buffer.write(extension.getCookie());
	}

	/** RFC 8446 */
	private static void encode(SupportedVersions extension, DataBuffer buffer, boolean selected) throws IOException {
		if (selected) {
			buffer.writeShort(extension.get(0));
		} else {
			buffer.writeByte(extension.size() * 2);
			for (int index = 0; index < extension.size(); index++) {
				buffer.writeShort(extension.get(index));
			}
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
		// OIDFilter filters<0..2^16-1>;
		int position = buffer.readable();
		buffer.writeShort(0);

		int p, length;
		OIDFilter filter;
		for (int i = 0; i < extension.size(); i++) {
			filter = extension.get(i);

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
	private static OIDFilters decodeOIDFilters(DataBuffer buffer, int length) throws IOException {
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

	/** RFC 6066 */
	private static MaxFragmentLength decodeMaxFragmentLength(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			return new MaxFragmentLength(buffer.readByte());
		} else {
			return new MaxFragmentLength((byte) 0);
		}
	}

	/** RFC 6066 */
	private static ClientCertificateURL decodeClientCertificateURL(DataBuffer buffer, int length) throws IOException {
		return ClientCertificateURL.INSTANCE;
	}

	/** RFC 6066 */
	private static TrustedAuthorities decodeTrustedCAKeys(DataBuffer buffer, int length) throws IOException {
		// TODO
		final TrustedAuthorities tas = new TrustedAuthorities();
		if (length > 0) {
			byte type;
			byte[] data;
			while (buffer.readable() > 3) {
				type = buffer.readByte();
				buffer.readFully(data = new byte[buffer.readUnsignedShort()]);
				tas.add(new TrustedAuthority(type, data));
			}

		}
		return tas;
	}

	/** RFC 6066 */
	private static TruncatedHMAC decodeTruncatedHMAC(DataBuffer buffer, int length) throws IOException {
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
	private static SupportedGroups decodeSupportedGroups(DataBuffer buffer, int length) throws IOException {
		final SupportedGroups groups = new SupportedGroups();
		if (length > 0) {
			length = buffer.readShort() / 2;
			while (length-- > 0) {
				groups.add(buffer.readShort());
			}
		}
		return groups;
	}

	/** RFC 8442 */
	private static ECPointFormats decodeECPointFormats(DataBuffer buffer, int length) throws IOException {
		final ECPointFormats formats = new ECPointFormats();
		if (length > 0) {
			length = buffer.readByte();
			while (length-- > 0) {
				formats.add(buffer.readByte());
			}
		}
		return formats;
	}

	/** RFC 8446 */
	private static SignatureAlgorithms decodeSignatureAlgorithms(DataBuffer buffer, int length) throws IOException {
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
	private static UseSRTP decodeUseSRTP(DataBuffer buffer, int length) throws IOException {
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
	private static Heartbeat decodeHeartbeat(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			return new Heartbeat(buffer.readByte());
		} else {
			return new Heartbeat();
		}
	}

	/** RFC 7301 */
	private static ApplicationLayerProtocolNegotiation decodeApplicationLayerProtocolNegotiation(DataBuffer buffer, int length) throws IOException {
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
	private static SignedCertificateTimestamp decodeSignedCertificateTimestamp(DataBuffer buffer, int length) throws IOException {
		final SignedCertificateTimestamp timestamp = new SignedCertificateTimestamp();
		if (length > 0) {
			// SerializedSCT sct_list <1..2^16-1>;
			length = buffer.readShort();
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
	private static ClientCertificateType decodeClientCertificateType(DataBuffer buffer, int length) throws IOException {
		// TODO
		final ClientCertificateType types = new ClientCertificateType();
		if (length > 0) {

			// CertificateType client_certificate_types<1..2^8-1>;
			length = buffer.readUnsignedByte();
			while (length-- > 0) {
				types.add(buffer.readByte());
			}
		}
		return types;
	}

	/** RFC 7250 */
	private static ServerCertificateType decodeServerCertificateType(DataBuffer buffer, int length) throws IOException {
		// TODO
		final ServerCertificateType type = new ServerCertificateType();
		if (length > 0) {
			// CertificateType server_certificate_type;
			type.add(buffer.readByte());
		}
		return type;
	}

	/** RFC 7627 */
	private static ExtendedMasterSecret decodeExtendedMasterSecret(DataBuffer buffer, int length) throws IOException {
		return ExtendedMasterSecret.INSTANCE;
	}

	/** RFC 8879 */
	private static CompressCertificate decodeCompressCertificate(DataBuffer buffer, int length) throws IOException {
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
	private static SessionTicket decodeSessionTicket(DataBuffer buffer, int length) throws IOException {
		final SessionTicket ticket = new SessionTicket();
		if (length > 0) {
			ticket.setTicket(new byte[length]);
			buffer.readFully(ticket.getTicket());
		}
		return ticket;
	}

	/** RFC 5746 */
	private static RenegotiationInfo decodeRenegotiationInfo(DataBuffer buffer, int length) throws IOException {
		final RenegotiationInfo info = new RenegotiationInfo();
		if (length > 0) {
			// opaque renegotiated_connection<0..255>;
			info.setValue(new byte[buffer.readUnsignedByte()]);
			buffer.readFully(info.getValue());
		}
		return info;
	}

	private static ApplicationSettings decodeApplicationSettings(DataBuffer buffer, int length) throws IOException {
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

	private static EncryptedClientHello decodeEncryptedClientHello(DataBuffer buffer, int length) throws IOException {
		final EncryptedClientHello extension = new EncryptedClientHello();
		// TODO
		return extension;
	}

	/** RFC 8446 */
	private static PskKeyExchangeModes decodePskKeyExchangeModes(DataBuffer buffer, int length) throws IOException {
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
	private static EarlyDataIndication decodeEarlyDataIndication(DataBuffer buffer, int length) throws IOException {
		if (length > 0) {
			final EarlyDataIndication max = new EarlyDataIndication();
			max.setMaxSize(buffer.readInt());
			return max;
		} else {
			return EarlyDataIndication.EMPTY;
		}
	}

	/** RFC 8446 */
	private static Cookie decodeCookie(DataBuffer buffer, int length) throws IOException {
		final Cookie cookie = new Cookie();
		if (length > 0) {
			cookie.setCookie(new byte[buffer.readUnsignedShort()]);
			buffer.readFully(cookie.getCookie());
		}
		return cookie;
	}

	/** RFC 8446 */
	private static SupportedVersions decodeSelectedVersion(DataBuffer buffer, int length) throws IOException {
		final SupportedVersions selected = new SupportedVersions();
		if (length > 0) {
			selected.add(buffer.readShort());
		}
		return selected;
	}

	/** RFC 8446 */
	private static SupportedVersions decodeSupportedVersions(DataBuffer buffer, int length) throws IOException {
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
	private static CertificateAuthorities decodeCertificateAuthorities(DataBuffer buffer, int length) throws IOException {
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
	private static PostHandshakeAuth decodePostHandshakeAuth(DataBuffer buffer, int length) throws IOException {
		return PostHandshakeAuth.INSTANCE;
	}

	/** RFC 8446 */
	private static SignatureAlgorithmsCert decodeSignatureAlgorithmsCert(DataBuffer buffer, int length) throws IOException {
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
}