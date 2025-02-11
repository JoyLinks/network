package com.joyzl.network.tls;

public class Invalid extends Record {

	public final static Invalid INSTANCE = new Invalid();

	@Override
	public byte contentType() {
		return INVALID;
	}
}