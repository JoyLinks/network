package com.joyzl.network.tls;

public class CertificateStatus extends Handshake {

	@Override
	public byte msgType() {
		return CERTIFICATE_STATUS;
	}
}