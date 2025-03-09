package com.joyzl.network.tls;

/**
 * <pre>
 * TLS 1.3
 * 
 * struct {
 *     opaque certificate_request_context<0..2^8-1>;
 *     Extension extensions<2..2^16-1>;
 * } CertificateRequest;
 * </pre>
 * 
 * <pre>
 * TLS 1.2
 * enum {
 *       rsa_sign(1), dss_sign(2), rsa_fixed_dh(3), dss_fixed_dh(4),
 *       rsa_ephemeral_dh_RESERVED(5), dss_ephemeral_dh_RESERVED(6),
 *       fortezza_dms_RESERVED(20), (255)
 * } ClientCertificateType;
 * 
 * opaque DistinguishedName<1..2^16-1>;
 * 
 * struct {
 *       ClientCertificateType certificate_types<1..2^8-1>;
 *       SignatureAndHashAlgorithm
 *       supported_signature_algorithms<2^16-1>;
 *       DistinguishedName certificate_authorities<0..2^16-1>;
 * } CertificateRequest;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateRequest extends HandshakeExtensions {

	private byte[] context = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE_REQUEST;
	}

	public byte[] getContext() {
		return context;
	}

	public void setContext(byte[] value) {
		if (value == null) {
			context = TLS.EMPTY_BYTES;
		} else {
			context = value;
		}
	}
}