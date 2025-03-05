package com.joyzl.network.tls;

/**
 * 数字签名算法
 * 
 * @author ZhangXi 2025年2月24日
 */
public interface SignatureScheme {

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
	public final static short DSA_SHA1_RESERVED = 0x0202;
	// obsolete_RESERVED(0x0204..0x0400;
	public final static short DSA_SHA256_RESERVED = 0x0402;
	// obsolete_RESERVED(0x0404..0x0500;
	public final static short DSA_SHA384_RESERVED = 0x0502;
	// obsolete_RESERVED(0x0504..0x0600;
	public final static short DSA_SHA512_RESERVED = 0x0602;
	// obsolete_RESERVED(0x0604..0x06FF),
	// private_use(0xFE00..0xFFFF),

	/**
	 * 全部数字签名算法
	 */
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
			ECDSA_SHA1,//
	};

	public static String named(short value) {
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