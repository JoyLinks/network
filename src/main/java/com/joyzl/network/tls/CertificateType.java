package com.joyzl.network.tls;

public enum CertificateType {
	X509(0), RAW_PUBLIC_KEY(2),
	// MAX(255)
	;

	private final int code;

	private CertificateType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static CertificateType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}