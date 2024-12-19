package com.joyzl.network.tls;

public enum NameType {
	HOST_NAME(0),
	// MAx(255)
	;

	private final int code;

	private NameType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static NameType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}