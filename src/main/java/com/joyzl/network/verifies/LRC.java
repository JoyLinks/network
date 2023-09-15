/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * LRC 纵向冗余校验(Longitudinal Redundancy Check)
 * 
 * @author ZhangXi
 *
 */
public final class LRC extends Verifier {

	private int lrc = 0;

	@Override
	public byte check(byte value) {
		lrc += value & 0xFF;
		return value;
	}

	@Override
	public int value() {
		return 256 - (lrc % 256);
	}

	@Override
	public void reset() {
		lrc = 0;
	}
}