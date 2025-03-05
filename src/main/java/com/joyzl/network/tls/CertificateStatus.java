package com.joyzl.network.tls;

class CertificateStatus extends Handshake {

	@Override
	public byte msgType() {
		return CERTIFICATE_STATUS;
	}
}