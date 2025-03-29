package com.joyzl.network.tls;

/**
 * 证书签名用于验证Certificate消息
 * 
 * <pre>
 * TLS 1.2
 * struct {
 *       digitally-signed struct {
 *             opaque handshake_messages[handshake_messages_length];
 *       }
 * } CertificateVerify;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateVerifyV2 extends Handshake {

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

	@Override
	public String toString() {
		return name() + ':' + signature.length + "byte";
	}
}