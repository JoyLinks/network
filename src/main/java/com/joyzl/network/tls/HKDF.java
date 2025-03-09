package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * TLS 1.3 密钥提取与展开方法<br>
 * 
 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)<br>
 * RFC 2104 HMAC: Keyed-Hashing for Message Authentication
 * 
 * @author ZhangXi 2024年12月22日
 */
class HKDF extends TranscriptHash {

	/*-
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

	/** HMAC */
	private Mac hmac;

	/** Hash.length 00 */
	private byte[] ZEROS;

	public HKDF() {
	}

	public HKDF(String name) throws Exception {
		hmac(name);
	}

	/** 指定认证算法 */
	public void hmac(String name) throws Exception {
		hmac = Mac.getInstance(name);
		ZEROS = new byte[hmac.getMacLength()];
	}

	/** Hash.length */
	public int hashLength() {
		return hmac.getMacLength();
	}

	/** HKDF-Extract(salt, IKM) -> PRK=HMAC-Hash(salt, IKM) */
	public final byte[] extract(byte[] salt, byte[] IKM) throws Exception {
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

	/** HKDF-Expand */
	public final byte[] expand(byte[] PRK, byte[] info, int length) throws Exception {
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

	/** HKDF-Expand-Label(Secret, Label, Context, Length) */
	public byte[] expandLabel(byte[] secret, byte[] label, byte[] context, int length) throws Exception {
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
	 * Derive-Secret(Secret,Label,Messages)=HKDF-Expand-Label(Secret,Label,Transcript-Hash(Messages),Hash.length)
	 */
	public final byte[] deriveSecret(byte[] secret, byte[] label, byte[] hash) throws Exception {
		return expandLabel(secret, label, hash, hashLength());
	}
}