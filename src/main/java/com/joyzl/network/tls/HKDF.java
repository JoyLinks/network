package com.joyzl.network.tls;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.joyzl.network.Utility;

/**
 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)<br>
 * RFC 2104 HMAC: Keyed-Hashing for Message Authentication
 * 
 * @author ZhangXi 2024年12月22日
 */
public class HKDF implements CipherSuite {
	// https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html

	private MessageDigest digest;

	public HKDF() throws NoSuchAlgorithmException {
		digest = MessageDigest.getInstance("SHA-256");
	}

	/**
	 * 提取伪随机键
	 * 
	 * @param salt 随机值
	 * @param IKM 输入材料
	 * @return 伪随机键
	 */
	public byte[] extract(byte[] salt, byte[] IKM) throws NoSuchAlgorithmException, InvalidKeyException {
		if (salt == null || salt.length == 0) {
			// HashLen zeros
			salt = new byte[digest.getDigestLength()];
		}
		// PRK = HMAC-Hash(salt, IKM)
		final SecretKeySpec key = new SecretKeySpec(salt, "HmacSHA256");
		final Mac mac = Mac.getInstance("HmacSHA256");
		mac.getMacLength();
		mac.init(key);
		mac.update(IKM);
		return mac.doFinal();
	}

	public byte[] expand(byte[] PRK, byte[] info, int length) throws NoSuchAlgorithmException, InvalidKeyException {
		if (info == null) {
			info = new byte[0];
		}
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}
		/*-
		 * N = ceil(L/HashLen)
		 * T = T(1) | T(2) | T(3) | ... | T(N)
		 * OKM = first L octets of T
		 * where:
		 * T(0) = empty string (zero length)
		 * T(1) = HMAC-Hash(PRK, T(0) | info | 0x01)
		 * T(2) = HMAC-Hash(PRK, T(1) | info | 0x02)
		 * T(3) = HMAC-Hash(PRK, T(2) | info | 0x03)
		 * ...
		 */
		final SecretKeySpec key = new SecretKeySpec(PRK, "HmacSHA256");
		final Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(key);

		byte[] T = TLS.EMPTY_BYTES;
		int t, N = (int) Math.ceil(length / 1.0 / mac.getMacLength());
		ByteBuffer buffer = ByteBuffer.allocate(length);
		for (int n = 1; n <= N; n++) {
			mac.update(T);
			mac.update(info);
			mac.update((byte) n);
			T = mac.doFinal();

			t = Math.min(length, T.length);
			buffer.put(T, 0, t);
			length -= t;
		}

		return buffer.array();
	}

	public static void main(String[] argments) throws Exception {
		// Hash = SHA-256
		// IKM = 0x0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b (22 octets)
		final byte[] IKM = new byte[] { 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b };
		// salt = 0x000102030405060708090a0b0c (13 octets)
		final byte[] salt = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c };
		// info = 0xf0f1f2f3f4f5f6f7f8f9 (10 octets)
		final byte[] info = new byte[] { (byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9 };

		final HKDF hkdf = new HKDF();
		final byte[] PRK = hkdf.extract(salt, IKM);
		System.out.println(Utility.hex(PRK));
		// PRK=0x077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5(32octets)

		// L = 42
		// OKM=0x3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865(42octets)
		final byte[] OKM = hkdf.expand(PRK, info, 42);
		System.out.println(Utility.hex(OKM));
	}

	/*-
	 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)
	 *     extract-then-expand:提取-展开
	 *     HMAC-based Key Derivation Function (HKDF)
	 *     HKDF-Extract(salt, IKM) -> PRK
	 *     PRK = HMAC-Hash(salt, IKM)
	 *     
	 * HKDF-Expand-Label(Secret, Label, Context, Length) = HKDF-Expand(Secret, HkdfLabel, Length)
	 * 
	 * struct {
	 *     uint16 length = Length;
	 *     opaque label<7..255> = "tls13 " + Label;
	 *     opaque context<0..255> = Context;
	 * } HkdfLabel;
	 * 
	 * Derive-Secret(Secret, Label, Messages) = HKDF-Expand-Label(Secret, Label, Transcript-Hash(Messages), Hash.length)
	 * 
	 * 
	 */
}