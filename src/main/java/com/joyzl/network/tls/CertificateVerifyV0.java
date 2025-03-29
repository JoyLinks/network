package com.joyzl.network.tls;

/**
 * 证书签名用于验证Certificate消息
 * 
 * <pre>
 * TLS 1.0
 * select (SignatureAlgorithm){
 *       case anonymous: struct { };
 *       case rsa:
 *             digitally-signed struct {
 *                   opaque md5_hash[16];
 *                   opaque sha_hash[20];
 *             };
 *       case dsa:
 *             digitally-signed struct {
 *                   opaque sha_hash[20];
 *             };
 * } Signature;
 * 
 * struct {
 *       Signature signature;
 * } CertificateVerify;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateVerifyV0 extends Handshake implements SignatureScheme {

	private byte[] signedMD5 = TLS.EMPTY_BYTES;
	private byte[] signedSHA = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE_VERIFY;
	}

	public byte[] getSignedMD5() {
		return signedMD5;
	}

	public void setSignedMD5(byte[] value) {
		if (value == null) {
			signedMD5 = TLS.EMPTY_BYTES;
		} else {
			signedMD5 = value;
		}
	}

	public byte[] getSignedSHA() {
		return signedSHA;
	}

	public void setSignedSHA(byte[] value) {
		if (value == null) {
			signedSHA = TLS.EMPTY_BYTES;
		} else {
			signedSHA = value;
		}
	}

	@Override
	public String toString() {
		return name() + ":MD5(" + signedMD5.length + "bytes),SHA(" + signedSHA.length + "byte)";
	}
}