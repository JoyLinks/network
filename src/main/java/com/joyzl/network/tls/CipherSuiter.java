package com.joyzl.network.tls;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

/**
 * 初始化密码套件
 * 
 * @author ZhangXi 2024年12月24日
 */
public class CipherSuiter implements CipherSuite {

	protected MessageDigest digest;
	protected Mac hmac;

	public CipherSuiter(short code) throws NoSuchAlgorithmException {
		switch (code) {
			// v1.3
			case TLS_AES_128_GCM_SHA256:
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				break;
			case TLS_AES_256_GCM_SHA384:
				digest = MessageDigest.getInstance("SHA-384");
				hmac = Mac.getInstance("HmacSHA256");
				break;
			case TLS_CHACHA20_POLY1305_SHA256:
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				break;
			case TLS_AES_128_CCM_SHA256:
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
				break;
			case TLS_AES_128_CCM_8_SHA256:
				digest = MessageDigest.getInstance("SHA-256");
				hmac = Mac.getInstance("HmacSHA256");
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

}