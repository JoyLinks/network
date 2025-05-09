/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
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