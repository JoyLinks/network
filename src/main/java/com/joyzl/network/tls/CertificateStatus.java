package com.joyzl.network.tls;

public class CertificateStatus extends Handshake {

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CERTIFICATE_STATUS;
	}

}
