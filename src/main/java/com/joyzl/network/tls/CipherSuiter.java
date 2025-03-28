package com.joyzl.network.tls;

import java.nio.ByteBuffer;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;

/**
 * 密码套件执行消息加密与解密
 * 
 * @author ZhangXi 2024年12月24日
 */
abstract class CipherSuiter implements CipherSuite {

	// https://www.bouncycastle.org/
	// https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html

	// 尝试并建立本地可用的密码套件
	final static short[] AVAILABLES;
	static {
		final short[] items = new short[CipherSuiteType.values().length];
		int size = 0;
		for (CipherSuiteType type : CipherSuiteType.values()) {
			try {
				Cipher.getInstance(type.cipherAlgorithm());
				MessageDigest.getInstance(type.digestAlgorithm());
				Mac.getInstance(type.macAlgorithm());
			} catch (Exception e) {
				// 忽略此异常
				continue;
			}
			items[size++] = type.code();
		}
		AVAILABLES = Arrays.copyOf(items, size);
	}

	/** 在指定范围查找密码套件 */
	static CipherSuiteType find(short code, CipherSuiteType[] types) {
		for (int t = 0; t < types.length; t++) {
			if (types[t].code() == code) {
				return types[t];
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////

	protected CipherSuiteType type = CipherSuiteType.TLS_NULL_WITH_NULL_NULL;

	protected long encryptSequence = 0;
	protected long decryptSequence = 0;
	protected Cipher encryptCipher;
	protected Cipher decryptCipher;
	protected byte[] encryptIV;
	protected byte[] decryptIV;
	protected Key encryptKey;
	protected Key decryptKey;

	public void suite(short code) throws Exception {
		suite(CipherSuiteType.valueOf(code));
	}

	public void suite(CipherSuiteType type) throws Exception {
		if (type != this.type) {
			this.type = type;

			if (type.cipherAlgorithm() != null) {
				encryptCipher = Cipher.getInstance(type.cipherAlgorithm());
				decryptCipher = Cipher.getInstance(type.cipherAlgorithm());
			} else {
				encryptCipher = null;
				decryptCipher = null;
			}
		}
		encryptKey = null;
		decryptKey = null;
	}

	public CipherSuiteType type() {
		return type;
	}

	/**
	 * 标准流加密 Standard Stream Cipher<br>
	 * 无须初始化向量IV，任意大小块，须额外认证
	 */
	public boolean isStream() {
		return type.iv() == 0 && type.block() == 0 && type.tag() == 0;
	}

	/**
	 * 分组加密 CBC(Cipher Block Chaining)<br>
	 * 需求初始化向量IV，固定大小块，须额外认证
	 */
	public boolean isBlock() {
		return type.iv() > 0 && type.block() > 0 && type.tag() == 0;
	}

	/**
	 * 关联数据认证加密 AEAD(Authenticated Encryption with Associated Data)<br>
	 * 需求初始化向量IV，任意大小块，加密后带认证标签Tag
	 */
	public boolean isAEAD() {
		return type.iv() > 0 && type.block() == 0 && type.tag() > 0;
	}

	/**
	 * 重置加解密钥
	 */
	public void resetKeys() {
		encryptKey = null;
		decryptKey = null;
		encryptIV = null;
		decryptIV = null;
	}

	/**
	 * 重置加密密钥
	 */
	public void encryptReset(byte[] key, byte[] iv) throws Exception {
		encryptKey = new SecretKeySpec(key, type.keyAlgorithm());
		encryptIV = iv;
		encryptSequence = 0;
	}

	/**
	 * 重置解密密钥
	 */
	public void decryptReset(byte[] key, byte[] iv) throws Exception {
		decryptKey = new SecretKeySpec(key, type.keyAlgorithm());
		decryptIV = iv;
		decryptSequence = 0;
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
	 * 加密密钥
	 */
	public byte[] encryptKey() {
		return encryptKey.getEncoded();
	}

	/**
	 * 解密密钥
	 */
	public byte[] decryptKey() {
		return decryptKey.getEncoded();
	}

	/**
	 * 加密向量
	 */
	public byte[] encryptIV() {
		return encryptIV;
	}

	/**
	 * 解密向量
	 */
	public byte[] decryptIV() {
		return decryptIV;
	}

	/**
	 * 消息认证长度(Byte)
	 */
	public int macLength() {
		return type.hash();
	}

	/**
	 * 加密数据块长度(Byte)
	 */
	public int blockLength() {
		return type.block();
	}

	/**
	 * 消息标记长度(Byte)
	 */
	public int tagLength() {
		return type.tag();
	}

	/**
	 * 消息密钥长度(Byte)
	 */
	public int keyLength() {
		return type.key();
	}

	/**
	 * 消息随机数长度(Byte)
	 */
	public int ivLength() {
		return type.iv();
	}
}