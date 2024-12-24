package com.joyzl.network.tls;

/**
 * 保留或未明确的扩展字段
 * 
 * @author ZhangXi 2024年12月20日
 */
public class Reserved extends Extension {

	private short type;
	private byte[] data = TLS.EMPTY_BYTES;

	public Reserved() {
	}

	public Reserved(short type) {
		this.type = type;
	}

	@Override
	public short type() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] value) {
		if (value == null) {
			data = TLS.EMPTY_BYTES;
		} else {
			data = value;
		}
	}

	public short getType() {
		return type;
	}

	public void setType(short value) {
		type = value;
	}
}