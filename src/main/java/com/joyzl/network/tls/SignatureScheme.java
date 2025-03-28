package com.joyzl.network.tls;

/**
 * 数字签名算法
 * 
 * @author ZhangXi 2025年2月24日
 */
public interface SignatureScheme {

	// TLS 1.3
	// SignatureScheme MAX (0xFFFF)
	// Secure Hash Standard (SHS)

	/** TLS 1.2 RSASSA-PKCS1-v1_5 algorithms RFC8017 */
	public final static short RSA_PKCS1_SHA256 = 0x0401;
	/** TLS 1.2 RSASSA-PKCS1-v1_5 algorithms RFC8017 */
	public final static short RSA_PKCS1_SHA384 = 0x0501;
	/** TLS 1.2 RSASSA-PKCS1-v1_5 algorithms RFC8017 */
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
	// public final static short DSA_SHA1 = 0x0202;
	// obsolete_RESERVED(0x0204..0x0400;
	// public final static short DSA_SHA256 = 0x0402;
	// obsolete_RESERVED(0x0404..0x0500;
	// public final static short DSA_SHA384 = 0x0502;
	// obsolete_RESERVED(0x0504..0x0600;
	// public final static short DSA_SHA512 = 0x0602;
	// obsolete_RESERVED(0x0604..0x06FF),
	// private_use(0xFE00..0xFFFF),

	public final static short[] V13 = new short[] { //
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

			RSA_PKCS1_SHA256, //
			RSA_PKCS1_SHA384, //
			RSA_PKCS1_SHA512, //

			RSA_PKCS1_SHA1, //
			ECDSA_SHA1,//
	};

	// TLS 1.2
	// enum{none(0),md5(1),sha1(2),sha224(3),sha256(4),sha384(5),sha512(6),(255)}HashAlgorithm;

	final static byte NONE = 0;
	final static byte MD5 = 1;
	final static byte SHA1 = 2;
	final static byte SHA224 = 3;
	final static byte SHA256 = 4;
	final static byte SHA384 = 5;
	final static byte SHA512 = 6;

	// TLS 1.2
	// enum{anonymous(0),rsa(1),dsa(2),ecdsa(3),(255)}SignatureAlgorithm;

	final static byte ANON = 0;
	final static byte ANONYMOUS = 0;
	final static byte RSA = 1;
	final static byte DSA = 2;
	final static byte ECDSA = 3;

	final static byte DSS = 4;
	final static byte PSK = 5;

	// TLS 1.2
	// SignatureAndHashAlgorithm=hash|signature

	public final static short ANONYMOUS_NONE = 0x0000;
	public final static short ANONYMOUS_MD5 = 0x0100;
	public final static short ANONYMOUS_SHA1 = 0x0200;
	public final static short ANONYMOUS_SHA224 = 0x0300;
	public final static short ANONYMOUS_SHA256 = 0x0400;
	public final static short ANONYMOUS_SHA384 = 0x0500;
	public final static short ANONYMOUS_SHA512 = 0x0600;
	public final static short RSA_NONE = 0x0001;
	public final static short RSA_MD5 = 0x0101;
	public final static short RSA_SHA1 = 0x0201;
	public final static short RSA_SHA224 = 0x0301;
	public final static short RSA_SHA256 = 0x0401;
	public final static short RSA_SHA384 = 0x0501;
	public final static short RSA_SHA512 = 0x0601;
	public final static short DSA_NONE = 0x0002;
	public final static short DSA_MD5 = 0x0102;
	public final static short DSA_SHA1 = 0x0202;
	public final static short DSA_SHA224 = 0x0302;
	public final static short DSA_SHA256 = 0x0402;
	public final static short DSA_SHA384 = 0x0502;
	public final static short DSA_SHA512 = 0x0602;
	public final static short ECDSA_NONE = 0x0003;
	public final static short ECDSA_MD5 = 0x0103;
	// public final static short ECDSA_SHA1 = 0x0203;
	public final static short ECDSA_SHA224 = 0x0303;
	public final static short ECDSA_SHA256 = 0x0403;
	public final static short ECDSA_SHA384 = 0x0503;
	public final static short ECDSA_SHA512 = 0x0603;

	public final static short[] V12 = new short[] { //
			ANONYMOUS_NONE, //
			ANONYMOUS_MD5, //
			ANONYMOUS_SHA1, //
			ANONYMOUS_SHA224, //
			ANONYMOUS_SHA256, //
			ANONYMOUS_SHA384, //
			ANONYMOUS_SHA512, //
			RSA_NONE, //
			RSA_MD5, //
			RSA_SHA1, //
			RSA_SHA224, //
			RSA_SHA256, //
			RSA_SHA384, //
			RSA_SHA512, //
			DSA_NONE, //
			DSA_MD5, //
			DSA_SHA1, //
			DSA_SHA224, //
			DSA_SHA256, //
			DSA_SHA384, //
			DSA_SHA512, //
			ECDSA_NONE, //
			ECDSA_MD5, //
			ECDSA_SHA1, //
			ECDSA_SHA224, //
			ECDSA_SHA256, //
			ECDSA_SHA384, //
			ECDSA_SHA512,//
	};

