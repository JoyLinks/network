package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * RFC 6066
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
 *     pre_agreed(0), key_sha1_hash(1), x509_name(2),
 *     cert_sha1_hash(3), (255)
 * } IdentifierType;
 * 
 * opaque DistinguishedName<1..2^16-1>;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class TrustedAuthorities extends Extension {

	private final static TrustedAuthority[] EMPTY = new TrustedAuthority[0];
	private TrustedAuthority[] items = EMPTY;

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
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(TrustedAuthority value) {
		if (items == EMPTY) {
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
}