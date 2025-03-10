package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 签名算法，执行签名与验证
 * 
 * @author ZhangXi 2025年2月24日
 */
class Signaturer implements SignatureScheme {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private short scheme;
	private Signature signature;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	public void scheme(short value) throws Exception {
		scheme = value;
		switch (value) {
			// RSASSA-PKCS1-v1_5 algorithms
			case RSA_PKCS1_SHA256:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA256withRSA");
				break;
			case RSA_PKCS1_SHA384:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA384withRSA");
				break;
			case RSA_PKCS1_SHA512:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA512withRSA");
				break;

			// ECDSA algorithms
			case ECDSA_SECP256R1_SHA256:
				// factory = KeyFactory.getInstance("EC");
				// ECGenParameterSpec ecGenParameterSpec = new
				// ECGenParameterSpec("secp256r1");
				signature = Signature.getInstance("SHA256withECDSA");
				break;
			case ECDSA_SECP384R1_SHA384:
				// factory = KeyFactory.getInstance("EC");
				// ECGenParameterSpec ecGenParameterSpec = new
				// ECGenParameterSpec("secp384r1");
				signature = Signature.getInstance("SHA384withECDSA");
				break;
			case ECDSA_SECP521R1_SHA512:
				// factory = KeyFactory.getInstance("EC");
				// ECGenParameterSpec ecGenParameterSpec = new
				// ECGenParameterSpec("secp521r1");
				signature = Signature.getInstance("SHA512withECDSA");
				break;

			// RSASSA-PSS RSAE algorithms
			case RSA_PSS_RSAE_SHA256:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA256withRSA/PSS");
				break;
			case RSA_PSS_RSAE_SHA384:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA384withRSA/PSS");
				break;
			case RSA_PSS_RSAE_SHA512:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA512withRSA/PSS");
				break;

			// EdDSA algorithms
			case ED25519:
				// factory = KeyFactory.getInstance("Ed25519");
				signature = Signature.getInstance("Ed25519");
				break;
			case ED448:
				// factory = KeyFactory.getInstance("Ed448");
				signature = Signature.getInstance("Ed448");
				break;

			// RSASSA-PSS PSS algorithms
			case RSA_PSS_PSS_SHA256:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA256withRSA/PSS");
				break;
			case RSA_PSS_PSS_SHA384:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA384withRSA/PSS");
				break;
			case RSA_PSS_PSS_SHA512:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA512withRSA/PSS");
				break;

			// 不推荐的
			case RSA_PKCS1_SHA1:
				// factory = KeyFactory.getInstance("RSA");
				signature = Signature.getInstance("SHA1withRSA");
				break;
			case ECDSA_SHA1:
				// factory = KeyFactory.getInstance("EC");
				signature = Signature.getInstance("SHA1withECDSA");
				break;
			case DSA_SHA1_RESERVED:
				// factory = KeyFactory.getInstance("DSA");
				signature = Signature.getInstance("SHA1withDSA");
				break;
			case DSA_SHA256_RESERVED:
				// factory = KeyFactory.getInstance("DSA");
				signature = Signature.getInstance("SHA256withDSA");
				break;
			case DSA_SHA384_RESERVED:
				// factory = KeyFactory.getInstance("DSA");
				signature = Signature.getInstance("SHA384withDSA");
				break;
			case DSA_SHA512_RESERVED:
				// factory = KeyFactory.getInstance("DSA");
				signature = Signature.getInstance("SHA512withDSA");
				break;
			default:
				throw new IllegalArgumentException("TLS:UNKNOWN signature algorithm");
		}
	}

	public static short scheme(String algorithm) {
		switch (algorithm) {
			// RSASSA-PKCS1-v1_5 algorithms
			case "SHA256withRSA":
				return RSA_PKCS1_SHA256;
			case "SHA384withRSA":
				return RSA_PKCS1_SHA384;
			case "SHA512withRSA":
				return RSA_PKCS1_SHA512;

			// ECDSA algorithms
			case "SHA256withECDSA":
				return ECDSA_SECP256R1_SHA256;
			case "SHA384withECDSA":
				return ECDSA_SECP384R1_SHA384;
			case "SHA512withECDSA":
				return ECDSA_SECP521R1_SHA512;

			// RSASSA-PSS RSAE algorithms
			case "SHA256withRSA/PSS":
				return RSA_PSS_RSAE_SHA256;
			case "SHA384withRSA/PSS":
				return RSA_PSS_RSAE_SHA384;
			case "SHA512withRSA/PSS":
				return RSA_PSS_RSAE_SHA512;

			// EdDSA algorithms
			case "Ed25519":
				return ED25519;
			case "Ed448":
				return ED448;

			// RSASSA-PSS PSS algorithms
			// case "SHA256withRSA/PSS":
			// return RSA_PSS_PSS_SHA256;
			// case "SHA384withRSA/PSS":
			// return RSA_PSS_PSS_SHA384;
			// case "SHA512withRSA/PSS":
			// return RSA_PSS_PSS_SHA512;

			// 不推荐的
			case "SHA1withRSA":
				return RSA_PKCS1_SHA1;
			case "SHA1withECDSA":
				return ECDSA_SHA1;
			case "SHA1withDSA":
				return DSA_SHA1_RESERVED;
			case "SHA256withDSA":
				return DSA_SHA256_RESERVED;
			case "SHA384withDSA":
				return DSA_SHA384_RESERVED;
			case "SHA512withDSA":
				return DSA_SHA512_RESERVED;
			default:
				throw new IllegalArgumentException("TLS:UNKNOWN signature algorithm");
		}
	}

