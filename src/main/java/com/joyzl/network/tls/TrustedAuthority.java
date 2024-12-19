package com.joyzl.network.tls;

public class TrustedAuthority {

	public final static TrustedAuthority PRE_AGREED = new TrustedAuthority(IdentifierType.PRE_AGREED);

	private final IdentifierType type;
	private byte[] data;

	public TrustedAuthority(IdentifierType type) {
		this.type = type;
		this.data = TLS.EMPTY_BYTES;
	}

	public TrustedAuthority(IdentifierType type, byte[] data) {
		this.type = type;
		this.data = data;
	}

	public IdentifierType type() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] value) {
		data = value;
	}
}