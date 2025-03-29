package com.joyzl.network.tls;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * 密码套件执行消息加密与解密
 * 
 * @author ZhangXi 2024年12月24日
 */
class V1CipherSuiter extends V0CipherSuiter {

	// 1.1 采用随机生成的IV

	/**
	 * 开始加密
	 */
	public void encryptBlock(byte[] iv) throws Exception {
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKey, new IvParameterSpec(iv), TLS.RANDOM);
	}

	/**
	 * 开始解密
	 */
	public void decryptBlock(byte[] iv) throws Exception {
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKey, new IvParameterSpec(iv), TLS.RANDOM);
	}
}