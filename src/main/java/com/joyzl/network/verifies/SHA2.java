/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-2校验
 * 
 * @author ZhangXi
 *
 */
public final class SHA2 extends Verifier {

	private final MessageDigest digest;

	public SHA2() {
		try {
			digest = MessageDigest.getInstance("SHA-2");
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