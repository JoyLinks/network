package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;

/**
 * 密钥交换测试
 * 
 * @author ZhangXi 2025年2月4日
 */
class TestKeyExchange {

	@Test
	void x25519() throws Exception {
		final V3KeyExchange client = new V3KeyExchange();
		final V3KeyExchange server = new V3KeyExchange();

		client.initialize(NamedGroup.X25519);
		server.initialize(NamedGroup.X25519);

		// 生成随机密钥并交换获得共享密钥

		client.generate();
		server.generate();

		byte[] clientPublicKey = client.publicKey();
		byte[] serverPublicKey = server.publicKey();
		// System.out.println(Utility.hex(clientKey));
		// System.out.println(Utility.hex(serverKey));

		byte[] clientSharedKey = client.sharedKey(serverPublicKey);
		byte[] serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);

		// RFC 8448

		client.setPrivateKey(Utility.hex("49af42ba7f7994852d713ef2784bcbcaa7911de26adc5642cb634540e7ea5005"));
		client.setPublicKey(Utility.hex("99381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c"));
		server.setPrivateKey(Utility.hex("b1580eeadf6dd589b8ef4f2d5652578cc810e9980191ec8d058308cea216a21e"));
		server.setPublicKey(Utility.hex("c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f"));

		clientPublicKey = client.publicKey();
		serverPublicKey = server.publicKey();
		clientSharedKey = client.sharedKey(serverPublicKey);
		serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"));
		assertArrayEquals(serverSharedKey, Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"));
	}

	@Test
	void testX448() throws Exception {
		final V3KeyExchange client = new V3KeyExchange();
		final V3KeyExchange server = new V3KeyExchange();

		client.initialize(NamedGroup.X448);
		server.initialize(NamedGroup.X448);

		byte[] clientPublicKey = client.publicKey();
		byte[] serverPublicKey = server.publicKey();

		byte[] clientSharedKey = client.sharedKey(serverPublicKey);
		byte[] serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);

