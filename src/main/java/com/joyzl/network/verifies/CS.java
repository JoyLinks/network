/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
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