package com.joyzl.network.tls;

public enum AlertLevel {

	WARNING(1), FATAL(2),
	// MAX(255)
	;

	private final int code;

	private AlertLevel(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static AlertLevel code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}