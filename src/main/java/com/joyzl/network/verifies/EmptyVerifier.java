/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 2018年9月18日
 */
package com.joyzl.network.verifies;

/**
 * 空校验(什么也不做)
 * 
 * @author simon(ZhangXi TEL:13883833982)
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