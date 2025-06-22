/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TLS 1.1 1.0 密钥提取与展开方法
 * 
 * @author ZhangXi 2024年12月22日
 */
class V0PRF extends V0TranscriptHash {

	/** HMAC */
	private Mac md5;
	private Mac sha1;

	/** HMAC.length */
	public int hmacLength() {
		return md5.getMacLength();
	}

	public void initialize() throws NoSuchAlgorithmException {
		super.initialize();
		if (md5 == null || sha1 == null) {
			md5 = Mac.getInstance("HmacMD5");
			sha1 = Mac.getInstance("HmacSHA1");
		}
	}

	/*-
	 * TLS 1.1 1.0 PRF 基础方法
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
	 * L_S = length in bytes of secret;
	 * L_S1 = L_S2 = ceil(L_S / 2);
	 * 
	 * PRF(secret, label, seed) = P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed);
	 */

	protected final byte[] prf(byte[] secret, byte[] seed, int length) throws Exception {
		int s = (secret.length + 1) / 2;
		md5.init(new SecretKeySpec(secret, 0, s, md5.getAlgorithm()));
		sha1.init(new SecretKeySpec(secret, secret.length - s, s, sha1.getAlgorithm()));

		final byte[] output1 = new byte[length];
		final byte[] output2 = new byte[length];

		byte[] B = null, A = null;
		while (length > 0) {
			if (A == null) {
				md5.update(seed);
				A = md5.doFinal();
			} else {
				md5.update(A);
				md5.doFinal(A, 0);
			}
			md5.update(A);
			md5.update(seed);
			if (B == null) {
				B = md5.doFinal();
			} else {
				md5.doFinal(B, 0);
			}
			s = Math.min(length, B.length);
			System.arraycopy(B, 0, output1, output1.length - length, s);
			length -= s;
		}

		B = A = null;
		length = output2.length;
		while (length > 0) {
			if (A == null) {
				sha1.update(seed);
				A = sha1.doFinal();
			} else {
				sha1.update(A);
				sha1.doFinal(A, 0);
			}
			sha1.update(A);
			sha1.update(seed);
			if (B == null) {
				B = sha1.doFinal();
			} else {
				sha1.doFinal(B, 0);
			}
			s = Math.min(length, B.length);
			System.arraycopy(B, 0, output2, output2.length - length, s);
			length -= s;
		}

		// P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed);
		length = output1.length;
		for (s = 0; s < length; s++) {
			output1[s] ^= output2[s];
		}
		return output1;
	}

	protected final byte[] prf(byte[] secret, byte[] label, byte[] seed, int length) throws Exception {
		int s = (secret.length + 1) / 2;
		md5.init(new SecretKeySpec(secret, 0, s, md5.getAlgorithm()));
		sha1.init(new SecretKeySpec(secret, secret.length - s, s, sha1.getAlgorithm()));

		final byte[] output1 = new byte[length];
		final byte[] output2 = new byte[length];

		byte[] B = null, A = null;
		while (length > 0) {
			if (A == null) {
				md5.update(label);
				md5.update(seed);
				A = md5.doFinal();
			} else {
				md5.update(A);
				md5.doFinal(A, 0);
			}
			md5.update(A);
			md5.update(label);
			md5.update(seed);
			if (B == null) {
				B = md5.doFinal();
			} else {
				md5.doFinal(B, 0);
			}
			s = Math.min(length, B.length);
			System.arraycopy(B, 0, output1, output1.length - length, s);
			length -= s;
		}

		B = A = null;
		length = output2.length;
		while (length > 0) {
			if (A == null) {
				sha1.update(label);
				sha1.update(seed);
				A = sha1.doFinal();
			} else {
				sha1.update(A);
				sha1.doFinal(A, 0);
			}
			sha1.update(A);
			sha1.update(label);
			sha1.update(seed);
			if (B == null) {
				B = sha1.doFinal();
			} else {
				sha1.doFinal(B, 0);
			}
			s = Math.min(length, B.length);
			System.arraycopy(B, 0, output2, output2.length - length, s);
			length -= s;
		}

		// P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed);
		length = output1.length;
		for (s = 0; s < length; s++) {
			output1[s] ^= output2[s];
		}
		return output1;
	}

	protected final byte[] prf(byte[] secret, byte[] label, byte[] seed1, byte[] seed2, int length) throws Exception {
		int s = (secret.length + 1) / 2;
		md5.init(new SecretKeySpec(secret, 0, s, md5.getAlgorithm()));
		sha1.init(new SecretKeySpec(secret, secret.length - s, s, sha1.getAlgorithm()));

		final byte[] output1 = new byte[length];
		final byte[] output2 = new byte[length];

		byte[] B = null, A = null;
		while (length > 0) {
			if (A == null) {
				md5.update(label);
				md5.update(seed1);
				md5.update(seed2);
				A = md5.doFinal();
			} else {
				md5.update(A);
				md5.doFinal(A, 0);
			}
			md5.update(A);
			md5.update(label);
			md5.update(seed1);
			md5.update(seed2);
			if (B == null) {
				B = md5.doFinal();
			} else {
				md5.doFinal(B, 0);
			}
			s = Math.min(length, B.length);
			System.arraycopy(B, 0, output1, output1.length - length, s);
			length -= s;
		}

		B = A = null;
		length = output2.length;
		while (length > 0) {
			if (A == null) {
				sha1.update(label);
				sha1.update(seed1);
				sha1.update(seed2);
				A = sha1.doFinal();
			} else {
				sha1.update(A);
				sha1.doFinal(A, 0);
			}
			sha1.update(A);
			sha1.update(label);
			sha1.update(seed1);
			sha1.update(seed2);
			if (B == null) {
				B = sha1.doFinal();
			} else {
				sha1.doFinal(B, 0);
			}
			s = Math.min(length, B.length);
			System.arraycopy(B, 0, output2, output2.length - length, s);
			length -= s;
		}

		// P_MD5(S1, label + seed) XOR P_SHA-1(S2, label + seed);
		length = output1.length;
		for (s = 0; s < length; s++) {
			output1[s] ^= output2[s];
		}
		return output1;
	}
}