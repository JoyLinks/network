package com.joyzl.network.tls;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

/**
 * 密钥交换处理类
 * 
 * @author ZhangXi 2025年2月3日
 */
class KeyExchange implements NamedGroup {

	final static short[] AVAILABLES;
	static {
		final KeyExchange ke = new KeyExchange();
		short[] items = new short[0];
		for (short group : ALL) {
			try {
				ke.initialize(group);
				items = Arrays.copyOf(items, items.length + 1);
				items[items.length - 1] = group;
			} catch (Exception e) {
				// 忽略此异常
				e.printStackTrace();
			}
		}
		AVAILABLES = items;
	}

	////////////////////////////////////////////////////////////////////////////////

	private short group;

	private KeyPairGenerator generator;
	private KeyFactory factory;

	private PrivateKey privateKey;
	private PublicKey publicKey;

	public KeyExchange() {
	}

	public KeyExchange(short group) throws Exception {
		initialize(group);
	}

	/**
	 * 初始化
	 */
	public void initialize(short group) throws Exception {
		switch (this.group = group) {
			case X25519:
				factory = KeyFactory.getInstance("XDH");
				generator = KeyPairGenerator.getInstance("XDH");
				generator.initialize(NamedParameterSpec.X25519);
				break;
			case X448:
				factory = KeyFactory.getInstance("XDH");
				generator = KeyPairGenerator.getInstance("XDH");
				generator.initialize(NamedParameterSpec.X448);
				break;

			case SECP256R1:
				factory = KeyFactory.getInstance("EC");
				generator = KeyPairGenerator.getInstance("EC");
				generator.initialize(new ECGenParameterSpec("secp256r1"));
				break;
			case SECP384R1:
				factory = KeyFactory.getInstance("EC");
				generator = KeyPairGenerator.getInstance("EC");
				generator.initialize(new ECGenParameterSpec("secp384r1"));
				break;
			case SECP521R1:
				factory = KeyFactory.getInstance("EC");
				generator = KeyPairGenerator.getInstance("EC");
				generator.initialize(new ECGenParameterSpec("secp521r1"));
				break;

			case FFDHE2048:
				factory = KeyFactory.getInstance("DH");
				generator = KeyPairGenerator.getInstance("DH");
				generator.initialize(2048);
				break;
			case FFDHE3072:
				factory = KeyFactory.getInstance("DH");
				generator = KeyPairGenerator.getInstance("DH");
				generator.initialize(3072);
				break;
			case FFDHE4096:
				factory = KeyFactory.getInstance("DH");
				generator = KeyPairGenerator.getInstance("DH");
				generator.initialize(4096);
				break;
			case FFDHE6144:
				factory = KeyFactory.getInstance("DH");
				generator = KeyPairGenerator.getInstance("DH");
				generator.initialize(6144);
				break;
			case FFDHE8192:
				factory = KeyFactory.getInstance("DH");
				generator = KeyPairGenerator.getInstance("DH");
				generator.initialize(8192);
				break;

			default:
				throw new NoSuchAlgorithmException("KeyExchange:" + group);
		}
		generate();
	}

	/**
	 * 生成共享密钥
	 * 
	 * @param key 对方公钥
	 */
	public byte[] sharedKey(byte[] key) throws Exception {
		final KeyAgreement agreement = KeyAgreement.getInstance("XDH");
		agreement.init(getPrivateKey());
		agreement.doPhase(publicKey(key), true);
		return agreement.generateSecret();
	}

	/**
	 * 转换字节为公钥对象
	 */
	public PublicKey publicKey(byte[] key) throws Exception {
		// X509是对公钥的格式化
		/*-
		 * X509 (ASN.1)
		 * 
		 * SubjectPublicKeyInfo ::= SEQUENCE {
		 *     algorithm AlgorithmIdentifier,
		 *     subjectPublicKey BIT STRING
		 * }
		 */
		// 此采用了简化方案恢复对方公钥的格式化字节
		// 假设相同密钥格式的ASN.1头应完全相同
		final byte[] encoded = getPublicKey().getEncoded();
		System.arraycopy(key, 0, encoded, encoded.length - key.length, key.length);
		final X509EncodedKeySpec spec = new X509EncodedKeySpec(encoded);
		return factory.generatePublic(spec);
	}

	/**
	 * 转换公钥对象为字节
	 */
	public byte[] publicKey() throws Exception {
		// 此采用了简化方案恢复对方公钥的格式化字节
		// ASN.1头为12字节
		final byte[] encoded = getPublicKey().getEncoded();
		return Arrays.copyOfRange(encoded, 12, encoded.length);
	}

	/**
	 * 转换字节为私钥对象
	 */
	public PrivateKey privateKey(byte[] key) throws Exception {
		// PKCS8是对私钥的格式化
		/*-
		 * PKCS8 (ASN.1)
		 * 
		 * PrivateKeyInfo ::= SEQUENCE {
		 *     version Version,
		 *     privateKeyAlgorithm PrivateKeyAlgorithmIdentifier,
		 *     privateKey PrivateKey,
		 *     attributes [0] IMPLICIT Attributes OPTIONAL
		 * }
		 * Version ::= INTEGER
		 * PrivateKeyAlgorithmIdentifier ::= AlgorithmIdentifier
		 * PrivateKey ::= OCTET STRING
		 * Attributes ::= SET OF Attribute
		 */
		// 此采用了简化方案恢复对方公钥的格式化字节
		// 假设相同密钥格式的ASN.1头应完全相同，且无可选属性
		final byte[] encoded = getPrivateKey().getEncoded();
		System.arraycopy(key, 0, encoded, encoded.length - key.length, key.length);
		final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded);
		return factory.generatePrivate(spec);
	}

	/**
	 * 转换私钥对象为字节
	 */
	public byte[] privateKey() throws Exception {
		// 此采用了简化方案恢复对方公钥的格式化字节
		// ASN.1头为16字节
		final byte[] encoded = getPrivateKey().getEncoded();
		return Arrays.copyOfRange(encoded, 16, encoded.length);
	}

	/**
	 * 生成密钥对（公钥和私钥）
	 */
	public KeyPair generate() {
		final KeyPair pair = generator.generateKeyPair();
		privateKey = pair.getPrivate();
		publicKey = pair.getPublic();
		return pair;
	}

	/**
	 * 获取公钥
	 */
	public PublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * 设置公钥
	 */
	public void setPublicKey(byte[] key) throws Exception {
		publicKey = publicKey(key);
	}

	/**
	 * 获取私钥
	 */
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	/**
	 * 设置私钥
	 */
	public void setPrivateKey(byte[] key) throws Exception {
		privateKey = privateKey(key);
	}

	public short group() {
		return group;
	}
}