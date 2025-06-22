/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
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