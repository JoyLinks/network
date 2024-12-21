package com.joyzl.network.tls;

public abstract class CertificateStatusRequest {

	// CertificateStatusType MAX(255)

	public final static byte OCSP = 1;

	////////////////////////////////////////////////////////////////////////////////

	public abstract byte type();
}