package com.joyzl.network.tls;

public enum CipherType {
	STREAM(0), BLOCK(1),
	// MAX(255)
	;

	private final int code;

	private CipherType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static CipherType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}