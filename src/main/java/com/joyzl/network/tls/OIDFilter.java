package com.joyzl.network.tls;

public class OIDFilter {

	private byte[] oid;
	private byte[] values;

	public OIDFilter() {
	}

	public OIDFilter(byte[] oid) {
		this.oid = oid;
	}

	public byte[] getOID() {
		return oid;
	}

	public void setOID(byte[] value) {
		oid = value;
	}

	public byte[] getValues() {
		return values;
	}

	public void setValues(byte[] value) {
		values = value;
	}
}