package com.joyzl.network.tls;

import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;

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
	 * TLS 1.2 开始加密，AEAD 附加数据长度应包括Tag部分
	 */
	public void encryptAEAD(byte type, short version, int length) throws Exception {
		final AlgorithmParameterSpec apspec = new GCMParameterSpec(this.type.tag() * 8, encryptIV);
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, apspec, TLS.RANDOM);
		encryptCipher.updateAAD(additionalData(encryptSequence, type, version, length));
	}

	/**
	 * TLS 1.2 开始解密，AEAD 附加数据长度应包括Tag部分
	 */
	public void decryptAEAD(byte type, short version, int length) throws Exception {
		final AlgorithmParameterSpec apspec = new GCMParameterSpec(this.type.tag() * 8, decryptIV);
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, apspec, TLS.RANDOM);
		decryptCipher.updateAAD(additionalData(decryptSequence, type, version, length));
	}
}