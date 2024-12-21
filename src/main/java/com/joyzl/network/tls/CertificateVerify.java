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

	private short algorithm;
	private byte[] signature;

	@Override
	public byte msgType() {
		return CERTIFICATE_VERIFY;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] value) {
		signature = value;
	}

	public short getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(short value) {
		algorithm = value;
	}
}