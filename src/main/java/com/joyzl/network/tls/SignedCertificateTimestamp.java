package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * <pre>
 * RFC 6962 Certificate Transparency
 * 
 * opaque SerializedSCT<1..2^16-1>;
 * 
 * struct {
 *     SerializedSCT sct_list <1..2^16-1>;
 * } SignedCertificateTimestampList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class SignedCertificateTimestamp extends Extension {

	private byte[][] items = TLS.EMPTY_STRINGS;

	@Override
	public short type() {
		return SIGNED_CERTIFICATE_TIMESTAMP;
	}

	public byte[][] get() {
		return items;
	}

	public byte[] get(int index) {
		return items[index];
	}

	public String getString(int index) {
		return new String(items[index], StandardCharsets.US_ASCII);
	}

	public void set(byte[]... value) {
		if (value == null) {
			items = TLS.EMPTY_STRINGS;
		} else {
			items = value;
		}
	}

	public void add(byte[] value) {
		if (items == TLS.EMPTY_STRINGS) {
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
		return "signed_certificate_timestamp:" + size();
	}
}