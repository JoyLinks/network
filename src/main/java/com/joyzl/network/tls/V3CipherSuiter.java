package com.joyzl.network.tls;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;

/**
 * TLS 1.3 密码套件执行消息加密与解密
 * 
 * <pre>
 * 构建套件
 * CipherSuiter cipher=new CipherSuiter(CipherSuite.TLS_AES_128_GCM_SHA256);
 * 重置密钥
 * cipher.encryptReset(...);
 * 附加数据长度
 * cipher.encryptAdditional(length);
 * cipher.encrypt(...);
 * cipher.encryptFinal();
 * 加密完成
 * 
 * 附加数据长度（每次附加都会重新构建随机码）
 * ...
 * 加密完成
 * </pre>
 * 
 * @author ZhangXi 2024年12月24日
 */
class V3CipherSuiter extends V3SecretCache implements CipherSuite {

	// https://www.bouncycastle.org/
	// https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html

	// 尝试并建立本地可用的密码套件
	final static short[] AVAILABLES;
	static {
		final V3CipherSuiter s = new V3CipherSuiter();
		short[] items = new short[0];
		for (short suite : ALL) {
			try {
				s.suite(suite);
				items = Arrays.copyOf(items, items.length + 1);
				items[items.length - 1] = suite;
			} catch (Exception e) {
				// 忽略此异常
			}
		}
		AVAILABLES = items;
	}

	////////////////////////////////////////////////////////////////////////////////

	private short code = TLS_NULL_WITH_NULL_NULL;

	private String secretKeyAlgorithm;
	private String transformation;
	private String digestAlgorithm;
	private String macAlgorithm;
	private int tagLength;
	private int keyLength;
	private int ivLength;

	private long encryptSequence = 0;
	private long decryptSequence = 0;
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	private byte[] encryptIV;
	private byte[] decryptIV;
	private Key encryptKey;
	private Key decryptKey;

	public V3CipherSuiter() {
		super();
	}

	public V3CipherSuiter(short code) throws Exception {
		super();
		suite(code);
	}

	public void suite(short code) throws Exception {
		switch (this.code = code) {
			// 1.3
			case TLS_AES_128_CCM_SHA256:
				secretKeyAlgorithm = "AES";
				transformation = "AES/CCM/NoPadding";
				digestAlgorithm = "SHA-256";
				macAlgorithm = "HmacSHA256";
				tagLength = 16;
				keyLength = 16;
				ivLength = 12;
				break;
			case TLS_AES_128_CCM_8_SHA256:
				secretKeyAlgorithm = "AES";
				transformation = "AES/CCM/NoPadding";
				digestAlgorithm = "SHA-256";
				macAlgorithm = "HmacSHA256";
				tagLength = 8;
				keyLength = 16;
				ivLength = 12;
				break;
			case TLS_AES_128_GCM_SHA256:
				secretKeyAlgorithm = "AES";
				transformation = "AES/GCM/NoPadding";
				digestAlgorithm = "SHA-256";
				macAlgorithm = "HmacSHA256";
				tagLength = 16;
				keyLength = 16;
				ivLength = 12;
				break;
			case TLS_AES_256_GCM_SHA384:
				secretKeyAlgorithm = "AES";
				transformation = "AES/GCM/NoPadding";
				digestAlgorithm = "SHA-384";
				macAlgorithm = "HmacSHA384";
				keyLength = 32;
				tagLength = 16;
				ivLength = 12;
				break;
			case TLS_CHACHA20_POLY1305_SHA256:
				secretKeyAlgorithm = "ChaCha20-Poly1305";
				transformation = "ChaCha20-Poly1305";
				digestAlgorithm = "SHA-256";
				macAlgorithm = "HmacSHA256";
				keyLength = 32;
				tagLength = 16;
				ivLength = 12;
				break;
			default:
				throw new NoSuchAlgorithmException("TLS:UNKNOWN cipher suiter " + code);
		}
		encryptKey = null;
		decryptKey = null;
		encryptCipher = Cipher.getInstance(transformation);
		decryptCipher = Cipher.getInstance(transformation);
		digest(digestAlgorithm);
		hmac(macAlgorithm);
	}

