/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

/**
 * CS 校验(长度1字节)
 * <p>
 * 字节累加不进位<br>
 * Byte wise addition (8 bits with no Carry)
 * 
 * @author ZhangXi
 *
 */
public final class CS extends Verifier {

	private byte cs;

	@Override
	public byte check(byte value) {
		cs += value;
		return value;
	}

	@Override
	public int value() {
		return cs;
	}

	@Override
	public void reset() {
		cs = 0;
	}
}