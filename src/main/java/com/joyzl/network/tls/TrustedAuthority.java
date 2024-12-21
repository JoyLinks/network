package com.joyzl.network.tls;

public class TrustedAuthority {

	// IdentifierType MAX(255)

	public final static byte PRE_AGREED = 0;
	public final static byte KEY_SHA1_HASH = 1;
	public final static byte X509_NAME = 2;
	public final static byte CERT_SHA1_HASH = 3;

	////////////////////////////////////////////////////////////////////////////////

	private final byte type;
	private byte[] data;

	public TrustedAuthority(byte type) {
		this.type = type;
		this.data = TLS.EMPTY_BYTES;
	}

	public TrustedAuthority(byte type, byte[] data) {
		this.type = type;
		this.data = data;
	}

	public byte type() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] value) {
		data = value;
	}
}