	/**
	 * AEAD 随机数
	 */
	private byte[] v13Nonce(byte[] IV, long sequence) {
		final byte[] nonce = new byte[ivLength];
		// 1.填充的序列号
		nonce[ivLength - 8] = (byte) (sequence >>> 56);
		nonce[ivLength - 7] = (byte) (sequence >>> 48);
		nonce[ivLength - 6] = (byte) (sequence >>> 40);
		nonce[ivLength - 5] = (byte) (sequence >>> 32);
		nonce[ivLength - 4] = (byte) (sequence >>> 24);
		nonce[ivLength - 3] = (byte) (sequence >>> 16);
		nonce[ivLength - 2] = (byte) (sequence >>> 8);
		nonce[ivLength - 1] = (byte) sequence;
		// 2.填充的序列号与writeIV异或
		// writeIV.length始终与iv_length相同
		for (int i = 0; i < ivLength; i++) {
			nonce[i] = (byte) (nonce[i] ^ IV[i]);
		}
		return nonce;
	}

	/**
	 * TLS 1.3 AEAD 附加数据
	 * 
	 * <pre>
	 * additional_data = TLSCiphertext.opaque_type ||
	 *                   TLSCiphertext.legacy_record_version ||
	 *                   TLSCiphertext.length
	 * -
	 * </pre>
	 */
	private byte[] v13AdditionalData(int length) {
		final byte[] data = new byte[] {
				// TLSCiphertext.opaque_type 1Byte
				Record.APPLICATION_DATA,
				// TLSCiphertext.legacy_record_version 2Byte
				(byte) (TLS.V12 >>> 8), (byte) TLS.V12,
				// TLSCiphertext.length 2Byte
				(byte) (length >>> 8), (byte) (length) };
		return data;
	}

	/**
	 * 加密是否已就绪
	 */
	public boolean encryptReady() {
		return encryptKey != null;
	}

	/**
	 * 解密是否已就绪
	 */
	public boolean decryptReady() {
		return decryptKey != null;
	}

	/**
	 * 重置加密密钥
	 */
	public void v13EncryptReset(byte[] secret) throws Exception {
		encryptKey = new SecretKeySpec(v13WriteKey(secret, keyLength()), secretKeyAlgorithm);
		encryptIV = v13WriteIV(secret, ivLength());
		encryptSequence = 0;
	}

	/**
	 * 重置解密密钥
	 */
	public void v13DecryptReset(byte[] secret) throws Exception {
		decryptKey = new SecretKeySpec(v13WriteKey(secret, keyLength()), secretKeyAlgorithm);
		decryptIV = v13WriteIV(secret, ivLength());
		decryptSequence = 0;
	}

