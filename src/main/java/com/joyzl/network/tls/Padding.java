package com.joyzl.network.tls;

/**
 * RFC7685 A Transport Layer Security (TLS) ClientHello Padding Extension
 * 
 * @author ZhangXi 2024年12月19日
 */
public class Padding extends Extension {

	private int size;

	public Padding() {
	}

	public Padding(int size) {
		this.size = size;
	}

	@Override
	public short type() {
		return PADDING;
	}

	/**
	 * 填充的零字节数量（包含Padding自身的标识4Byte）
	 */
	public int getSiez() {
		return size;
	}

	public void setSiez(int value) {
		size = value;
	}
}