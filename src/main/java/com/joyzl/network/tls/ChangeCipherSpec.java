package com.joyzl.network.tls;

public enum ChangeCipherSpec {

	CHANGE_CIPHER_SPEC(1);

	private final int code;

	private ChangeCipherSpec(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}
}