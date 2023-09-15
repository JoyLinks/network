/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * BCC 信息组校验码(Block Check Character)
 * 
 * @author ZhangXi
 *
 */
public final class BCC extends Verifier {

	private byte bcc = 0;

	@Override
	public byte check(byte value) {
		bcc ^= value;
		return value;
	}

	@Override
	public int value() {
		return bcc;
	}

	@Override
	public void reset() {
		bcc = 0;
	}
}