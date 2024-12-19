package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *     SignatureScheme algorithm;
 *     opaque signature<0..2^16-1>;
 * } CertificateVerify;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class CertificateVerify extends Handshake {

	private SignatureScheme algorithm;
	private byte[] signature;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CERTIFICATE_VERIFY;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] value) {
		signature = value;
	}

	public SignatureScheme getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(SignatureScheme value) {
		algorithm = value;
	}
}