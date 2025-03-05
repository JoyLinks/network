package com.joyzl.network.tls;

/**
 * 兼容（明文）
 * 
 * @author ZhangXi 2025年2月10日
 */
class ChangeCipherSpec extends Record {

	public final static byte ONE = 0x01;
	public final static ChangeCipherSpec INSTANCE = new ChangeCipherSpec();

	@Override
	public byte contentType() {
		return CHANGE_CIPHER_SPEC;
	}
}