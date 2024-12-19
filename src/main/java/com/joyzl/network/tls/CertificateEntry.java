package com.joyzl.network.tls;

public class CertificateEntry {

	private final CertificateType type;
	private byte[] data;

	public CertificateEntry(CertificateType type) {
		this.type = type;
	}

	public CertificateType type() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}