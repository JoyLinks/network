package com.joyzl.network.tls;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.Mac;

/**
 * 初始化密码套件
 * 
 * @author ZhangXi 2024年12月24日
 */
public class CipherSuiter implements CipherSuite {

	private int key_length, iv_length;
	/** HASH */
	protected MessageDigest digest;
	/** 消息加密/解密 */
	protected Cipher cipher;
	/** HMAC */
	protected Mac hmac;

	public CipherSuiter(short code) throws Exception {
		// https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html
		switch (code) {
			// v1.3
			case TLS_AES_128_GCM_SHA256:
				cipher = Cipher.getInstance("AES/GCM/NoPadding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				key_length = 16;
				iv_length = 12;
				break;
			case TLS_AES_256_GCM_SHA384:
				cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
				digest = MessageDigest.getInstance("SHA-384");
				hmac = Mac.getInstance("HmacSHA256");
				key_length = 32;
				iv_length = 12;
				break;
			case TLS_CHACHA20_POLY1305_SHA256:
				cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				break;
			case TLS_AES_128_CCM_SHA256:
				cipher = Cipher.getInstance("AES/GCM/PKCS5Padding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				key_length = 16;
				iv_length = 12;
				break;
			case TLS_AES_128_CCM_8_SHA256:
				cipher = Cipher.getInstance("AES/CCM/PKCS5Padding");
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				key_length = 16;
				iv_length = 12;
				break;
			// v1.2 v1.1 v1.0
			case TLS_RSA_WITH_NULL_MD5:
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_NULL_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_EXPORT_WITH_RC4_40_MD5:
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_RC4_128_MD5:
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_RC4_128_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5:
				digest = MessageDigest.getInstance("MD5");
				hmac = Mac.getInstance("HmacMD5");
				break;
			case TLS_RSA_WITH_IDEA_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_EXPORT_WITH_DES40_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_WITH_DES_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_RSA_WITH_3DES_EDE_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			//
			case TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_DSS_WITH_DES_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_RSA_WITH_DES_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			//
			case TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_DSS_WITH_DES_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_RSA_WITH_DES_CBC_SHA:
				digest = MessageDigest.getInstance("SHA-1");
				hmac = Mac.getInstance("HmacSHA1");
				break;
			case TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA:
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
	}

	public int keyLength() {
		return key_length;
	}

	public int ivLength() {
		return iv_length;
	}
}