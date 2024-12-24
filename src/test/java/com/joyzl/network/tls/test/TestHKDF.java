package com.joyzl.network.tls.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;
import com.joyzl.network.tls.CipherSuite;
import com.joyzl.network.tls.HKDF;

class TestHKDF {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testSample() throws Exception {
		// RFC 5869
		// Hash = SHA-256
		// IKM = 0x0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b (22 octets)
		final byte[] IKM = new byte[] { 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b, 0x0b };
		// salt = 0x000102030405060708090a0b0c (13 octets)
		final byte[] salt = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c };
		// info = 0xf0f1f2f3f4f5f6f7f8f9 (10 octets)
		final byte[] info = new byte[] { (byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3, (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9 };
		// PRK=0x077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5(32octets)
		final String PRK = "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5";
		// L = 42
		// OKM=0x3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865(42octets)
		final String OKM = "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865";

		final HKDF hkdf = new HKDF(CipherSuite.TLS_AES_128_GCM_SHA256);
		final byte[] temp1 = hkdf.extract(salt, IKM);
		assertEquals(Utility.hex(temp1), PRK);

		final byte[] temp2 = hkdf.expand(temp1, info, 42);
		assertEquals(Utility.hex(temp2), OKM);
	}

}