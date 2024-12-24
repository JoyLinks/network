package com.joyzl.network.tls;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;

/**
 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)<br>
 * RFC 2104 HMAC: Keyed-Hashing for Message Authentication
 * 
 * @author ZhangXi 2024年12月22日
 */
public class HKDF extends TranscriptHash {

	// https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html

	/*-
	 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)
	 *     extract-then-expand:提取-展开
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

	private byte[] PRK;

	public HKDF(short code) throws NoSuchAlgorithmException {
		super(code);
	}

	/** HKDF-Extract(salt, IKM) -> PRK=HMAC-Hash(salt, IKM) */
	public byte[] extract(byte[] salt, byte[] IKM) throws NoSuchAlgorithmException, InvalidKeyException {
		if (salt == null || salt.length == 0) {
			// HashLen zeros
			salt = new byte[hmac.getMacLength()];
		}
		// PRK = HMAC-Hash(salt, IKM)

		hmac.reset();
		hmac.init(new SecretKeySpec(salt, hmac.getAlgorithm()));
		hmac.update(IKM);
		return PRK = hmac.doFinal();
	}

	/** HKDF-Expand */
	public byte[] expand(byte[] PRK, byte[] info, int length) throws NoSuchAlgorithmException, InvalidKeyException {
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
	public byte[] expandLabel(byte[] label, byte[] context, int length) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		if (length <= 0) {
			throw new IllegalArgumentException("length");
		}
		if (label == null) {
			label = TLS.EMPTY_BYTES;
		}
		if (context == null) {
			context = TLS.EMPTY_BYTES;
		}

		final SecretKeySpec key = new SecretKeySpec(PRK, hmac.getAlgorithm());
		hmac.reset();
		hmac.init(key);

		final byte[] OKM = new byte[length];
		int t, N = (int) Math.ceil(length / 1.0 / hmac.getMacLength());
		byte[] T = TLS.EMPTY_BYTES;
		for (int n = 1; n <= N; n++) {
			hmac.update(T);
			// HkdfLabel uint16 length
			hmac.update((byte) (length >>> 8));
			hmac.update((byte) (length));
			// HkdfLabel "tls13 " + Label;
			hmac.update(TLS13_);
			hmac.update(label);
			// HkdfLabel Context
			hmac.update(context);
			// T(n)
			hmac.update((byte) n);
			T = hmac.doFinal();

			t = Math.min(length, T.length);
			System.arraycopy(T, 0, OKM, OKM.length - length, t);
			length -= t;
		}

		return OKM;
	}

	/** Derive-Secret */
	public byte[] derive(byte[] label) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		return expandLabel(label, digest(), hmac.getMacLength());
	}

	/*-
	 * +-----------+-------------------------+-----------------------------+
	 * | Mode      | Handshake Context       | Base Key                    |
	 * +-----------+-------------------------+-----------------------------+
	 * | Server    | ClientHello ... later   | server_handshake_traffic_   |
	 * |           | of EncryptedExtensions/ | secret                      |
	 * |           | CertificateRequest      |                             |
	 * |           |                         |                             |
	 * | Client    | ClientHello ... later   | client_handshake_traffic_   |
	 * |           | of server               | secret                      |
	 * |           | Finished/EndOfEarlyData |                             |
	 * |           |                         |                             |
	 * | Post-     | ClientHello ... client  | client_application_traffic_ |
	 * | Handshake | Finished +              | secret_N                    |
	 * |           | CertificateRequest      |                             |
	 * +-----------+-------------------------+-----------------------------+
	 */

	final static byte[] FINISHED = "finished".getBytes(StandardCharsets.US_ASCII);

	public byte[] finishedVerifyData() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		// finished_key=HKDF-Expand-Label(BaseKey,"finished","",Hash.length)
		// verify_data=HMAC(finished_key,Transcript-Hash(Handshake-Context,Certificate*,CertificateVerify*))

		final byte[] finished_key = expandLabel(FINISHED, TLS.EMPTY_BYTES, hmac.getMacLength());
		final SecretKeySpec key = new SecretKeySpec(finished_key, hmac.getAlgorithm());

		hmac.reset();
		hmac.init(key);
		hmac.update(digest());
		return hmac.doFinal();
	}
}