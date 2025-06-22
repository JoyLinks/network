/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.junit.jupiter.api.Test;

public class TestSignaturer {

	@Test
	void testPKCS1() throws Exception {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		final KeyPair kp = keyPairGenerator.generateKeyPair();

		final Signaturer signaturer = new Signaturer();
		signaturer.setPrivateKey(kp.getPrivate());
		signaturer.setPublicKey(kp.getPublic());

		final byte[] text = "1234567890".getBytes(StandardCharsets.US_ASCII);
		byte[] temp = signaturer.encryptPKCS1(text);
		temp = signaturer.decryptPKCS1(temp);
		assertArrayEquals(temp, text);
	}

	@Test
	void testOAEP() throws Exception {
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		final KeyPair kp = keyPairGenerator.generateKeyPair();

		final Signaturer signaturer = new Signaturer();
		signaturer.setPrivateKey(kp.getPrivate());
		signaturer.setPublicKey(kp.getPublic());

		final byte[] text = "1234567890".getBytes(StandardCharsets.US_ASCII);

		byte[] temp = signaturer.encryptOAEP(text);
		temp = signaturer.decryptOAEP(temp);
		assertArrayEquals(temp, text);
	}
}