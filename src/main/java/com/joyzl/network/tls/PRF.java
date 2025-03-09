package com.joyzl.network.tls;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TLS 1.2 1.1 1.0 密钥获取与展开
 * 
 * @author ZhangXi 2025年3月8日
 */
public class PRF {

	/*-
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
	 * PRF(secret, label, seed) = P_<hash>(secret, label + seed)
	 */

	/** HMAC */
	private Mac hmac;

	public PRF() {
	}

	public PRF(String algorithm) throws NoSuchAlgorithmException {
		hmac(algorithm);
	}

	/** 指定算法 */
	public void hmac(String algorithm) throws NoSuchAlgorithmException {
		hmac = Mac.getInstance(algorithm);
	}

	/** P_hash(secret, seed) */
	public final byte[] expand(byte[] secret, byte[] seed, int length) throws InvalidKeyException {
		hmac.reset();
		hmac.init(new SecretKeySpec(secret, hmac.getAlgorithm()));

		final byte[] output = new byte[length];

		byte[] A = seed;
		int copy;
		while (length > 0) {
			hmac.update(A);
			A = hmac.doFinal(seed);
			copy = Math.min(length, A.length);
			System.arraycopy(A, 0, output, output.length - length, copy);
			length -= copy;
		}
		return output;
	}

	/** PRF(secret, label, seed) = P_<hash>(secret, label + seed) */
	public final byte[] expandLabel(byte[] secret, byte[] label, byte[] seed, int length) throws InvalidKeyException {
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}

		hmac.reset();
		hmac.init(new SecretKeySpec(secret, hmac.getAlgorithm()));

		final byte[] output = new byte[length];

		hmac.update(label);
		byte[] A = hmac.doFinal(seed);
		int copy = Math.min(length, A.length);
		System.arraycopy(A, 0, output, output.length - length, copy);
		length -= copy;

		while (length > 0) {
			hmac.update(A);
			hmac.update(label);
			A = hmac.doFinal(seed);
			copy = Math.min(length, A.length);
			System.arraycopy(A, 0, output, output.length - length, copy);
			length -= copy;
		}
		return output;
	}

	/** PRF(secret, label, seed) = P_<hash>(secret, label + seed1 +seed2) */
	public final byte[] expandLabel(byte[] secret, byte[] label, byte[] seed1, byte[] seed2, int length) throws InvalidKeyException {
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}

		hmac.reset();
		hmac.init(new SecretKeySpec(secret, hmac.getAlgorithm()));

		final byte[] output = new byte[length];

		hmac.update(label);
		hmac.update(seed1);
		byte[] A = hmac.doFinal(seed2);
		int copy = Math.min(length, A.length);
		System.arraycopy(A, 0, output, output.length - length, copy);
		length -= copy;

		while (length > 0) {
			hmac.update(A);
			hmac.update(label);
			hmac.update(seed1);
			A = hmac.doFinal(seed2);
			copy = Math.min(length, A.length);
			System.arraycopy(A, 0, output, output.length - length, copy);
			length -= copy;
		}
		return output;
	}
}