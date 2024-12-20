package com.joyzl.network.tls;

public class ChangeCipherSpec extends TLSPlaintext {

	public final static ChangeCipherSpec INSTANCE = new ChangeCipherSpec();
	public final static byte ONE = 0x01;

	@Override
	public ContentType contentType() {
		return ContentType.CHANGE_CIPHER_SPEC;
	}
}