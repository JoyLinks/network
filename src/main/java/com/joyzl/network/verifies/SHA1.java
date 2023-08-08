/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 2018年9月18日
 */
package com.joyzl.network.verifies;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-1校验
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public final class SHA1 extends Verifier {

	private final MessageDigest digest;

	public SHA1() {
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte check(byte value) {
		digest.update(value);
		return value;
	}

	@Override
	public int value() {
		throw new UnsupportedOperationException();
	}

	public byte[] getBytes() {
		return digest.digest();
	}

	@Override
	public void reset() {
		digest.reset();
	}
}