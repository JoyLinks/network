package com.joyzl.network.tls;

/**
 * zero bytes
 * 
 * @author ZhangXi 2024年12月19日
 */
public class Padding extends Extension {

	private int siez;

	@Override
	public short type() {
		return PADDING;
	}

	/**
	 * 填充的零字节数量（包含Padding自身的标识4Byte）
	 */
	public int getSiez() {
		return siez;
	}

	public void setSiez(int value) {
		siez = value;
	}
}