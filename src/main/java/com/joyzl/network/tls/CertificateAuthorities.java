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
public class CertificateAuthorities extends Extension {

	private final static byte[][] EMPTY = new byte[0][];
	private byte[][] items;

	@Override
	public short type() {
		return CERTIFICATE_AUTHORITIES;
	}

	public byte[][] get() {
		return items;
	}

	public byte[] get(int index) {
		return items[index];
	}

	public void set(byte[]... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(byte[] value) {
		if (items == EMPTY) {
			items = new byte[][] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}

	@Override
	public String toString() {
		return "certificate_authorities:" + items.length;
	}
}