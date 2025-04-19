package com.joyzl.network.tls;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

import com.joyzl.network.codec.Binary;

/**
 * 密码套件执行消息加密与解密
 * 
 * @author ZhangXi 2024年12月24日
 */
class V2CipherSuiter extends V1CipherSuiter {

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
	 * TLS 1.2 AEAD 随机数
	 * 
	 * <pre>
	 * RFC 5116
	 * +-------------------+--------------------+---------------+
	 * |    Fixed-Common   |   Fixed-Distinct   |    Counter    |
	 * +-------------------+--------------------+---------------+
	 *  <---- implicit ---> <------------ explicit ------------>
	 * 
	 * GenericAEADCipher.nonce_explicit
	 * implicit: client_write_iv / server_write_iv
	 * 
	 * RFC5288
	 * struct {
	 *       opaque salt[4];           // implicit
	 *       opaque nonce_explicit[8]; // explicit
	 * } GCMNonce;
	 * 
	 * RFC6655
	 * struct {
	 *       opaque salt[4];
	 *       opaque nonce_explicit[8];
	 * } CCMNonce;
	 * </pre>
	 */
	private byte[] nonce(byte[] iv, long sequence) {
		// RFC5288
		// fixed_iv_length = 4;
		// record_iv_length = 8;

		// sequence = sequence & 0x01FFFFFFFFFFFFFFL;
		// implicit(iv) || explicit(sequence)
		Binary.put(iv, iv.length - 8, sequence);
		return iv;
	}

	/**
	 * TLS 1.2 开始加密，AEAD 附加数据长度应包括Tag部分
	 */
	public void encryptAEAD(byte type, short version, int length) throws Exception {
		final AlgorithmParameterSpec apspec = new GCMParameterSpec(this.type.tag() * 8, nonce(encryptIV, encryptSequence));
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, apspec, TLS.RANDOM);
		encryptCipher.updateAAD(additionalData(encryptSequence, type, version, length));
	}

	/**
	 * TLS 1.2 开始解密，AEAD 附加数据长度应包括Tag部分
	 */
	public void decryptAEAD(byte type, short version, int length) throws Exception {
		final AlgorithmParameterSpec apspec = new GCMParameterSpec(this.type.tag() * 8, nonce(decryptIV, decryptSequence));
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, apspec, TLS.RANDOM);
		decryptCipher.updateAAD(additionalData(decryptSequence, type, version, length));
	}
}