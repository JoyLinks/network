package com.joyzl.network.tls;

public class OCSPStatusRequest extends CertificateStatusRequest {

	private byte[] responderID = TLS.EMPTY_BYTES;
	private byte[] extensions = TLS.EMPTY_BYTES;

	@Override
	public CertificateStatusType type() {
		return CertificateStatusType.OCSP;
	}

	public byte[] getExtensions() {
		return extensions;
	}

	public void setExtensions(byte[] value) {
		extensions = value;
	}

	public byte[] getResponderID() {
		return responderID;
	}

	public void setResponderID(byte[] value) {
		responderID = value;
	}
}