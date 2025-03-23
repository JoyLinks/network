package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;
import com.joyzl.network.codec.Binary;

/**
 * 密钥提取与展开方法<br>
 * 
 * RFC 5869 HMAC-based Extract-and-Expand Key Derivation Function (HKDF)<br>
 * RFC 2104 HMAC: Keyed-Hashing for Message Authentication
 * 
 * @author ZhangXi 2024年12月22日
 */
class PRF_HKDF extends TranscriptHash {

	/** HMAC */
	private Mac hmac;

	/** Hash.length 00 */
	private byte[] ZEROS;

	public PRF_HKDF() {
	}

	public PRF_HKDF(String name) throws Exception {
		hmac(name);
	}

	/** 指定认证算法 */
	public void hmac(String name) throws Exception {
		hmac = Mac.getInstance(name);
		ZEROS = new byte[hmac.getMacLength()];
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
	protected final byte[] v13Extract(byte[] salt, byte[] IKM) throws Exception {
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
	protected final byte[] v13Expand(byte[] PRK, byte[] info, int length) throws Exception {
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
	protected byte[] v13ExpandLabel(byte[] secret, byte[] label, byte[] context, int length) throws Exception {
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
	protected final byte[] v13DeriveSecret(byte[] secret, byte[] label, byte[] hash) throws Exception {
		return v13ExpandLabel(secret, label, hash, hashLength());
	}

	////////////////////////////////////////////////////////////////////////////////

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
	protected final byte[] v12PHash(byte[] secret, byte[] seed, int length) throws InvalidKeyException {
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

	/** TLS 1.2 PRF(secret, label, seed) = P_hash(secret, label + seed) */
	protected final byte[] v12PRF(byte[] secret, byte[] label, byte[] seed, int length) throws InvalidKeyException {
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

	/**
	 * TLS 1.2 PRF(secret, label, seed) = P_hash(secret, label + seed1 +seed2)
	 */
	protected final byte[] v12PRF(byte[] secret, byte[] label, byte[] seed1, byte[] seed2, int length) throws InvalidKeyException {
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

	/**
	 * TLS 1.2
	 * 
	 * <pre>
	 * MAC(MAC_write_key, seq_num +
	 *                    TLSCompressed.type +
	 *                    TLSCompressed.version +
	 *                    TLSCompressed.length +
	 *                    TLSCompressed.fragment);
	 * 由于压缩未启用TLSCompressed = TLSPlaintext
	 * </pre>
	 */
	protected final byte[] v12MAC(byte[] key, long seqnum, byte type, short version, int length, DataBuffer fragment) throws InvalidKeyException {
		hmac.reset();
		hmac.init(new SecretKeySpec(key, hmac.getAlgorithm()));

		final byte[] temp = new byte[8];
		Binary.put(temp, 0, seqnum);
		hmac.update(temp);

		hmac.update(type);

		Binary.put(temp, 0, version);
		hmac.update(temp, 0, 2);

		Binary.put(temp, 0, (short) length);
		hmac.update(temp, 0, 2);

		// fragment
		DataBufferUnit unit = fragment.head();
		while (unit != null && length > 0) {
			unit.buffer().mark();
			if (unit.readable() <= length) {
				length -= unit.readable();
				hmac.update(unit.buffer());
			} else {
				length = unit.readable() - length;
				unit.writeIndex(unit.writeIndex() - length);
				hmac.update(unit.buffer());
				unit.writeIndex(unit.writeIndex() + length);
				length = 0;
			}
			unit.buffer().reset();
			unit = unit.next();
		}
		
		return hmac.doFinal();
	}
}