	/**
	 * TLS 1.3 开始加密，AEAD 附加数据长度应包括Tag部分
	 */
	public void v13EncryptAEAD(int length) throws Exception {
		final AlgorithmParameterSpec spec = new GCMParameterSpec(tagLength * 8, v13Nonce(encryptIV, encryptSequence));
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, spec);
		encryptCipher.updateAAD(v13AdditionalData(length));
	}

	/**
	 * TLS 1.3 开始解密，AEAD 附加数据长度应包括Tag部分
	 */
	public void v13DecryptAEAD(int length) throws Exception {
		final AlgorithmParameterSpec spec = new GCMParameterSpec(tagLength * 8, v13Nonce(decryptIV, decryptSequence));
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, spec);
		decryptCipher.updateAAD(v13AdditionalData(length));
	}

	/** AES-GCM 加密记录限制 */
	final static long MAX_SEQUENCE = (long) Math.pow(2, 24.5);
	/** 空的堆内缓存对象，用于完成时的缺省参数 */
	final static ByteBuffer EMPTY = ByteBuffer.allocate(0);

	/**
	 * 加密
	 */
	public byte[] encrypt(byte[] data) {
		return encryptCipher.update(data);
	}

	/**
	 * 解密
	 */
	public byte[] decrypt(byte[] data) {
		return decryptCipher.update(data);
	}

	/**
	 * 加密完成
	 */
	public byte[] encryptFinal() throws Exception {
		encryptSequence++;
		return encryptCipher.doFinal();
	}

	/**
	 * 加密完成
	 */
	public byte[] encryptFinal(byte[] data) throws Exception {
		encryptSequence++;
		return encryptCipher.doFinal(data);
	}

	/**
	 * 解密完成
	 */
	public byte[] decryptFinal() throws Exception {
		decryptSequence++;
		return decryptCipher.doFinal();
	}

	/**
	 * 加密，加密数据并输出到缓存尾部
	 */
	public void encryptUpdate(byte[] in, DataBuffer out) throws Exception {
		in = encryptCipher.update(in);
		if (in != null) {
			out.write(in);
		}
	}

	/**
	 * 加密，加密数据并输出到缓存尾部
	 */
	public void encryptUpdate(byte[] in, DataBuffer out, int length) throws Exception {
		in = encryptCipher.update(in, 0, length);
		if (in != null) {
			out.write(in);
		}
	}

	/**
	 * 加密，读取指定数量的输入数据，加密后的数据输出到输出缓存的尾部
	 */
	public void encryptUpdate(DataBuffer in, DataBuffer out, int length) throws Exception {
		// 注意：Cipher.update()方法内部偶尔会调用output参数的ByteBuffer.mark()方法

		int size, reads, exclude;
		ByteBuffer o, i;
		while (length > 0) {
			i = in.read();
			if (i.remaining() > length) {
				exclude = i.remaining() - length;
				i.limit(i.limit() - exclude);
			} else {
				exclude = 0;
			}
			reads = i.remaining();
			while (i.remaining() > 0) {
				o = out.write();
				size = encryptCipher.getOutputSize(i.remaining());
				if (size > o.remaining()) {
					do {
						// 减除超出数量，测试输出数量
						size = i.remaining() - (size - o.remaining());
					} while (encryptCipher.getOutputSize(size) > o.remaining());

					if (size > 0) {
						// 调整输入数量
						size = i.remaining() - size;
						i.limit(i.limit() - size);
						out.written(encryptCipher.update(i, o));
						i.limit(i.limit() + size);
					} else {
						out.written(Integer.MIN_VALUE);
					}
				} else {
					out.written(encryptCipher.update(i, o));
				}
			}
			i.limit(i.limit() + exclude);
			length -= reads;
			in.read(reads);
		}
	}

	/**
	 * 加密，读取全部输入数据完成加密，加密后的数据输出到输出缓存的尾部
	 */
	public void encryptFinal(DataBuffer in, DataBuffer out) throws Exception {
		// 注意：Cipher.update()方法内部偶尔会调用output参数的ByteBuffer.mark()方法

		int size, reads;
		ByteBuffer o, i;
		while (in.readable() > 0) {
			i = in.read();
			reads = i.remaining();
			while (i.remaining() > 0) {
				o = out.write();
				size = encryptCipher.getOutputSize(i.remaining());
				if (size > o.remaining()) {
					do {
						// 减除超出数量，测试输出数量
						size = i.remaining() - (size - o.remaining());
					} while (encryptCipher.getOutputSize(size) > o.remaining());

					if (size > 0) {
						// 调整输入数量
						size = i.remaining() - size;
						i.limit(i.limit() - size);
						out.written(encryptCipher.update(i, o));
						i.limit(i.limit() + size);
					} else {
						out.written(Integer.MIN_VALUE);
					}
				} else {
					out.written(encryptCipher.update(i, o));
				}
			}
			in.read(reads);
		}

		o = out.write();
		size = encryptCipher.getOutputSize(0);
		if (size > o.remaining()) {
			out.written(Integer.MIN_VALUE);
			o = out.write();
		}
		out.written(encryptCipher.doFinal(EMPTY, o));
		encryptSequence++;
	}

	/**
	 * 加密，完成加密，加密后的数据输出到输出缓存的尾部
	 */
	public void encryptFinal(DataBuffer out) throws Exception {
		ByteBuffer o = out.write();
		int size = encryptCipher.getOutputSize(0);
		if (size > o.remaining()) {
			out.written(Integer.MIN_VALUE);
			o = out.write();
		}
		out.written(encryptCipher.doFinal(EMPTY, o));
		encryptSequence++;
	}

	// GCM模式解码时须等待最后的Tag校验完成才会输出解码后的数据
	// 这将导致必须构建足够长度的ByteBuffer以接收输出结果
	// 强烈建议：Java应逐块解码输出结果，不应等待最终校验
	ByteBuffer decryptOutput = null;

	ByteBuffer decryptBuffer() {
		if (decryptOutput == null) {
			int size = decryptCipher.getOutputSize(Record.CIPHERTEXT_MAX);
			decryptOutput = ByteBuffer.allocateDirect(size);
		} else {
			decryptOutput.clear();
		}
		return decryptOutput;
	}

	/**
	 * 解密，缓存数据将被解密后的数据替代
	 */
	public void decryptFinal(DataBuffer buffer) throws Exception {
		final ByteBuffer output = decryptBuffer();
		DataBufferUnit i = buffer.take();
		while (i != null) {
			decryptCipher.update(i.buffer(), output);
			i.release();
			i = buffer.take();
		}

		decryptCipher.doFinal(EMPTY, output);
		decryptSequence++;

		buffer.append(output.flip());
	}

	/**
	 * 解密
	 */
	public void decryptFinal(DataBuffer in, DataBuffer out) throws Exception {
		final ByteBuffer output = decryptBuffer();
		DataBufferUnit i = in.take();
		while (i != null) {
			decryptCipher.update(i.buffer(), output);
			i.release();
			i = in.take();
		}

		decryptCipher.doFinal(EMPTY, output);
		decryptSequence++;

		out.append(output.flip());
	}

	/**
	 * 解密
	 */
	public void decryptFinal(DataBuffer in, DataBuffer out, int length) throws Exception {
		final ByteBuffer output = decryptBuffer();

		int size;
		ByteBuffer i;
		while (length > 0) {
			i = in.read();
			if (i.remaining() <= length) {
				length -= size = i.remaining();
				decryptCipher.update(i, output);
				in.read(size);
			} else {
				size = i.remaining() - length;
				i.limit(i.limit() - size);
				decryptCipher.update(i, output);
				i.limit(i.limit() + size);
				in.read(length);
				length = 0;
			}
		}

		decryptCipher.doFinal(EMPTY, output);
		decryptSequence++;

		out.append(output.flip());
	}

	/**
	 * 指示是否应更新密钥，密钥使用次数到达限制
	 */
	public boolean encryptLimit() {
		if (encryptSequence >= MAX_SEQUENCE) {
			return true;
		}
		return false;
	}

	/**
	 * 指示是否应更新密钥，密钥使用次数到达限制
	 */
	public boolean decryptLimit() {
		if (decryptSequence >= MAX_SEQUENCE) {
			return true;
		}
		return false;
	}

	/**
	 * 加密序列号
	 */
	public long encryptSequence() {
		return encryptSequence;
	}

	/**
	 * 解密序列号
	 */
	public long decryptSequence() {
		return decryptSequence;
	}

	/**
	 * 消息标记长度(Byte)
	 */
	public int tagLength() {
		return tagLength;
	}

	/**
	 * 消息密钥长度(Byte)
	 */
	public int keyLength() {
		return keyLength;
	}

	/**
	 * 消息随机数长度(Byte)
	 */
	public int ivLength() {
		return ivLength;
	}

	public short suite() {
		return code;
	}
}