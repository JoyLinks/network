package com.joyzl.network.tls;

class TLSCompressed {

	private short version;
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

	public short getVersion() {
		return version;
	}

	public void setVersion(short value) {
		version = value;
	}
}