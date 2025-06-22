/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
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