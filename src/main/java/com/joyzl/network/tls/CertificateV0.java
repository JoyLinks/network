package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 用于认证的证书和链中任何支持的证书
 * 
 * <pre>
 * RFC 2246 TLSv1.0
 * RFC 4336 TLSv1.1
 * RFC 5246 TLSv1.2
 * 
 * opaque ASN.1Cert<1..2^24-1>; // X.509v3
 * 
 * struct {
 *     ASN.1Cert certificate_list<0..2^24-1>;
 * } Certificate;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateV0 extends Handshake {

	private byte[][] list = EMPTY_BYTES_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE;
	}

	public void add(byte[] value) {
		if (list == EMPTY_BYTES_BYTES) {
			list = new byte[][] { value };
		} else {
			list = Arrays.copyOf(list, list.length + 1);
			list[list.length - 1] = value;
		}
	}

	public byte[] get(int index) {
		return list[index];
	}

	public byte[][] get() {
		return list;
	}

	public void set(byte[]... values) {
		if (values == null) {
			list = EMPTY_BYTES_BYTES;
		} else {
			list = values;
		}
	}

	public int size() {
		return list.length;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(':');
		b.append(size());
		if (size() > 0) {
			for (int index = 0; index < size(); index++) {
				b.append('\n');
				b.append('\t');
				b.append("X509(");
				b.append(get(index).length);
				b.append("byte)");
			}
		}
		return b.toString();
	}
}