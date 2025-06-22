/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

/**
 * 空校验(什么也不做)，此校验器实例用于减少空(null)判断
 * 
 * @author ZhangXi
 *
 */
public final class EmptyVerifier extends Verifier {

	public final static EmptyVerifier INSTANCE = new EmptyVerifier();

	private EmptyVerifier() {
	}

	@Override
	public byte check(byte value) {
		return value;
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	public void reset() {
	}
}