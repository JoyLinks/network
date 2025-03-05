package com.joyzl.network.tls;

class Invalid extends Record {

	public final static Invalid INSTANCE = new Invalid();

	@Override
	public byte contentType() {
		return INVALID;
	}
}