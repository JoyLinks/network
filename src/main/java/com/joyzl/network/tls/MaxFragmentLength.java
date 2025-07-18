/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 扩展：最大分块长度；
 * 
 * <pre>
 * RFC 6066
 * RFC 8449 Record Size Limit Extension for TLS
 * 
 * enum{
 *        2^9(1), 2^10(2), 2^11(3), 2^12(4), (255)
 * } MaxFragmentType;
 * </pre>
 * 
 * @author ZhangXi 2024年12月18日
 */
class MaxFragmentLength extends Extension {

	// MaxFragmentType MAX(255)

	/** 2^9 */
	public final static byte MAX_512 = 1;
	/** 2^10 */
	public final static byte MAX_1024 = 2;
	/** 2^11 */
	public final static byte MAX_2048 = 3;
	/** 2^12 */
	public final static byte MAX_4096 = 4;

	////////////////////////////////////////////////////////////////////////////////

	private final byte type;

	public MaxFragmentLength(byte type) {
		this.type = type;
	}

	@Override
	public short type() {
		return MAX_FRAGMENT_LENGTH;
	}

	public byte getType() {
		return type;
	}

	public int getMaxFragment() {
		if (type == MAX_4096) {
			return 4096;
		}
		if (type == MAX_2048) {
			return 2048;
		}
		if (type == MAX_1024) {
			return 1024;
		}
		if (type == MAX_512) {
			return 512;
		}
		return 0;
	}

	@Override
	public String toString() {
		return "max_fragment_length:" + getMaxFragment();
	}
}