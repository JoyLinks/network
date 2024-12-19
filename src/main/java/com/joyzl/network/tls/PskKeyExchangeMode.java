package com.joyzl.network.tls;

public enum PskKeyExchangeMode {
	PSK_KE(0), PSK_DHE_KE(1),
	// MAX (255)
	;

	private final int code;

	private PskKeyExchangeMode(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static PskKeyExchangeMode code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}