package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * TLS 1.1
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
 *       DistinguishedName certificate_authorities<0..2^16-1>;
 * } CertificateRequest;
 * </pre>
 * 
 * <pre>
 * TLS 1.0
 * enum {
 *       rsa_sign(1), dss_sign(2), rsa_fixed_dh(3), dss_fixed_dh(4), (255)
 * } ClientCertificateType;
 * 
 * opaque DistinguishedName<1..2^16-1>;
 * 
 * struct {
 *       ClientCertificateType certificate_types<1..2^8-1>;
 *       DistinguishedName certificate_authorities<3..2^16-1>;
 * } CertificateRequest;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateRequestV0 extends Handshake {

	private byte[] types = TLS.EMPTY_BYTES;
	private byte[][] authorities = TLS.EMPTY_BYTES_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE_REQUEST;
	}

	public byte[] getTypes() {
		return types;
	}

	public byte getType(int index) {
		return types[index];
	}

	public void setTypes(byte... values) {
		if (values == null) {
			types = TLS.EMPTY_BYTES;
		} else {
			types = values;
		}
	}

	public int typeSize() {
		return types.length;
	}

	public byte[][] getNames() {
		return authorities;
	}

	public byte[] getName(int index) {
		return authorities[index];
	}

	public void setNames(byte[]... value) {
		if (value == null) {
			authorities = TLS.EMPTY_BYTES_BYTES;
		} else {
			authorities = value;
		}
	}

	public void addName(byte[] value) {
		if (authorities == TLS.EMPTY_BYTES_BYTES) {
			authorities = new byte[][] { value };
		} else {
			authorities = Arrays.copyOf(authorities, authorities.length + 1);
			authorities[authorities.length - 1] = value;
		}
	}

	public int nameSize() {
		return authorities.length;
	}
}