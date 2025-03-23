package com.joyzl.network.tls;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;
import com.joyzl.network.codec.Binary;

/**
 * 密码套件执行消息加密与解密
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
class V2CipherSuiter extends V2SecretCache implements CipherSuite {

	// KeyExchangeAlgorithm

	final static int RSA = 0;
	final static int DHE_DSS = 1;
	final static int DHE_RSA = 2;
	final static int DH_DSS = 3;
	final static int DH_RSA = 4;
	final static int DH_ANON = 5;

	////////////////////////////////////////////////////////////////////////////////

	// 尝试并建立本地可用的密码套件
	final static short[] AVAILABLES;
	static {
		final CipherSuiter s = new CipherSuiter();
		short[] items = new short[0];
		for (short suite : V12) {
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
	private int blockLength;
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
	private Mac mac;

	public V2CipherSuiter() {
		super();
	}

	public V2CipherSuiter(short code) throws Exception {
		super();
		suite(code);
	}

	public void suite(short code) throws Exception {
		switch (this.code = code) {
			case TLS_KRB5_WITH_3DES_EDE_CBC_MD5:
				secretKeyAlgorithm = "DESede";
				transformation = "DESede/CBC/NoPadding";
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 8;
				keyLength = 24;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA:
			case TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
			case TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
			case TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
			case TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
			case TLS_KRB5_WITH_3DES_EDE_CBC_SHA:
			case TLS_RSA_WITH_3DES_EDE_CBC_SHA:
				secretKeyAlgorithm = "DESede";
				transformation = "DESede/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 8;
				keyLength = 24;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_DH_ANON_WITH_AES_128_CBC_SHA:
			case TLS_DH_DSS_WITH_AES_128_CBC_SHA:
			case TLS_DH_RSA_WITH_AES_128_CBC_SHA:
			case TLS_DHE_DSS_WITH_AES_128_CBC_SHA:
			case TLS_DHE_RSA_WITH_AES_128_CBC_SHA:
			case TLS_RSA_WITH_AES_128_CBC_SHA:
				secretKeyAlgorithm = "AES";
				transformation = "AES/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 16;
				keyLength = 16;
				tagLength = 0;
				ivLength = 16;
				break;
			case TLS_DH_ANON_WITH_AES_128_CBC_SHA256:
			case TLS_DH_DSS_WITH_AES_128_CBC_SHA256:
			case TLS_DH_RSA_WITH_AES_128_CBC_SHA256:
			case TLS_DHE_DSS_WITH_AES_128_CBC_SHA256:
			case TLS_DHE_RSA_WITH_AES_128_CBC_SHA256:
			case TLS_RSA_WITH_AES_128_CBC_SHA256:
				secretKeyAlgorithm = "AES";
				transformation = "AES/CBC/NoPadding";
				macAlgorithm = "HmacSHA256";
				digestAlgorithm = "SHA-256";
				blockLength = 16;
				keyLength = 16;
				tagLength = 0;
				ivLength = 16;
				break;
			case TLS_DH_ANON_WITH_AES_256_CBC_SHA:
			case TLS_DH_DSS_WITH_AES_256_CBC_SHA:
			case TLS_DH_RSA_WITH_AES_256_CBC_SHA:
			case TLS_DHE_DSS_WITH_AES_256_CBC_SHA:
			case TLS_DHE_RSA_WITH_AES_256_CBC_SHA:
			case TLS_RSA_WITH_AES_256_CBC_SHA:
				secretKeyAlgorithm = "AES";
				transformation = "AES/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 16;
				keyLength = 32;
				tagLength = 0;
				ivLength = 16;
				break;
			case TLS_DH_ANON_WITH_AES_256_CBC_SHA256:
			case TLS_DH_DSS_WITH_AES_256_CBC_SHA256:
			case TLS_DH_RSA_WITH_AES_256_CBC_SHA256:
			case TLS_DHE_DSS_WITH_AES_256_CBC_SHA256:
			case TLS_DHE_RSA_WITH_AES_256_CBC_SHA256:
			case TLS_RSA_WITH_AES_256_CBC_SHA256:
				secretKeyAlgorithm = "AES";
				transformation = "AES/CBC/NoPadding";
				macAlgorithm = "HmacSHA256";
				digestAlgorithm = "SHA-256";
				blockLength = 16;
				keyLength = 32;
				tagLength = 0;
				ivLength = 16;
				break;
			case TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5:
				secretKeyAlgorithm = "DES";
				transformation = "DES/CBC/NoPadding";
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 8;
				keyLength = 8;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA:
				secretKeyAlgorithm = "DES";
				transformation = "DES/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 8;
				keyLength = 8;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_KRB5_WITH_DES_CBC_MD5:
				secretKeyAlgorithm = "DES";
				transformation = "DES/CBC/NoPadding";
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 8;
				keyLength = 8;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_DH_ANON_WITH_DES_CBC_SHA:
			case TLS_DH_DSS_WITH_DES_CBC_SHA:
			case TLS_DH_RSA_WITH_DES_CBC_SHA:
			case TLS_DHE_DSS_WITH_DES_CBC_SHA:
			case TLS_DHE_RSA_WITH_DES_CBC_SHA:
			case TLS_KRB5_WITH_DES_CBC_SHA:
			case TLS_RSA_WITH_DES_CBC_SHA:
				secretKeyAlgorithm = "DES";
				transformation = "DES/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 8;
				keyLength = 8;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_DH_ANON_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_RSA_EXPORT_WITH_DES40_CBC_SHA:
				secretKeyAlgorithm = "DES";
				transformation = "DES/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 8;
				keyLength = 8;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_KRB5_WITH_IDEA_CBC_MD5:
				secretKeyAlgorithm = "IDEA";
				transformation = "IDEA/CBC/NoPadding";
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 8;
				keyLength = 16;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_KRB5_WITH_IDEA_CBC_SHA:
			case TLS_RSA_WITH_IDEA_CBC_SHA:
				secretKeyAlgorithm = "IDEA";
				transformation = "IDEA/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 8;
				keyLength = 16;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_RSA_WITH_NULL_MD5:
				secretKeyAlgorithm = null;
				transformation = null;
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 0;
				keyLength = 0;
				tagLength = 0;
				ivLength = 0;
				break;
			case TLS_RSA_WITH_NULL_SHA:
				secretKeyAlgorithm = null;
				transformation = null;
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 0;
				keyLength = 0;
				tagLength = 0;
				ivLength = 0;
				break;
			case TLS_RSA_WITH_NULL_SHA256:
				secretKeyAlgorithm = null;
				transformation = null;
				macAlgorithm = "HmacSHA256";
				digestAlgorithm = "SHA-256";
				blockLength = 0;
				keyLength = 0;
				tagLength = 0;
				ivLength = 0;
				break;
			case TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5:
			case TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5:
				secretKeyAlgorithm = "RC2";
				transformation = "RC2/CBC/NoPadding";
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 8;
				keyLength = 5;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA:
				secretKeyAlgorithm = "RC2";
				transformation = "RC2/CBC/NoPadding";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 8;
				keyLength = 5;
				tagLength = 0;
				ivLength = 8;
				break;
			case TLS_DH_ANON_WITH_RC4_128_MD5:
			case TLS_KRB5_WITH_RC4_128_MD5:
			case TLS_RSA_WITH_RC4_128_MD5:
				secretKeyAlgorithm = "RC4";
				transformation = "RC4";
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 0;
				keyLength = 16;
				tagLength = 0;
				ivLength = 0;
				break;
			case TLS_KRB5_WITH_RC4_128_SHA:
			case TLS_RSA_WITH_RC4_128_SHA:
				secretKeyAlgorithm = "RC4";
				transformation = "RC4";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 0;
				keyLength = 16;
				tagLength = 0;
				ivLength = 0;
				break;
			case TLS_DH_ANON_EXPORT_WITH_RC4_40_MD5:
			case TLS_KRB5_EXPORT_WITH_RC4_40_MD5:
			case TLS_RSA_EXPORT_WITH_RC4_40_MD5:
				secretKeyAlgorithm = "RC4";
				transformation = "RC4";
				macAlgorithm = "HmacMD5";
				digestAlgorithm = "MD5";
				blockLength = 0;
				keyLength = 5;
				tagLength = 0;
				ivLength = 0;
				break;
			case TLS_KRB5_EXPORT_WITH_RC4_40_SHA:
				secretKeyAlgorithm = "RC4";
				transformation = "RC4";
				macAlgorithm = "HmacSHA1";
				digestAlgorithm = "SHA-1";
				blockLength = 0;
				keyLength = 5;
				tagLength = 0;
				ivLength = 0;
				break;
			case TLS_NULL_WITH_NULL_NULL:
				secretKeyAlgorithm = null;
				transformation = null;
				macAlgorithm = null;
				digestAlgorithm = null;
				blockLength = 0;
				keyLength = 0;
				tagLength = 0;
				ivLength = 0;
				break;
			default:
				throw new NoSuchAlgorithmException("TLS:UNKNOWN cipher suiter " + code);
		}
		encryptKey = null;
		decryptKey = null;
		encryptCipher = Cipher.getInstance(transformation);
		decryptCipher = Cipher.getInstance(transformation);

		// TranscriptHash
		// extended_master_secret:session_hash
		// 扩展密钥使用至少SHA256(RFC7627)

		// PRF:所有密码套件至少使用SHA256
		// PRF:如果密码套件指定的算法安全级别高于SHA256，则采用SHA384算法
		if ("HmacMD5".equalsIgnoreCase(macAlgorithm) //
				|| "HmacSHA1".equalsIgnoreCase(macAlgorithm)) {
			hmac("HmacSHA256");
			digest("SHA-256");
		} else {
			hmac(macAlgorithm);
			digest(digestAlgorithm);
		}

		// 消息签名使用密码套件算法
		// SecurityParameters.mac_algorithm
		// SecurityParameters.mac_length
		// SecurityParameters.mac_key_length
		mac = Mac.getInstance(macAlgorithm);
		// mac = Mac.getInstance("HmacSHA256");
	}

	/**
	 * 标准流加密 Standard Stream Cipher<br>
	 * 无须初始化向量IV，任意大小块，须额外认证
	 */
	public boolean isStream() {
		return ivLength == 0 && blockLength == 0 && tagLength == 0;
	}

	/**
	 * 分组加密 CBC(Cipher Block Chaining)<br>
	 * 需求初始化向量IV，固定大小块，须额外认证
	 */
	public boolean isBlock() {
		return ivLength > 0 && blockLength > 0 && tagLength == 0;
	}

	/**
	 * 关联数据认证加密 AEAD(Authenticated Encryption with Associated Data)<br>
	 * 需求初始化向量IV，任意大小块，加密后带认证标签Tag
	 */
	public boolean isAEAD() {
		return ivLength > 0 && blockLength == 0 && tagLength > 0;
	}

	/**
	 * TLS 1.2 AEAD 附加数据
	 * 
	 * <pre>
	 * additional_data = seq_num +
	 *                   TLSCompressed.type +
	 *                   TLSCompressed.version +
	 *                   TLSCompressed.length
	 * -
	 * </pre>
	 */
	private byte[] additionalData(long sequence, byte type, short version, int length) {
		final byte[] data = new byte[8 + 1 + 2 + 2];
		Binary.put(data, 0, sequence);
		data[8] = type;
		Binary.put(data, 9, version);
		Binary.put(data, 11, (short) length);
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
	public void encryptReset(byte[] key, byte[] iv) throws Exception {
		encryptKey = new SecretKeySpec(key, secretKeyAlgorithm);
		encryptIV = iv;
		encryptSequence = 0;
	}

	/**
	 * 重置解密密钥
	 */
	public void decryptReset(byte[] key, byte[] iv) throws Exception {
		decryptKey = new SecretKeySpec(key, secretKeyAlgorithm);
		decryptIV = iv;
		decryptSequence = 0;
	}

	/**
	 * TLS 1.2 开始加密，AEAD 附加数据长度应包括Tag部分
	 */
	public void encryptAEAD(byte type, short version, int length) throws Exception {
		final AlgorithmParameterSpec apspec = new GCMParameterSpec(tagLength * 8, encryptIV);
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, apspec, TLS.RANDOM);
		encryptCipher.updateAAD(additionalData(encryptSequence, type, version, length));
	}

	/**
	 * TLS 1.2 开始解密，AEAD 附加数据长度应包括Tag部分
	 */
	public void decryptAEAD(byte type, short version, int length) throws Exception {
		final AlgorithmParameterSpec apspec = new GCMParameterSpec(tagLength * 8, decryptIV);
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, apspec, TLS.RANDOM);
		decryptCipher.updateAAD(additionalData(decryptSequence, type, version, length));
	}

	/**
	 * TLS 1.2 开始加密
	 */
	public void encryptBlock(byte[] iv) throws Exception {
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, new IvParameterSpec(iv), TLS.RANDOM);
	}

	/**
	 * TLS 1.2 开始解密
	 */
	public void decryptBlock(byte[] iv) throws Exception {
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, new IvParameterSpec(iv), TLS.RANDOM);
	}

	/**
	 * TLS 1.2 开始加密
	 */
	public void encryptStream() throws Exception {
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, TLS.RANDOM);
	}

	/**
	 * TLS 1.2 开始解密
	 */
	public void decryptStream() throws Exception {
		decryptCipher.init(Cipher.ENCRYPT_MODE, decryptKey, TLS.RANDOM);
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

	/**
	 * 解密
	 */
	public byte[] decrypt(byte[] data) {
		return decryptCipher.update(data);
	}

	/**
	 * 解密完成
	 */
	public byte[] decryptFinal() throws Exception {
		decryptSequence++;
		return decryptCipher.doFinal();
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

	byte[] encryptMACKey;

	public void encryptMACKey(byte[] key) {
		encryptMACKey = key;
	}

	public byte[] encryptMAC(byte type, short version, DataBuffer data, int length) throws Exception {
		// System.out.println("encryptSequence" + encryptSequence);
		// 经过实际验证序列号从加密消息开始自增
		return MAC(encryptMACKey, encryptSequence, type, version, length, data);
	}

	byte[] decryptMACKey;

	public void decryptMACKey(byte[] key) {
		decryptMACKey = key;
	}

	public byte[] decryptMAC(byte type, short version, DataBuffer data, int length) throws Exception {
		// System.out.println("decryptSequence" + decryptSequence);
		// 经过实际验证序列号从加密消息开始自增
		// 由于解密后才能执行验证，因此解密序列号须减一
		return MAC(decryptMACKey, decryptSequence - 1, type, version, length, data);
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
	protected final byte[] MAC(byte[] key, long seqnum, byte type, short version, int length, DataBuffer fragment) throws InvalidKeyException {
		mac.init(new SecretKeySpec(key, mac.getAlgorithm()));

		final byte[] temp = new byte[8];
		Binary.put(temp, 0, seqnum);
		mac.update(temp);

		mac.update(type);

		Binary.put(temp, 0, version);
		mac.update(temp, 0, 2);

		Binary.put(temp, 0, (short) length);
		mac.update(temp, 0, 2);

		// fragment
		DataBufferUnit unit = fragment.head();
		while (unit != null && length > 0) {
			unit.buffer().mark();
			if (unit.readable() <= length) {
				length -= unit.readable();
				mac.update(unit.buffer());
			} else {
				length = unit.readable() - length;
				unit.writeIndex(unit.writeIndex() - length);
				mac.update(unit.buffer());
				unit.writeIndex(unit.writeIndex() + length);
				length = 0;
			}
			unit.buffer().reset();
			unit = unit.next();
		}

		return mac.doFinal();
	}

	/**
	 * 加密数据块最小长度(Byte)
	 */
	public int blockLength() {
		return blockLength;
	}

	/**
	 * 消息签名长度(Byte)
	 */
	public int macLength() {
		return mac.getMacLength();
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