	public final static short[] ALL = new short[] { //
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

			RSA_PKCS1_SHA256, //
			RSA_PKCS1_SHA384, //
			RSA_PKCS1_SHA512, //

			RSA_PKCS1_SHA1, //
			ECDSA_SHA1, //

			ANONYMOUS_NONE, //
			ANONYMOUS_MD5, //
			ANONYMOUS_SHA1, //
			ANONYMOUS_SHA224, //
			ANONYMOUS_SHA256, //
			ANONYMOUS_SHA384, //
			ANONYMOUS_SHA512, //

			RSA_NONE, //
			RSA_MD5, //
			RSA_SHA1, //
			RSA_SHA224, //
			RSA_SHA256, //
			RSA_SHA384, //
			RSA_SHA512, //

			DSA_NONE, //
			DSA_MD5, //
			DSA_SHA1, //
			DSA_SHA224, //
			DSA_SHA256, //
			DSA_SHA384, //
			DSA_SHA512, //

			ECDSA_NONE, //
			ECDSA_MD5, //
			ECDSA_SHA1, //
			ECDSA_SHA224, //
			ECDSA_SHA256, //
			ECDSA_SHA384, //
			ECDSA_SHA512,//
	};

	public static String name(short value) {
		String name = name3(value);
		if (name == null) {
			name = name2(value);
		}
		return name;
	}

	public static String name3(short value) {
		switch (value) {
			case RSA_PKCS1_SHA256:
				return "RSA_PKCS1_SHA256";
			case RSA_PKCS1_SHA384:
				return "RSA_PKCS1_SHA384";
			case RSA_PKCS1_SHA512:
				return "RSA_PKCS1_SHA512";
			case ECDSA_SECP256R1_SHA256:
				return "ECDSA_SECP256R1_SHA256";
			case ECDSA_SECP384R1_SHA384:
				return "ECDSA_SECP384R1_SHA384";
			case ECDSA_SECP521R1_SHA512:
				return "ECDSA_SECP521R1_SHA512";
			case RSA_PSS_RSAE_SHA256:
				return "RSA_PSS_RSAE_SHA256";
			case RSA_PSS_RSAE_SHA384:
				return "RSA_PSS_RSAE_SHA384";
			case RSA_PSS_RSAE_SHA512:
				return "RSA_PSS_RSAE_SHA512";
			case ED25519:
				return "ED25519";
			case ED448:
				return "ED448";
			case RSA_PSS_PSS_SHA256:
				return "RSA_PSS_PSS_SHA256";
			case RSA_PSS_PSS_SHA384:
				return "RSA_PSS_PSS_SHA384";
			case RSA_PSS_PSS_SHA512:
				return "RSA_PSS_PSS_SHA512";
			case RSA_PKCS1_SHA1:
				return "RSA_PKCS1_SHA1";
			case ECDSA_SHA1:
				return "ECDSA_SHA1";
			default:
				return null;
		}
	}

	public static String name2(short value) {
		switch (value) {
			case ANONYMOUS_NONE:
				return "ANONYMOUS_NONE";
			case ANONYMOUS_MD5:
				return "ANONYMOUS_MD5";
			case ANONYMOUS_SHA1:
				return "ANONYMOUS_SHA1";
			case ANONYMOUS_SHA224:
				return "ANONYMOUS_SHA224";
			case ANONYMOUS_SHA256:
				return "ANONYMOUS_SHA256";
			case ANONYMOUS_SHA384:
				return "ANONYMOUS_SHA384";
			case ANONYMOUS_SHA512:
				return "ANONYMOUS_SHA512";
			case RSA_NONE:
				return "RSA_NONE";
			case RSA_MD5:
				return "RSA_MD5";
			case RSA_SHA1:
				return "RSA_SHA1";
			case RSA_SHA224:
				return "RSA_SHA224";
			case RSA_SHA256:
				return "RSA_SHA256";
			case RSA_SHA384:
				return "RSA_SHA384";
			case RSA_SHA512:
				return "RSA_SHA512";
			case DSA_NONE:
				return "DSA_NONE";
			case DSA_MD5:
				return "DSA_MD5";
			case DSA_SHA1:
				return "DSA_SHA1";
			case DSA_SHA224:
				return "DSA_SHA224";
			case DSA_SHA256:
				return "DSA_SHA256";
			case DSA_SHA384:
				return "DSA_SHA384";
			case DSA_SHA512:
				return "DSA_SHA512";
			case ECDSA_NONE:
				return "ECDSA_NONE";
			case ECDSA_MD5:
				return "ECDSA_MD5";
			case ECDSA_SHA1:
				return "ECDSA_SHA1";
			case ECDSA_SHA224:
				return "ECDSA_SHA224";
			case ECDSA_SHA256:
				return "ECDSA_SHA256";
			case ECDSA_SHA384:
				return "ECDSA_SHA384";
			case ECDSA_SHA512:
				return "ECDSA_SHA512";
			default:
				return null;
		}
	}

