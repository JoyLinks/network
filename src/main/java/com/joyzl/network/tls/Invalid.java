package com.joyzl.network.tls;

public class Invalid extends TLSPlaintext {

	public final static Invalid INSTANCE = new Invalid();

	@Override
	public ContentType contentType() {
		return ContentType.INVALID;
	}
}