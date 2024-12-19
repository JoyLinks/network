package com.joyzl.network.tls;

public enum IdentifierType {
	PRE_AGREED(0), KEY_SHA1_HASH(1), X509_NAME(2), CERT_SHA1_HASH(3),
	// MAX(255)
	;

	private final int code;

	private IdentifierType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static IdentifierType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}