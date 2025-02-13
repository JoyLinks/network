package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *       opaque verify_data[Hash.length];
 * } Finished;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class Finished extends Handshake {

	/** 这个特殊常量标记完成校验成功 */
	final static byte[] OK = new byte[] { 1, 2 };

	private byte[] verify_data = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return FINISHED;
	}

	public byte[] getVerifyData() {
		return verify_data;
	}

	public void setVerifyData(byte[] value) {
		verify_data = value;
	}
}