package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * struct {
 *        SignatureScheme supported_signature_algorithms<2..2^16-2>;
 * } SignatureSchemeList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class SignatureAlgorithms extends Extension {

	// SignatureScheme MAX (0xFFFF)

	/** RSASSA-PKCS1-v1_5 algorithms */
	public final static short RSA_PKCS1_SHA256 = 0x0401;
	/** RSASSA-PKCS1-v1_5 algorithms */
	public final static short RSA_PKCS1_SHA384 = 0x0501;
	/** RSASSA-PKCS1-v1_5 algorithms */
	public final static short RSA_PKCS1_SHA512 = 0x0601;

	/** ECDSA algorithms */
	public final static short ECDSA_SECP256R1_SHA256 = 0x0403;
	/** ECDSA algorithms */
	public final static short ECDSA_SECP384R1_SHA384 = 0x0503;
	/** ECDSA algorithms */
	public final static short ECDSA_SECP521R1_SHA512 = 0x0603;

	/** RSASSA-PSS algorithms with public key OID rsaEncryption */
	public final static short RSA_PSS_RSAE_SHA256 = 0x0804;
	/** RSASSA-PSS algorithms with public key OID rsaEncryption */
	public final static short RSA_PSS_RSAE_SHA384 = 0x0805;
	/** RSASSA-PSS algorithms with public key OID rsaEncryption */
	public final static short RSA_PSS_RSAE_SHA512 = 0x0806;

	/** EdDSA algorithms */
	public final static short ED25519 = 0x0807;
	/** EdDSA algorithms */
	public final static short ED448 = 0x0808;

	/** RSASSA-PSS algorithms with public key OID RSASSA-PSS */
	public final static short RSA_PSS_PSS_SHA256 = 0x0809;
	/** RSASSA-PSS algorithms with public key OID RSASSA-PSS */
	public final static short RSA_PSS_PSS_SHA384 = 0x080a;
	/** RSASSA-PSS algorithms with public key OID RSASSA-PSS */
	public final static short RSA_PSS_PSS_SHA512 = 0x080b;

	/** Legacy algorithms */
	public final static short RSA_PKCS1_SHA1 = 0x0201;
	/** Legacy algorithms */
	public final static short ECDSA_SHA1 = 0x0203;

	/** Reserved Code Points */
	// obsolete_RESERVED(0x0000..0x0200;
	public final static short DSA_SHA1_RESERVED = 0x0202;
	// obsolete_RESERVED(0x0204..0x0400;
	public final static short DSA_SHA256_RESERVED = 0x0402;
	// obsolete_RESERVED(0x0404..0x0500;
	public final static short DSA_SHA384_RESERVED = 0x0502;
	// obsolete_RESERVED(0x0504..0x0600;
	public final static short DSA_SHA512_RESERVED = 0x0602;
	// obsolete_RESERVED(0x0604..0x06FF),
	// private_use(0xFE00..0xFFFF),

	public final static short[] ALL = new short[] { //
			RSA_PKCS1_SHA256, //
			RSA_PKCS1_SHA384, //
			RSA_PKCS1_SHA512, //
			ECDSA_SECP256R1_SHA256, //
			ECDSA_SECP384R1_SHA384, //
			ECDSA_SECP521R1_SHA512, //
			RSA_PSS_RSAE_SHA256, //
			RSA_PSS_RSAE_SHA384, //
			RSA_PSS_RSAE_SHA512, //
			ED25519, //
			ED448, //
			RSA_PSS_PSS_SHA256, //
			RSA_PSS_PSS_SHA384, //
			RSA_PSS_PSS_SHA512, //
			RSA_PKCS1_SHA1, //
			ECDSA_SHA1, //
			DSA_SHA1_RESERVED, //
			DSA_SHA256_RESERVED, //
			DSA_SHA384_RESERVED, //
			DSA_SHA512_RESERVED,//
	};

	////////////////////////////////////////////////////////////////////////////////

	private final static short[] EMPTY = new short[0];
	private short[] items = EMPTY;

	public SignatureAlgorithms() {
	}

	public SignatureAlgorithms(short... value) {
		set(value);
	}

	@Override
	public short type() {
		return SIGNATURE_ALGORITHMS;
	}

	public short[] get() {
		return items;
	}

	public short get(int index) {
		return items[index];
	}

	public void set(short... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(short value) {
		if (items == EMPTY) {
			items = new short[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}
}