package com.joyzl.network.tls;

/**
 * @see CertificateTypes
 * @author ZhangXi 2024年12月21日
 */
public class CertificateEntry {

	private final byte type;
	private byte[] data;

	public CertificateEntry(byte type) {
		this.type = type;
	}

	public byte type() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}