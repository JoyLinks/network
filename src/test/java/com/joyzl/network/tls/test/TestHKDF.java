package com.joyzl.network.tls.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;
import com.joyzl.network.tls.HKDF;

class TestHKDF {

	// RFC 5869

	@Test
	void testSample() throws Exception {
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

		final HKDF hkdf = new HKDF("HmacSHA256");
		final byte[] temp1 = hkdf.extract(salt, IKM);
		assertEquals(Utility.hex(temp1), PRK);

		final byte[] temp2 = hkdf.expand(temp1, info, 42);
		assertEquals(Utility.hex(temp2), OKM);
	}

	void testRFC8848() throws Exception {
		final String PRK = "33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a";
		final String info = "00200d746c733133206465726976656420e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
		final String expanded = "6f2615a108c702c5678f54fc9dbab69716c076189c48250cebeac3576c3611ba";

		final HKDF hkdf = new HKDF("HmacSHA256");
		final byte[] temp = hkdf.expand(Utility.hex(PRK), Utility.hex(info), 32);
		assertEquals(Utility.hex(temp), expanded);
	}
}