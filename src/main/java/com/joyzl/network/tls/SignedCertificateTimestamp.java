/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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

	private byte[][] items = TLS.EMPTY_BYTES_BYTES;

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
			items = TLS.EMPTY_BYTES_BYTES;
		} else {
			items = value;
		}
	}

	public void add(byte[] value) {
		if (items == TLS.EMPTY_BYTES_BYTES) {
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