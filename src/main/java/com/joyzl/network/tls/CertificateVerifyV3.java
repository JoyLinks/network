package com.joyzl.network.tls;

/**
 * 证书签名用于验证Certificate消息
 * 
 * <pre>
 * TLS 1.3
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
class CertificateVerifyV3 extends CertificateVerifyV2 {

	private short algorithm;

	@Override
	public byte msgType() {
		return CERTIFICATE_VERIFY;
	}

	public short getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(short value) {
		algorithm = value;
	}

	@Override
	public String toString() {
		return name() + ':' + SignatureScheme.name(algorithm) + ",signature=" + getSignature().length + "byte";
	}
}