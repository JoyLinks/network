package com.joyzl.network.tls;

public enum CertChainType {
	/** DER-encoded X.509v3 certificate */
	INDIVIDUAL_CERTS(0),
	/** MIME:application/pkix-pkipath */
	PKIPATH(1),
	// MAX(255)
	;

	private final int code;

	private CertChainType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}
}