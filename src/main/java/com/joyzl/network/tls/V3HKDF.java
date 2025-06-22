/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 密钥提取与展开方法<br>
 * 
 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)<br>
 * RFC 2104 HMAC: Keyed-Hashing for Message Authentication
 * 
 * @author ZhangXi 2024年12月22日
 */
class V3HKDF extends V2TranscriptHash {

	/** HMAC */
	private Mac hmac;

	/** Hash.length 00 */
	private byte[] ZEROS;

	public void initialize(String mac, String digest) throws Exception {
		super.initialize(digest);
		if (hmac == null || !mac.equals(hmac.getAlgorithm())) {
			hmac = Mac.getInstance(mac);
			ZEROS = new byte[hmac.getMacLength()];
		} else {
			hmac.reset();
		}
	}

	/** HMAC.length */
	public int hmacLength() {
		return hmac.getMacLength();
	}

	////////////////////////////////////////////////////////////////////////////////

	/*-
	 * TLS 1.3 HKDF 基础方法
	 * 
	 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)
	 *     extract-then-expand:提取-展开
	 *     salt:盐
	 *     IKM:密钥材料
	 *
	 *     HMAC-based Key Derivation Function (HKDF)
	 *     HKDF-Extract(salt, IKM) -> PRK
	 *     PRK = HMAC-Hash(salt, IKM)
	 *
	 *     HKDF-Expand
	 *     N = ceil(L/HashLen)
	 *     T = T(1) | T(2) | T(3) | ... | T(N)
	 *     OKM = first L octets of T
	 *     where:
	 *     T(0) = empty string (zero length)
	 *     T(1) = HMAC-Hash(PRK, T(0) | info | 0x01)
	 *     T(2) = HMAC-Hash(PRK, T(1) | info | 0x02)
	 *     T(3) = HMAC-Hash(PRK, T(2) | info | 0x03)
	 *     ...
	 */

	/** TLS 1.3 HKDF-Extract(salt, IKM) -> PRK=HMAC-Hash(salt, IKM) */
	protected final byte[] extract(byte[] salt, byte[] IKM) throws Exception {
		if (salt == null || salt.length == 0) {
			// HashLen zeros
			salt = ZEROS;
		}
		if (IKM == null || IKM.length == 0) {
			// HashLen zeros
			IKM = ZEROS;
		}

		// PRK = HMAC-Hash(salt, IKM)

		hmac.reset();
		hmac.init(new SecretKeySpec(salt, hmac.getAlgorithm()));
		hmac.update(IKM);
		return hmac.doFinal();
	}

	/** TLS 1.3 HKDF-Expand */
	protected final byte[] expand(byte[] PRK, byte[] info, int length) throws Exception {
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}
		if (info == null) {
			info = TLS.EMPTY_BYTES;
		}

		hmac.reset();
		hmac.init(new SecretKeySpec(PRK, hmac.getAlgorithm()));

		final byte[] OKM = new byte[length];
		int t, N = (int) Math.ceil(length / 1.0 / hmac.getMacLength());
		byte[] T = TLS.EMPTY_BYTES;
		for (int n = 1; n <= N; n++) {
			hmac.update(T);
			hmac.update(info);
			hmac.update((byte) n);
			T = hmac.doFinal();

			t = Math.min(length, T.length);
			System.arraycopy(T, 0, OKM, OKM.length - length, t);
			length -= t;
		}
		return OKM;
	}

	final static byte[] TLS13_ = "tls13 ".getBytes(StandardCharsets.US_ASCII);

	/*-
	 * HKDF-Expand-Label(Secret, Label, Context, Length) = HKDF-Expand(Secret, HkdfLabel, Length)
	 * 
	 * struct {
	 *     uint16 length = Length;
	 *     opaque label<7..255> = "tls13 " + Label;
	 *     opaque context<0..255> = Context;
	 * } HkdfLabel;
	 * 
	 * Derive-Secret(Secret, Label, Messages) = HKDF-Expand-Label(Secret, Label, Transcript-Hash(Messages), Hash.length)
	 */

	/** TLS 1.3 HKDF-Expand-Label(Secret, Label, Context, Length) */
	protected byte[] expandLabel(byte[] secret, byte[] label, byte[] context, int length) throws Exception {
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}
		if (label == null) {
			label = TLS.EMPTY_BYTES;
		}
		if (context == null) {
			context = TLS.EMPTY_BYTES;
		}

		hmac.reset();
		hmac.init(new SecretKeySpec(secret, hmac.getAlgorithm()));

		final byte[] OKM = new byte[length];
		int t, N = (int) Math.ceil(length / 1.0 / hmac.getMacLength());
		byte[] T = TLS.EMPTY_BYTES;
		for (int n = 1; n <= N; n++) {
			hmac.update(T);
			// info begin
			// HkdfLabel uint16 length
			hmac.update((byte) (OKM.length >>> 8));
			hmac.update((byte) (OKM.length));
			// HkdfLabel "tls13 " + Label;
			hmac.update((byte) (TLS13_.length + label.length));
			hmac.update(TLS13_);
			hmac.update(label);
			// HkdfLabel Context
			hmac.update((byte) (context.length));
			hmac.update(context);
			// info end
			// T(n)
			hmac.update((byte) n);
			T = hmac.doFinal();

			t = Math.min(length, T.length);
			System.arraycopy(T, 0, OKM, OKM.length - length, t);
			length -= t;
		}

		return OKM;
	}

	/**
	 * TLS 1.3
	 * Derive-Secret(Secret,Label,Messages)=HKDF-Expand-Label(Secret,Label,Transcript-Hash(Messages),Hash.length)
	 */
	protected final byte[] deriveSecret(byte[] secret, byte[] label, byte[] hash) throws Exception {
		return expandLabel(secret, label, hash, hashLength());
	}
}