	public static short name(String value) {
		short code = name3(value);
		if (code <= 0) {
			code = name2(value);
		}
		return code;
	}

	public static short name3(String value) {
		switch (value.toUpperCase()) {
			case "RSA_PKCS1_SHA256":
				return RSA_PKCS1_SHA256;
			case "RSA_PKCS1_SHA384":
				return RSA_PKCS1_SHA384;
			case "RSA_PKCS1_SHA512":
				return RSA_PKCS1_SHA512;
			case "ECDSA_SECP256R1_SHA256":
				return ECDSA_SECP256R1_SHA256;
			case "ECDSA_SECP384R1_SHA384":
				return ECDSA_SECP384R1_SHA384;
			case "ECDSA_SECP521R1_SHA512":
				return ECDSA_SECP521R1_SHA512;
			case "RSA_PSS_RSAE_SHA256":
				return RSA_PSS_RSAE_SHA256;
			case "RSA_PSS_RSAE_SHA384":
				return RSA_PSS_RSAE_SHA384;
			case "RSA_PSS_RSAE_SHA512":
				return RSA_PSS_RSAE_SHA512;
			case "ED25519":
				return ED25519;
			case "ED448":
				return ED448;
			case "RSA_PSS_PSS_SHA256":
				return RSA_PSS_PSS_SHA256;
			case "RSA_PSS_PSS_SHA384":
				return RSA_PSS_PSS_SHA384;
			case "RSA_PSS_PSS_SHA512":
				return RSA_PSS_PSS_SHA512;
			case "RSA_PKCS1_SHA1":
				return RSA_PKCS1_SHA1;
			case "ECDSA_SHA1":
				return ECDSA_SHA1;
			default:
				return 0;
		}
	}

	public static short name2(String value) {
		switch (value) {
			case "ANONYMOUS_NONE":
				return ANONYMOUS_NONE;
			case "ANONYMOUS_MD5":
				return ANONYMOUS_MD5;
			case "ANONYMOUS_SHA1":
				return ANONYMOUS_SHA1;
			case "ANONYMOUS_SHA224":
				return ANONYMOUS_SHA224;
			case "ANONYMOUS_SHA256":
				return ANONYMOUS_SHA256;
			case "ANONYMOUS_SHA384":
				return ANONYMOUS_SHA384;
			case "ANONYMOUS_SHA512":
				return ANONYMOUS_SHA512;
			case "RSA_NONE":
				return RSA_NONE;
			case "RSA_MD5":
				return RSA_MD5;
			case "RSA_SHA1":
				return RSA_SHA1;
			case "RSA_SHA224":
				return RSA_SHA224;
			case "RSA_SHA256":
				return RSA_SHA256;
			case "RSA_SHA384":
				return RSA_SHA384;
			case "RSA_SHA512":
				return RSA_SHA512;
			case "DSA_NONE":
				return DSA_NONE;
			case "DSA_MD5":
				return DSA_MD5;
			case "DSA_SHA1":
				return DSA_SHA1;
			case "DSA_SHA224":
				return DSA_SHA224;
			case "DSA_SHA256":
				return DSA_SHA256;
			case "DSA_SHA384":
				return DSA_SHA384;
			case "DSA_SHA512":
				return DSA_SHA512;
			case "ECDSA_NONE":
				return ECDSA_NONE;
			case "ECDSA_MD5":
				return ECDSA_MD5;
			case "ECDSA_SHA1":
				return ECDSA_SHA1;
			case "ECDSA_SHA224":
				return ECDSA_SHA224;
			case "ECDSA_SHA256":
				return ECDSA_SHA256;
			case "ECDSA_SHA384":
				return ECDSA_SHA384;
			case "ECDSA_SHA512":
				return ECDSA_SHA512;
			default:
				return 0;
		}
	}

	/**
	 * 匹配
	 */
	public static short match(short[] others) {
		for (int i = 0; i < ALL.length; i++) {
			for (int o = 0; o < others.length; o++) {
				if (ALL[i] == others[o]) {
					return others[o];
				}
			}
		}
		return 0;
	}
}