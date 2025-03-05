package com.joyzl.network.tls;

class TLSException extends Exception {

	private static final long serialVersionUID = 1L;

	private final byte description;

	public TLSException(byte description) {
		this.description = description;
	}

	public byte getDescription() {
		return description;
	}
}