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
class V3CipherSuiter extends CipherSuiter {

	/** AES-GCM 加密记录限制 */
	final static long MAX_SEQUENCE = (long) Math.pow(2, 24.5);

	final static CipherSuiteType[] V13 = new CipherSuiteType[] { //
			CipherSuiteType.TLS_AES_128_GCM_SHA256, //
			CipherSuiteType.TLS_AES_256_GCM_SHA384, //
			CipherSuiteType.TLS_CHACHA20_POLY1305_SHA256, //
			CipherSuiteType.TLS_AES_128_CCM_SHA256, //
			CipherSuiteType.TLS_AES_128_CCM_8_SHA256, //
			CipherSuiteType.TLS_SM4_GCM_SM3, //
			CipherSuiteType.TLS_SM4_CCM_SM3,//
	};

	////////////////////////////////////////////////////////////////////////////////

	public void initialize(short code) throws Exception {
		initialize(CipherSuiter.find(code, V13));
	}

	/**
	 * AEAD 随机数
	 */
	private byte[] nonce(byte[] IV, long sequence) {
		final byte[] nonce = new byte[type.iv()];
		// 1.填充的序列号，左补零
		Binary.put(nonce, nonce.length - 8, sequence);
		// nonce[ivLength - 8] = (byte) (sequence >>> 56);
		// nonce[ivLength - 7] = (byte) (sequence >>> 48);
		// nonce[ivLength - 6] = (byte) (sequence >>> 40);
		// nonce[ivLength - 5] = (byte) (sequence >>> 32);
		// nonce[ivLength - 4] = (byte) (sequence >>> 24);
		// nonce[ivLength - 3] = (byte) (sequence >>> 16);
		// nonce[ivLength - 2] = (byte) (sequence >>> 8);
		// nonce[ivLength - 1] = (byte) sequence;
		// 2.填充的序列号与writeIV异或
		// writeIV.length始终与iv_length相同
		for (int i = 0; i < nonce.length; i++) {
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
	private byte[] additionalData(int length) {
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
	 * TLS 1.3 开始加密，AEAD 附加数据长度应包括Tag部分
	 */
	public void encryptAEAD(int length) throws Exception {
		final AlgorithmParameterSpec spec = new GCMParameterSpec(type.tag() * 8, nonce(encryptIV, encryptSequence));
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, spec);
		encryptCipher.updateAAD(additionalData(length));
	}

	/**
	 * TLS 1.3 开始解密，AEAD 附加数据长度应包括Tag部分
	 */
	public void decryptAEAD(int length) throws Exception {
		final AlgorithmParameterSpec spec = new GCMParameterSpec(type.tag() * 8, nonce(decryptIV, decryptSequence));
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, spec);
		decryptCipher.updateAAD(additionalData(length));
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
}