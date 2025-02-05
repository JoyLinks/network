package com.joyzl.network.tls;

import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 初始化密码套件
 * 
 * @author ZhangXi 2024年12月24日
 */
public class CipherSuiter extends KeyExchange implements CipherSuite {

	// https://www.bouncycastle.org/

	/** CipherSuite */
	private short code;

	/** HASH */
	protected MessageDigest digest;
	/** HMAC */
	protected Mac hmac;

	/** 消息加密/解密 */
	private String algorithm;
	private Cipher cipherEncrypt;
	private Cipher cipherDecrypt;
	private long sequenceEncrypt = 0;
	private long sequenceDecrypt = 0;
	private byte[] ivEncrypt;
	private byte[] ivDecrypt;
	private Key keyEncrypt;
	private Key keyDecrypt;
	private int tagLength;
	private int keyLength;
	private int ivLength;

	public CipherSuiter(short code) throws Exception {
		// https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html
		switch (code) {
			// v1.3
			case TLS_AES_128_GCM_SHA256:
				algorithm = "AES";
				cipherEncrypt = Cipher.getInstance("AES/GCM/NoPadding");
				cipherDecrypt = Cipher.getInstance("AES/GCM/NoPadding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				tagLength = 16;
				keyLength = 16;
				ivLength = 12;
				break;
			case TLS_AES_256_GCM_SHA384:
				algorithm = "AES";
				cipherEncrypt = Cipher.getInstance("AES/GCM/NoPadding");
				cipherDecrypt = Cipher.getInstance("AES/GCM/NoPadding");
				digest = MessageDigest.getInstance("SHA-384");
				hmac = Mac.getInstance("HmacSHA384");
				tagLength = 32;
				keyLength = 32;
				ivLength = 12;
				break;
			case TLS_CHACHA20_POLY1305_SHA256:
				algorithm = "AES";
				cipherEncrypt = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");
				cipherDecrypt = Cipher.getInstance("ChaCha20/Poly1305/NoPadding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				tagLength = 32;
				keyLength = 32;
				ivLength = 12;
				break;
			case TLS_AES_128_CCM_SHA256:
				algorithm = "AES";
				cipherEncrypt = Cipher.getInstance("AES/CCM/NoPadding");
				cipherDecrypt = Cipher.getInstance("AES/CCM/NoPadding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				tagLength = 16;
				keyLength = 16;
				ivLength = 12;
				break;
			case TLS_AES_128_CCM_8_SHA256:
				algorithm = "AES";
				cipherEncrypt = Cipher.getInstance("AES/CCM/NoPadding");
				cipherDecrypt = Cipher.getInstance("AES/CCM/NoPadding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				tagLength = 16;
				keyLength = 16;
				ivLength = 12;
				break;
			// v1.2 v1.1 v1.0
			case TLS_RSA_WITH_NULL_MD5:
				algorithm = "RSA";
				cipherEncrypt = Cipher.getInstance("RSA/NONE/NoPadding");
				cipherDecrypt = Cipher.getInstance("RSA/NONE/NoPadding");
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_NULL_SHA:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_EXPORT_WITH_RC4_40_MD5:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_RC4_128_MD5:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_RC4_128_SHA:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_IDEA_CBC_SHA:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_EXPORT_WITH_DES40_CBC_SHA:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_WITH_DES_CBC_SHA:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_WITH_3DES_EDE_CBC_SHA:
				algorithm = "RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			//
			case TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA:
				algorithm = "DH-DSS-EXPORT";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_DSS_WITH_DES_CBC_SHA:
				algorithm = "DH-DSS";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
				algorithm = "DH-DSS";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA:
				algorithm = "DH-RSA-EXPORT";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_RSA_WITH_DES_CBC_SHA:
				algorithm = "DH-RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
				algorithm = "DH-RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			//
			case TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA:
				algorithm = "DHE-DSS-EXPORT";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_DSS_WITH_DES_CBC_SHA:
				algorithm = "DHE-DSS";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
				algorithm = "DHE-DSS";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA:
				algorithm = "DHE-RSA-EXPORT";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_RSA_WITH_DES_CBC_SHA:
				algorithm = "DHE-RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
				algorithm = "DHE-RSA";
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			//
			case TLS_DH_ANON_EXPORT_WITH_RC4_40_MD5:
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_DH_ANON_WITH_RC4_128_MD5:
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_DH_ANON_EXPORT_WITH_DES40_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_ANON_WITH_DES_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			//
			case TLS_NULL_WITH_NULL_NULL:
			default:
				digest = null;
		}
		this.code = code;
	}

	/**
	 * 加密套件参数集
	 */
	private AlgorithmParameterSpec parameters(byte[] nonce) {
		switch (code) {
			// v1.3
			case TLS_AES_128_GCM_SHA256:
			case TLS_AES_256_GCM_SHA384:
				return new GCMParameterSpec(tagLength * 8, nonce);
			case TLS_CHACHA20_POLY1305_SHA256:
			case TLS_AES_128_CCM_SHA256:
			case TLS_AES_128_CCM_8_SHA256:
				return new GCMParameterSpec(tagLength * 8, nonce);
			// v1.2 v1.1 v1.0
			case TLS_RSA_WITH_NULL_MD5:
			case TLS_RSA_WITH_NULL_SHA:
			case TLS_RSA_EXPORT_WITH_RC4_40_MD5:
			case TLS_RSA_WITH_RC4_128_MD5:
			case TLS_RSA_WITH_RC4_128_SHA:
			case TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5:
			case TLS_RSA_WITH_IDEA_CBC_SHA:
			case TLS_RSA_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_RSA_WITH_DES_CBC_SHA:
			case TLS_RSA_WITH_3DES_EDE_CBC_SHA:
				//
			case TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DH_DSS_WITH_DES_CBC_SHA:
			case TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
			case TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DH_RSA_WITH_DES_CBC_SHA:
			case TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
				//
			case TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DHE_DSS_WITH_DES_CBC_SHA:
			case TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
			case TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DHE_RSA_WITH_DES_CBC_SHA:
			case TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
				//
			case TLS_DH_ANON_EXPORT_WITH_RC4_40_MD5:
			case TLS_DH_ANON_WITH_RC4_128_MD5:
			case TLS_DH_ANON_EXPORT_WITH_DES40_CBC_SHA:
			case TLS_DH_ANON_WITH_DES_CBC_SHA:
			case TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA:
				//
			case TLS_NULL_WITH_NULL_NULL:
			default:
				return null;
		}
	}

	/**
	 * AEAD 随机数 TLS 1.3
	 */
	private byte[] nonce(byte[] IV, long sequence) {
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
	 * AEAD 附加数据 TLS 1.3
	 * 
	 * <pre>
	 * additional_data = TLSCiphertext.opaque_type || TLSCiphertext.legacy_record_version || TLSCiphertext.length
	 * </pre>
	 */
	private byte[] additional(int length) {
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
	 * 重置密钥和随机数
	 */
	public void encryptReset(byte[] writeKey, byte[] writeIV) {
		keyEncrypt = new SecretKeySpec(writeKey, algorithm);
		ivEncrypt = writeIV;
		sequenceEncrypt = 0;
	}

	/**
	 * 重置密钥和随机数
	 */
	public void decryptReset(byte[] readKey, byte[] readIV) {
		keyDecrypt = new SecretKeySpec(readKey, algorithm);
		ivDecrypt = readIV;
		sequenceDecrypt = 0;
	}

	/**
	 * AEAD 附加数据 TLS 1.3
	 */
	public void additionalEncrypt(int length) throws Exception {
		cipherEncrypt.init(Cipher.ENCRYPT_MODE, keyEncrypt, parameters(nonce(ivEncrypt, sequenceEncrypt)));
		cipherEncrypt.updateAAD(additional(length));
	}

	/**
	 * AEAD 附加数据 TLS 1.3
	 */
	public void additionalDecrypt(int length) throws Exception {
		cipherDecrypt.init(Cipher.DECRYPT_MODE, keyDecrypt, parameters(nonce(ivDecrypt, sequenceDecrypt)));
		cipherDecrypt.updateAAD(additional(length));
	}

	/**
	 * 加密
	 */
	public byte[] encrypt(byte[] data) {
		return cipherEncrypt.update(data);
	}

	/**
	 * 解密
	 */
	public byte[] decrypt(byte[] data) {
		return cipherDecrypt.update(data);
	}

	/**
	 * 加密完成
	 */
	public byte[] encryptFinal() throws Exception {
		sequenceEncrypt++;
		return cipherEncrypt.doFinal();
	}

	/**
	 * 解密完成
	 */
	public byte[] decryptFinal() throws Exception {
		sequenceDecrypt++;
		return cipherDecrypt.doFinal();
	}

	/**
	 * 加密序列号
	 */
	public long encryptSequence() {
		return sequenceEncrypt;
	}

	/**
	 * 解密序列号
	 */
	public long decryptSequence() {
		return sequenceDecrypt;
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

	/**
	 * 密码套件代码
	 */
	public short code() {
		return code;
	}
}