	/**
	 * 获取用于签名的私钥
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * 设置用于签名的私钥
	 */
	public void setPrivateKey(PrivateKey value) {
		privateKey = value;
	}

	/**
	 * 获取用于验证的公钥
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * 设置用于验证的公钥
	 */
	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	/** 上下文字符串 */
	final static byte[] CONTEXT_SERVER = "TLS 1.3, server CertificateVerify".getBytes(StandardCharsets.US_ASCII);
	/** 上下文字符串 */
	final static byte[] CONTEXT_CLIENT = "TLS 1.3, client CertificateVerify".getBytes(StandardCharsets.US_ASCII);
	/** 重复64次 */
	final static byte PREFIX = 0x20;
	/** 分隔 */
	final static byte SEPARATE = 0x00;

	/**
	 * 生成服务端签名
	 * 
	 * @param hash 消息哈希
	 */
	public byte[] singServer(byte[] hash) throws Exception {
		return singServer(privateKey, hash);
	}

	/**
	 * 生成服务端签名
	 * 
	 * @param key 服务端证书私钥
	 * @param hash 消息哈希
	 */
	public byte[] singServer(PrivateKey key, byte[] hash) throws Exception {
		signature.initSign(key);
		for (int i = 0; i < 64; i++) {
			signature.update(PREFIX);
		}
		signature.update(CONTEXT_SERVER);
		signature.update(SEPARATE);
		signature.update(hash);
		return signature.sign();
	}

	/**
	 * 验证服务端签名
	 * 
	 * @param key 服务端证书公钥
	 * @param hash 消息哈希
	 * @param sign 服务端签名
	 */
	public boolean verifyServer(PublicKey key, byte[] hash, byte[] sign) throws Exception {
		signature.initVerify(key);
		for (int i = 0; i < 64; i++) {
			signature.update(PREFIX);
		}
		signature.update(CONTEXT_SERVER);
		signature.update(SEPARATE);
		signature.update(hash);
		return signature.verify(sign);
	}

	/**
	 * 生成客户端签名
	 * 
	 * @param hash 消息哈希
	 */
	public byte[] singClient(byte[] hash) throws Exception {
		return singClient(privateKey, hash);
	}

	/**
	 * 生成客户端签名
	 * 
	 * @param key 客户端证书私钥
	 * @param hash 消息哈希
	 */
	public byte[] singClient(PrivateKey key, byte[] hash) throws Exception {
		signature.initSign(key);
		for (int i = 0; i < 64; i++) {
			signature.update(PREFIX);
		}
		signature.update(CONTEXT_CLIENT);
		signature.update(SEPARATE);
		signature.update(hash);
		return signature.sign();
	}

	/**
	 * 验证客户端签名
	 * 
	 * @param key 客户端证书公钥
	 * @param hash 消息哈希
	 * @param sign 客户端签名
	 */
	public boolean verifyClient(PublicKey key, byte[] hash, byte[] sign) throws Exception {
		signature.initVerify(key);
		for (int i = 0; i < 64; i++) {
			signature.update(PREFIX);
		}
		signature.update(CONTEXT_CLIENT);
		signature.update(SEPARATE);
		signature.update(hash);
		return signature.verify(sign);
	}

	public short scheme() {
		return scheme;
	}

	////////////////////////////////////////////////////////////////////////////////

	private byte[] hash;

	public void setHash(byte[] value) {
		hash = value;
	}

	public boolean verifyServer(byte[] sign) throws Exception {
		if (verifyServer(publicKey, hash, sign)) {
			hash = null;
			return true;
		}
		hash = null;
		return false;
	}

	public boolean verifyClient(byte[] sign) throws Exception {
		if (verifyClient(publicKey, hash, sign)) {
			hash = null;
			return true;
		}
		hash = null;
		return false;
	}
}