		clientSharedKey = client.sharedKey(serverPublicKey);
		serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);
	}

	@Test
	void testSECP521R1() throws Exception {
		final V3KeyExchange client = new V3KeyExchange();
		final V3KeyExchange server = new V3KeyExchange();

		client.initialize(NamedGroup.SECP521R1);
		server.initialize(NamedGroup.SECP521R1);

		/*-
		 * struct {
		 *       uint8 legacy_form = 4;
		 *       opaque X[coordinate_length];
		 *       opaque Y[coordinate_length];
		 * } UncompressedPointRepresentation;
		 */

		byte[] clientPublicKey = client.publicKey();
		byte[] serverPublicKey = server.publicKey();

		byte[] clientSharedKey = client.sharedKey(serverPublicKey);
		byte[] serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);

		clientSharedKey = client.sharedKey(serverPublicKey);
		serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);
	}

	@Test
	void testSECP384R1() throws Exception {
		final V3KeyExchange client = new V3KeyExchange();
		final V3KeyExchange server = new V3KeyExchange();

		client.initialize(NamedGroup.SECP384R1);
		server.initialize(NamedGroup.SECP384R1);

		/*-
		 * struct {
		 *       uint8 legacy_form = 4;
		 *       opaque X[coordinate_length];
		 *       opaque Y[coordinate_length];
		 * } UncompressedPointRepresentation;
		 */

		byte[] clientPublicKey = client.publicKey();
		byte[] serverPublicKey = server.publicKey();

		byte[] clientSharedKey = client.sharedKey(serverPublicKey);
		byte[] serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);

		clientSharedKey = client.sharedKey(serverPublicKey);
		serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);
	}

	@Test
	void testSECP256R1() throws Exception {
		final V3KeyExchange client = new V3KeyExchange();
		final V3KeyExchange server = new V3KeyExchange();

		client.initialize(NamedGroup.SECP256R1);
		server.initialize(NamedGroup.SECP256R1);

		/*-
		 * struct {
		 *       uint8 legacy_form = 4;
		 *       opaque X[coordinate_length];
		 *       opaque Y[coordinate_length];
		 * } UncompressedPointRepresentation;
		 */

		byte[] clientPublicKey = client.publicKey();
		byte[] serverPublicKey = server.publicKey();

		byte[] clientSharedKey = client.sharedKey(serverPublicKey);
		byte[] serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);

		clientSharedKey = client.sharedKey(serverPublicKey);
		serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);
	}

	//@Test
	void testFFDHE2048() throws Exception {
		final V3KeyExchange client = new V3KeyExchange();
		final V3KeyExchange server = new V3KeyExchange();

		client.initialize(NamedGroup.FFDHE2048);
		server.initialize(NamedGroup.FFDHE2048);

		byte[] clientPublicKey = client.publicKey();
		byte[] serverPublicKey = server.publicKey();

		byte[] clientSharedKey = client.sharedKey(serverPublicKey);
		byte[] serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);

		clientSharedKey = client.sharedKey(serverPublicKey);
		serverSharedKey = server.sharedKey(clientPublicKey);

		assertArrayEquals(clientSharedKey, serverSharedKey);
	}

	@Test
	void keyExchange() throws Exception {
		final String algorithm = "X25519";

		// client 生成公私钥对
		final KeyPairGenerator client = KeyPairGenerator.getInstance(algorithm);
		client.initialize(255);
		final KeyPair clientKeyPair = client.generateKeyPair();

		// server 生成公私钥对
		final KeyPairGenerator server = KeyPairGenerator.getInstance(algorithm);
		server.initialize(255);
		final KeyPair serverKeyPair = server.generateKeyPair();

		// client 计算共享密钥
		final KeyAgreement clientAgreement = KeyAgreement.getInstance(algorithm);
		clientAgreement.init(clientKeyPair.getPrivate());
		clientAgreement.doPhase(serverKeyPair.getPublic(), true);

		// server 计算共享密钥
		final KeyAgreement serverAgreement = KeyAgreement.getInstance(algorithm);
		serverAgreement.init(serverKeyPair.getPrivate());
		serverAgreement.doPhase(clientKeyPair.getPublic(), true);

		// 验证共享密钥相同
		// 每生成之后需要重新初始化
		final byte[] clientSharedKey = clientAgreement.generateSecret();
		final byte[] serverSharedKey = serverAgreement.generateSecret();
		assertArrayEquals(clientSharedKey, serverSharedKey);

		// 验证字节形式的公钥

		PublicKey publicKey;
		PrivateKey privatekey;
		final KeyFactory factory = KeyFactory.getInstance(algorithm);

		publicKey = factory.generatePublic(new X509EncodedKeySpec(serverKeyPair.getPublic().getEncoded()));
		privatekey = factory.generatePrivate(new PKCS8EncodedKeySpec(clientKeyPair.getPrivate().getEncoded()));
		clientAgreement.init(privatekey);
		clientAgreement.doPhase(publicKey, true);
		assertArrayEquals(clientAgreement.generateSecret(), clientSharedKey);

		publicKey = factory.generatePublic(new X509EncodedKeySpec(clientKeyPair.getPublic().getEncoded()));
		privatekey = factory.generatePrivate(new PKCS8EncodedKeySpec(serverKeyPair.getPrivate().getEncoded()));
		serverAgreement.init(privatekey);
		serverAgreement.doPhase(publicKey, true);
		assertArrayEquals(serverAgreement.generateSecret(), serverSharedKey);

		// X509和PKCS8是对公钥和私钥的格式化
		/*-
		 * X509 (ASN.1)
		 * 
		 * SubjectPublicKeyInfo ::= SEQUENCE {
		 *     algorithm AlgorithmIdentifier,
		 *     subjectPublicKey BIT STRING
		 * }
		 * 
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

		System.out.println(clientKeyPair.getPublic().getFormat());
		System.out.println(Utility.hex(clientKeyPair.getPublic().getEncoded()));
		// 302a300506032b656e03210001fc387be69a30eeee0a9605c0ac2cb2d72023aa8c830fd6e4843dbaa9f4017f
		// ------------------------99381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c

		System.out.println(serverKeyPair.getPrivate().getFormat());
		System.out.println(Utility.hex(serverKeyPair.getPrivate().getEncoded()));
		// 302e020100300506032b656e04220420c73f99a7e0f36556252b62207e7b6a7b82b3b4ee5d669298aa56bc10d47a836a
		// --------------------------------b1580eeadf6dd589b8ef4f2d5652578cc810e9980191ec8d058308cea216a21e

		final String ClientPublicKey = "99381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c";
		final String ServerPrivateKey = "b1580eeadf6dd589b8ef4f2d5652578cc810e9980191ec8d058308cea216a21e";
		// ServerPrivateKey+ClientPublickey:8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d

		publicKey = factory.generatePublic(new X509EncodedKeySpec(Utility.hex("302a300506032b656e032100" + ClientPublicKey)));
		privatekey = factory.generatePrivate(new PKCS8EncodedKeySpec(Utility.hex("302e020100300506032b656e04220420" + ServerPrivateKey)));

		final KeyAgreement agreement = KeyAgreement.getInstance(algorithm);
		agreement.init(privatekey);
		agreement.doPhase(publicKey, true);
		final byte[] sharedKey = agreement.generateSecret();
		assertArrayEquals(sharedKey, Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"));
	}
}