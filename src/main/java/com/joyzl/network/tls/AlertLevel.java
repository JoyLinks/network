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
}