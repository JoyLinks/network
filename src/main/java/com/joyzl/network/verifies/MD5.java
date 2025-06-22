/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5校验
 * 
 * @author ZhangXi
 *
 */
public final class MD5 extends Verifier {

	private final MessageDigest digest;

	public MD5() {
		try {
			digest = MessageDigest.getInstance("MD5");
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