/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TLS 1.2 密钥提取与展开方法
 * 
 * @author ZhangXi 2024年12月22日
 */
class V2PRF extends V2TranscriptHash {

	/** HMAC */
	private Mac hmac;

	public void initialize(String mac, String digest) throws NoSuchAlgorithmException {
		super.initialize(digest);
		if (hmac == null || !mac.equals(hmac.getAlgorithm())) {
			hmac = Mac.getInstance(mac);
		} else {
			hmac.reset();
		}
	}

	/*-
	 * TLS 1.2 PRF 基础方法
	 * 
	 * struct {
	 *       uint8 major;
	 *       uint8 minor;
	 * } ProtocolVersion;
	 * 
	 * struct {
	 *       uint32 gmt_unix_time;
	 *       opaque random_bytes[28];
	 * } Random;
	 * 
	 * struct {
	 *       ProtocolVersion client_version;
	 *       opaque random[46];
	 * } PreMasterSecret;
	 * 
	 * 
	 * P_hash(secret, seed) = HMAC_hash(secret, A(1) + seed) +
	 *                        HMAC_hash(secret, A(2) + seed) + 
	 *                        HMAC_hash(secret, A(3) + seed) + ...
	 * 
	 * A(0) = seed
	 * A(i) = HMAC_hash(secret, A(i-1))
	 * 
	 * PRF(secret, label, seed) = P_hash(secret, label + seed)
	 */

	/** TLS 1.2 P_hash(secret, seed) */
	protected final byte[] pHash(byte[] secret, byte[] seed, int length) throws Exception {
		hmac.init(new SecretKeySpec(secret, hmac.getAlgorithm()));

		final byte[] output = new byte[length];
		byte[] B = null, A = null;
		int copy;
		while (length > 0) {
			if (A == null) {
				hmac.update(seed);
				A = hmac.doFinal();
			} else {
				hmac.update(A);
				hmac.doFinal(A, 0);
			}
			hmac.update(A);
			hmac.update(seed);
			if (B == null) {
				B = hmac.doFinal();
			} else {
				hmac.doFinal(B, 0);
			}
			copy = Math.min(length, B.length);
			System.arraycopy(B, 0, output, output.length - length, copy);
			length -= copy;
		}
		return output;
	}

	/** TLS 1.2 PRF(secret, label, seed) = P_hash(secret, label + seed) */
	protected final byte[] prf(byte[] secret, byte[] label, byte[] seed, int length) throws Exception {
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}

		hmac.init(new SecretKeySpec(secret, hmac.getAlgorithm()));

		final byte[] output = new byte[length];
		byte[] B = null, A = null;
		int copy;

		while (length > 0) {
			if (A == null) {
				hmac.update(label);
				hmac.update(seed);
				A = hmac.doFinal();
			} else {
				hmac.update(A);
				hmac.doFinal(A, 0);
			}
			hmac.update(A);
			hmac.update(label);
			hmac.update(seed);
			if (B == null) {
				B = hmac.doFinal();
			} else {
				hmac.doFinal(B, 0);
			}
			copy = Math.min(length, B.length);
			System.arraycopy(B, 0, output, output.length - length, copy);
			length -= copy;
		}
		return output;
	}

	/**
	 * TLS 1.2 PRF(secret, label, seed) = P_hash(secret, label + seed1 +seed2)
	 */
	protected final byte[] prf(byte[] secret, byte[] label, byte[] seed1, byte[] seed2, int length) throws Exception {
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}

		hmac.init(new SecretKeySpec(secret, hmac.getAlgorithm()));

		final byte[] output = new byte[length];
		byte[] B = null, A = null;
		int copy;

		while (length > 0) {
			if (A == null) {
				hmac.update(label);
				hmac.update(seed1);
				hmac.update(seed2);
				A = hmac.doFinal();
			} else {
				hmac.update(A);
				hmac.doFinal(A, 0);
			}
			hmac.update(A);
			hmac.update(label);
			hmac.update(seed1);
			hmac.update(seed2);
			if (B == null) {
				B = hmac.doFinal();
			} else {
				hmac.doFinal(B, 0);
			}
			copy = Math.min(length, B.length);
			System.arraycopy(B, 0, output, output.length - length, copy);
			length -= copy;
		}
		return output;
	}
}