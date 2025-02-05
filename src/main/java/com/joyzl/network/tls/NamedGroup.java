package com.joyzl.network.tls;

/**
 * 密钥协商算法，用于密钥交换
 * 
 * @author ZhangXi 2025年2月3日
 */
public interface NamedGroup {

	// NamedGroup MAX (0xFFFF)

	/** Elliptic Curve Groups (ECDHE) */
	public final static short SECP256R1 = 0X0017;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short SECP384R1 = 0X0018;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short SECP521R1 = 0X0019;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short X25519 = 0X001D;
	/** Elliptic Curve Groups (ECDHE) */
	public final static short X448 = 0X001E;
	/** Elliptic Curve Groups (ECDHE) */

	/** Finite Field Groups (DHE) */
	public final static short FFDHE2048 = 0X0100;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE3072 = 0X0101;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE4096 = 0X0102;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE6144 = 0X0103;
	/** Finite Field Groups (DHE) */
	public final static short FFDHE8192 = 0X0104;

	/* Reserved Code Points */
	// ffdhe_private_use(0x01FC..0x01FF),
	// ecdhe_private_use(0xFE00..0xFEFF),

	public final static short[] ALL = new short[] { //
			X25519, //
			X448, //

			SECP256R1, //
			SECP384R1, //
			SECP521R1, //

			FFDHE2048, //
			FFDHE3072, //
			FFDHE4096, //
			FFDHE6144, //
			FFDHE8192,//
	};

	public static String named(short code) {
		switch (code) {
			case X25519:
				return "X25519";
			case X448:
				return "X448";
			case SECP256R1:
				return "SECP256R1";
			case SECP384R1:
				return "SECP384R1";
			case SECP521R1:
				return "SECP521R1";
			case FFDHE2048:
				return "FFDHE2048";
			case FFDHE3072:
				return "FFDHE3072";
			case FFDHE4096:
				return "FFDHE4096";
			case FFDHE6144:
				return "FFDHE6144";
			case FFDHE8192:
				return "FFDHE8192";
			default:
				return null;
		}
	}
}