package com.joyzl.network.tls;

public class TLSCompressed {

	private ContentType type;
	private ProtocolVersion version;
	private int length;
	private byte[] fragment;

	public byte[] getFragment() {
		return fragment;
	}

	public void setFragment(byte[] value) {
		fragment = value;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int value) {
		length = value;
	}

	public ProtocolVersion getVersion() {
		return version;
	}

	public void setVersion(ProtocolVersion value) {
		version = value;
	}

	public ContentType getType() {
		return type;
	}

	public void setType(ContentType value) {
		type = value;
	}
}