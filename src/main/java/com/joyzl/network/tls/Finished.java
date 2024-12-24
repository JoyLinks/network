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

	private byte[] verify_data;

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