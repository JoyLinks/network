package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * TLS 1.2 客户端发送的可信任CA用于选择证书
 * 
 * <pre>
 * RFC 6066
 * 
 * struct {
 *     TrustedAuthority trusted_authorities_list<0..2^16-1>;
 * } TrustedAuthorities;
 * 
 * struct {
 *     IdentifierType identifier_type;
 *     select (identifier_type) {
 *      case pre_agreed: struct {};
 *      case key_sha1_hash: SHA1Hash;
 *      case x509_name: DistinguishedName;
 *      case cert_sha1_hash: SHA1Hash;
 *     } identifier;
 * } TrustedAuthority;
 * 
 * enum {
 *     pre_agreed(0), 
 *     key_sha1_hash(1), 
 *     x509_name(2),
 *     cert_sha1_hash(3),
 *     (255)
 * } IdentifierType;
 * 
 * opaque DistinguishedName<1..2^16-1>;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class TrustedAuthorities extends Extension {

	// IdentifierType MAX(255)

	public final static byte PRE_AGREED = 0;
	public final static byte KEY_SHA1_HASH = 1;
	public final static byte X509_NAME = 2;
	public final static byte CERT_SHA1_HASH = 3;

	////////////////////////////////////////////////////////////////////////////////

	private final static TrustedAuthority[] EMPTY_TRUSTED_AUTHORITY = new TrustedAuthority[0];
	private TrustedAuthority[] items = EMPTY_TRUSTED_AUTHORITY;

	@Override
	public short type() {
		return TRUSTED_CA_KEYS;
	}

	public TrustedAuthority[] get() {
		return items;
	}

	public TrustedAuthority get(int index) {
		return items[index];
	}

	public void set(TrustedAuthority... value) {
		if (value == null) {
			items = EMPTY_TRUSTED_AUTHORITY;
		} else {
			items = value;
		}
	}

	public void add(TrustedAuthority value) {
		if (items == EMPTY_TRUSTED_AUTHORITY) {
			items = new TrustedAuthority[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}

	@Override
	public String toString() {
		return "trusted_ca_keys:";
	}

	static class TrustedAuthority {

		private byte type;
		private byte[] data;

		public TrustedAuthority() {
		}

		public TrustedAuthority(byte type) {
			this.type = type;
			this.data = TLS.EMPTY_BYTES;
		}

		public TrustedAuthority(byte type, byte[] data) {
			this.type = type;
			this.data = data;
		}

		public byte getType() {
			return type;
		}

		public void setType(byte value) {
			type = value;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] value) {
			if (value == null) {
				data = TLS.EMPTY_BYTES;
			} else {
				data = value;
			}
		}
	}
}