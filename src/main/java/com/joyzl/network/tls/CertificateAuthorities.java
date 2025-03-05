package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 终端支持的证书颁发机构CA(Certificate Authority)
 * 
 * <pre>
 * opaque DistinguishedName<1..2^16-1>;
 * 
 * struct {
 *     DistinguishedName authorities<3..2^16-1>;
 * } CertificateAuthoritiesExtension;
 * 
 * X501 DER X690
 * https://www.itu.int/rec/T-REC-X.501/en
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateAuthorities extends Extension {

	private byte[][] authorities;

	@Override
	public short type() {
		return CERTIFICATE_AUTHORITIES;
	}

	public byte[][] get() {
		return authorities;
	}

	public byte[] get(int index) {
		return authorities[index];
	}

	public void set(byte[]... value) {
		if (value == null) {
			authorities = TLS.EMPTY_STRINGS;
		} else {
			authorities = value;
		}
	}

	public void add(byte[] value) {
		if (authorities == TLS.EMPTY_STRINGS) {
			authorities = new byte[][] { value };
		} else {
			authorities = Arrays.copyOf(authorities, authorities.length + 1);
			authorities[authorities.length - 1] = value;
		}
	}

	public int size() {
		return authorities.length;
	}

	@Override
	public String toString() {
		return "certificate_authorities:" + authorities.length;
	}
}