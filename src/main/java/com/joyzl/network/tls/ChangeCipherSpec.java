package com.joyzl.network.tls;

/**
 * 兼容（明文）
 * 
 * <pre>
 * struct {
 *       enum { change_cipher_spec(1), (255) } type;
 * } ChangeCipherSpec;
 * </pre>
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

	@Override
	public String toString() {
		return super.toString() + ":1";
	}
}