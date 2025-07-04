/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class OIDFilter {

	private byte[] oid = TLS.EMPTY_BYTES;
	private byte[][] values = TLS.EMPTY_BYTES_BYTES;

	public OIDFilter() {
	}

	public OIDFilter(byte[] oid) {
		this.oid = oid;
	}

	public byte[] getOID() {
		return oid;
	}

	public String getOIDString() {
		return new String(oid, StandardCharsets.US_ASCII);
	}

	public void setOID(byte[] value) {
		if (value == null) {
			oid = TLS.EMPTY_BYTES;
		} else {
			oid = value;
		}
	}

	public boolean hasValues() {
		return values.length > 0;
	}

	public byte[][] getValues() {
		return values;
	}

	public byte[] getValues(int index) {
		return values[index];
	}

	public void addValue(byte[] value) {
		if (values == TLS.EMPTY_BYTES_BYTES) {
			values = new byte[][] { value };
		} else {
			values = Arrays.copyOf(values, values.length + 1);
			values[values.length - 1] = value;
		}
	}

	public void setValues(byte[][] value) {
		if (value == null) {
			value = TLS.EMPTY_BYTES_BYTES;
		} else {
			values = value;
		}
	}

	public int valueSize() {
		return values.length;
	}
}