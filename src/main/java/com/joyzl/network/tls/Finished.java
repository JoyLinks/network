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

	private byte[] data;

	@Override
	public byte msgType() {
		return FINISHED;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] value) {
		data = value;
	}
}