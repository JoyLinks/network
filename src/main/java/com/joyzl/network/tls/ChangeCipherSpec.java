package com.joyzl.network.tls;

public class ChangeCipherSpec extends TLSPlaintext {

	public final static ChangeCipherSpec INSTANCE = new ChangeCipherSpec();
	public final static byte ONE = 0x01;

	@Override
	public byte contentType() {
		return CHANGE_CIPHER_SPEC;
	}
}