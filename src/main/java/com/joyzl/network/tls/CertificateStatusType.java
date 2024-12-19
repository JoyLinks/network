package com.joyzl.network.tls;

public enum CertificateStatusType {
	OCSP(1),
	// MAX(255)
	;

	private final int code;

	private CertificateStatusType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static CertificateStatusType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}