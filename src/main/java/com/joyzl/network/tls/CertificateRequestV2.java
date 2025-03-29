package com.joyzl.network.tls;

/**
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
 *       SignatureAndHashAlgorithm supported_signature_algorithms<2^16-1>;
 *       DistinguishedName certificate_authorities<0..2^16-1>;
 * } CertificateRequest;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateRequestV2 extends CertificateRequestV0 {

	private short[] algorithms = TLS.EMPTY_SHORTS;

	public short[] getAlgorithms() {
		return algorithms;
	}

	public short getAlgorithm(int index) {
		return algorithms[index];
	}

	public void setAlgorithms(short... values) {
		if (values == null) {
			algorithms = TLS.EMPTY_SHORTS;
		} else {
			algorithms = values;
		}
	}

	public int algorithmSize() {
		return algorithms.length;
	}
}