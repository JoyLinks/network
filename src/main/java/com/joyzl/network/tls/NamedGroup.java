package com.joyzl.network.tls;

public enum NamedGroup {

	/* Elliptic Curve Groups (ECDHE) */
	SECP256R1(0X0017),
	SECP384R1(0X0018),
	SECP521R1(0X0019),
	X25519(0X001D),
	X448(0X001E),

	/* Finite Field Groups (DHE) */
	FFDHE2048(0X0100),
	FFDHE3072(0X0101),
	FFDHE4096(0X0102),
	FFDHE6144(0X0103),
	FFDHE8192(0X0104),

	/* Reserved Code Points */
	// ffdhe_private_use(0x01FC..0x01FF),
	// ecdhe_private_use(0xFE00..0xFEFF),
	// MAX (0xFFFF)
	;

	private final int code;

	private NamedGroup(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static NamedGroup code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}