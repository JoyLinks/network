package com.joyzl.network.tls;

/**
 * 证书签名用于验证Certificate消息
 * 
 * <pre>
 * struct {
 *     SignatureScheme algorithm;
 *     opaque signature<0..2^16-1>;
 * } CertificateVerify;
 * 
 * Transcript-Hash(Handshake Context, Certificate)
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateVerify extends Handshake implements SignatureScheme {

	private short algorithm;
	private byte[] signature = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE_VERIFY;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] value) {
		if (value == null) {
			signature = TLS.EMPTY_BYTES;
		} else {
			signature = value;
		}
	}

	public short getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(short value) {
		algorithm = value;
	}

	@Override
	public String toString() {
		return name() + ':' + SignatureScheme.named(algorithm) + ",signature=" + signature.length + "byte";
	}
}