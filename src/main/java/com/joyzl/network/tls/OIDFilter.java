package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class OIDFilter {

	private byte[] oid = TLS.EMPTY_BYTES;
	private byte[][] values = TLS.EMPTY_STRINGS;

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
		if (values == TLS.EMPTY_STRINGS) {
			values = new byte[][] { value };
		} else {
			values = Arrays.copyOf(values, values.length + 1);
			values[values.length - 1] = value;
		}
	}

	public void setValues(byte[][] value) {
		if (value == null) {
			value = TLS.EMPTY_STRINGS;
		} else {
			values = value;
		}
	}

	public int valueSize() {
		return values.length;
	}
}