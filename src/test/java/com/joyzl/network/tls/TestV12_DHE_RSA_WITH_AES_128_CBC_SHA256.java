package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.joyzl.network.TestHelper;

class TestV12_DHE_RSA_WITH_AES_128_CBC_SHA256 extends TestHelper {

	final byte[] preMasterSecret = bytes("""
			04 a3 c2 98 f6 fe 52 b5 3e 41 b7 9d fa de 79 0d
			c9 51 47 35 df 59 a9 64 7d cb 12 e4 1a b3 99 6f
			34 03 36 a3 82 d0 73 26 98 e5 14 32 96 a5 79 16
			fc eb e6 de 6f 09 d1 f4 02 c6 06 a0 8e 0c 3d e9
			d1 69 eb e4 c7 1f f0 10 15 27 4c 77 a3 b9 cf 43
			1f 3d 60 74 33 c4 66 2c 73 53 9d a1 25 e0 7f 68
			66 ee 22 a9 bc d0 b9 46 03 20 ee 6f 33 7f 3d 59
			d5 42 56 99 3f d0 17 62 dc 78 84 38 19 81 e8 76
			""");

	final byte[] clientRandom = bytes("""
			64 61 aa 72 dd 0a d6 2b 42 11 c4 f6 45 40 cf f4
			c2 eb f3 37 55 8e c7 d0 91 04 06 2d f4 85 ce 55
			""");
	final byte[] clientHello = bytes("""
			01 00 01 2e 03 03 64 61 aa 72 dd 0a d6 2b 42 11
			c4 f6 45 40 cf f4 c2 eb f3 37 55 8e c7 d0 91 04
			06 2d f4 85 ce 55 00 00 b0 c0 30 c0 2c c0 28 c0
			24 c0 14 c0 0a 00 a5 00 a3 00 a1 00 9f 00 6b 00
			6a 00 69 00 68 00 39 00 38 00 37 00 36 00 88 00
			87 00 86 00 85 00 81 00 80 c0 32 c0 2e c0 2a c0
			26 c0 0f c0 05 00 9d 00 3d 00 35 00 84 c0 2f c0
			2b c0 27 c0 23 c0 13 c0 09 00 a4 00 a2 00 a0 00
			9e 00 67 00 40 00 3f 00 3e 00 33 00 32 00 31 00
			30 00 9a 00 99 00 98 00 97 00 45 00 44 00 43 00
			42 c0 31 c0 2d c0 29 c0 25 c0 0e c0 04 00 9c 00
			3c 00 2f 00 96 00 41 00 07 c0 11 c0 07 c0 0c c0
			02 00 05 00 04 c0 12 c0 08 00 16 00 13 00 10 00
			0d c0 0d c0 03 00 0a 00 ff 01 00 00 55 00 0b 00
			04 03 00 01 02 00 0a 00 1c 00 1a 00 17 00 19 00
			1c 00 1b 00 18 00 1a 00 16 00 0e 00 0d 00 0b 00
			0c 00 09 00 0a 00 23 00 00 00 0d 00 20 00 1e 06
			01 06 02 06 03 05 01 05 02 05 03 04 01 04 02 04
			03 03 01 03 02 03 03 02 01 02 02 02 03 00 0f 00
			01 01""");
	final byte[] serverRandom = bytes("""
			5c 4a b2 c7 3d d5 c1 16 52 e7 48 ea 79 d4 3e 7e
			8c 81 21 37 82 4a 8d 0b cf a7 e1 16 7c bf e8 0e
			""");
	final byte[] serverHello = bytes("""
			02 00 00 4d 03 03 5c 4a b2 c7 3d d5 c1 16 52 e7
			48 ea 79 d4 3e 7e 8c 81 21 37 82 4a 8d 0b cf a7
			e1 16 7c bf e8 0e 20 5c 4a b2 c7 94 02 bb 63 f4
			65 b2 51 90 c3 1c f4 ff de ef 04 ec fc 66 3f 41
			f3 0a fc d3 73 70 b5 00 67 00 00 05 ff 01 00 01
			00""");
	final byte[] serverCertificate = bytes("""
			0b 00 03 8f 00 03 8c 00 03 89 30 82 03 85 30 82
			02 6d a0 03 02 01 02 02 09 00 ad e0 51 8f d5 09
			f3 bc 30 0d 06 09 2a 86 48 86 f7 0d 01 01 0b 05
			00 30 58 31 0b 30 09 06 03 55 04 06 13 02 43 4e
			31 0b 30 09 06 03 55 04 08 0c 02 58 58 31 0b 30
			09 06 03 55 04 07 0c 02 58 58 31 1c 30 1a 06 03
			55 04 0a 0c 13 44 65 66 61 75 6c 74 20 43 6f 6d
			70 61 6e 79 20 4c 74 64 31 11 30 0f 06 03 55 04
			03 0c 08 74 65 73 74 2e 63 6f 6d 30 20 17 0d 31
			39 30 31 32 33 30 31 34 37 32 31 5a 18 0f 32 31
			31 38 31 32 33 30 30 31 34 37 32 31 5a 30 58 31
			0b 30 09 06 03 55 04 06 13 02 43 4e 31 0b 30 09
			06 03 55 04 08 0c 02 58 58 31 0b 30 09 06 03 55
			04 07 0c 02 58 58 31 1c 30 1a 06 03 55 04 0a 0c
			13 44 65 66 61 75 6c 74 20 43 6f 6d 70 61 6e 79
			20 4c 74 64 31 11 30 0f 06 03 55 04 03 0c 08 74
			65 73 74 2e 63 6f 6d 30 82 01 22 30 0d 06 09 2a
			86 48 86 f7 0d 01 01 01 05 00 03 82 01 0f 00 30
			82 01 0a 02 82 01 01 00 cd 79 c6 0c 71 f8 c0 34
			c1 c7 a9 1a f5 9a 96 42 54 e1 5b ad 10 c0 51 76
			7d 91 2b de 0b 8f af 61 2d 6d 28 44 2e 35 71 65
			20 3b 3c b9 07 c1 4d a3 c8 c6 13 f9 00 3e 95 c2
			56 55 3b 8c d8 b8 65 82 73 00 dc 7a f5 13 bf 74
			f2 82 71 85 66 fd bc 34 23 43 04 a0 4f d7 bc 96
			54 88 c2 91 de b3 a4 04 13 83 6c 27 a0 d8 52 d5
			b1 48 dd e9 07 a3 9c f2 51 dc 0e e2 72 ac 38 85
			3d 9b 76 34 0c 02 b3 86 bb 06 6d 8e f0 d2 1f 09
			56 db d8 b4 2f 70 76 30 34 97 b0 0e 83 03 59 58
			62 bb 4d 5e 1b 75 de d5 f9 5a be 65 25 92 d7 07
			8d 3c d2 cb 7e 46 06 27 51 59 6d 7c 3c 00 fb 3d
			fa 9e 99 91 cd 49 cf ce 11 d7 be 06 3e 18 50 48
			19 d6 0d a3 55 06 97 a1 a6 fa 2c 55 7b ba 91 18
			7d 75 74 9c d0 c8 a5 8c 00 af f3 04 ae d7 87 d1
			32 3c 07 cd f1 3f cc 1d 38 88 33 82 da d3 29 f8
			f0 c8 68 c9 8a 63 21 eb 02 03 01 00 01 a3 50 30
			4e 30 1d 06 03 55 1d 0e 04 16 04 14 92 a1 d9 27
			21 65 2a bc da fd c7 9d ef 5c 5a 70 40 04 1d 98
			30 1f 06 03 55 1d 23 04 18 30 16 80 14 92 a1 d9
			27 21 65 2a bc da fd c7 9d ef 5c 5a 70 40 04 1d
			98 30 0c 06 03 55 1d 13 04 05 30 03 01 01 ff 30
			0d 06 09 2a 86 48 86 f7 0d 01 01 0b 05 00 03 82
			01 01 00 25 6b d1 da 31 63 ba 8c 71 dd 28 46 5a
			19 63 8d 03 d0 0f 97 12 4f 95 21 3d d6 a3 90 14
			58 56 b9 79 cd a6 6a b8 ec ab 43 d8 60 db cc 4e
			ea 1e f4 09 3d 2c 61 59 89 ed 5b b7 01 a0 f0 4c
			fe c4 d6 fc 09 8e 86 dd 88 3d ae 60 61 95 5b 04
			93 1b c7 b8 44 8a 2b 86 9f 91 6f e3 54 41 7b 3a
			31 46 17 48 65 8d af 94 23 50 bc 76 a8 05 73 3c
			68 37 c8 19 a3 8a 33 43 cd 08 f6 7e 28 33 d8 0e
			e9 9f 72 f6 5f c8 fe e9 fd 32 6a d1 99 21 24 aa
			87 db 49 a2 48 2c cb b6 b7 db 22 67 8f e9 5f 6a
			dc 90 e0 ad 02 da ef e1 a5 56 58 32 e4 90 33 78
			bb b6 29 d3 17 6a f6 b8 c0 d4 0c c4 03 cb 94 64
			02 34 e3 7d f2 c6 75 1c 52 3d bd 02 bb 27 5d 4e
			57 f1 bc fa d9 57 45 e3 4c 2b 3c 65 fd f8 7e bb
			2f ea 61 a0 d2 9b 71 bf 7b 3e 70 81 d8 f3 86 d1
			c1 0a e2 8d 73 4c ec f9 ec ef 5f 19 ef 51 da 1a
			9a e3 53""");
	final byte[] serverKeyExchange = bytes("""
			0c 00 02 0b 00 80 ff ff ff ff ff ff ff ff c9 0f
			da a2 21 68 c2 34 c4 c6 62 8b 80 dc 1c d1 29 02
			4e 08 8a 67 cc 74 02 0b be a6 3b 13 9b 22 51 4a
			08 79 8e 34 04 dd ef 95 19 b3 cd 3a 43 1b 30 2b
			0a 6d f2 5f 14 37 4f e1 35 6d 6d 51 c2 45 e4 85
			b5 76 62 5e 7e c6 f4 4c 42 e9 a6 37 ed 6b 0b ff
			5c b6 f4 06 b7 ed ee 38 6b fb 5a 89 9f a5 ae 9f
			24 11 7c 4b 1f e6 49 28 66 51 ec e6 53 81 ff ff
			ff ff ff ff ff ff 00 01 02 00 80 78 8d 66 69 7b
			bf c9 01 f8 2c f0 02 cc 5b 70 cf af 53 4e 65 26
			19 16 48 21 7d 43 50 2f af a1 8c e8 c9 7c c6 52
			f9 a9 fc f9 8d 57 35 e9 c2 d6 41 75 4d 96 15 fa
			ae 3e 90 b5 47 96 1c 7e e9 10 46 d7 25 73 f7 c6
			f2 7b c0 10 3f 76 ab 5c c5 fd 65 ec d0 8f 36 c9
			28 66 3f 64 78 84 9c 5a 16 17 8e 78 f5 30 d1 11
			f9 d3 19 fa f8 83 2e 97 50 f7 d4 7a 70 b2 10 c2
			db 45 73 e6 ef 8a 1c 27 a2 73 86 06 01 01 00 a0
			fd b5 85 64 8d c2 fc 11 64 d6 d4 75 ec 9f b9 f4
			b9 2e 02 d5 61 90 56 98 1c 04 59 aa 16 f0 f1 75
			e1 20 af bf 18 55 b5 dc 10 69 39 d6 3b e2 fe 80
			20 a4 b3 64 95 76 58 ce e3 64 79 e5 74 65 9a f4
			82 bf 45 e7 1b cc ab 7d f1 a1 9a 7f b8 79 04 05
			d8 91 2d 46 a5 4b 74 e6 26 e3 f3 5c 60 7a 97 65
			d4 86 1b 5d 7b 79 29 03 09 55 2c 65 85 39 f0 2b
			f4 34 fb af 84 3b 78 0a bb e2 58 93 7f 7d b1 b0
			c7 9d a7 59 43 1e 42 b2 f0 b7 05 f0 d1 8d 94 b9
			08 81 f8 e0 fd ec 6d 90 ef d2 a3 24 1a b2 ae af
			ee bc 16 d6 f6 ea ee 1f 21 58 64 1b bf d9 ed aa
			00 2d 94 80 79 35 db c8 0b 11 38 51 9e b2 08 4f
			32 92 5f 9e fd d7 f4 8d 8b 4e 32 ce aa 7f f9 e4
			90 6c 7e 5f 19 07 b5 27 31 9c a0 0a 30 cc d8 03
			a2 ac 92 04 8b 84 36 99 18 a9 5c 38 94 24 5e 5d
			4d 1c b8 49 b1 72 96 31 3e 42 6b f1 6a b5 93
			""");
	final byte[] serverHelloDone = bytes("0e 00 00 00");
	final byte[] clientKeyExchange = bytes("""
			10 00 00 82 00 80 d0 c1 af 38 0b 94 93 df 22 e5
			be 3e d6 92 a2 80 ef 03 f0 75 d1 4c ab a0 83 e8
			8f f6 3f 9d c5 3e 3a 38 4e 3a 67 a2 89 d7 bb 4a
			63 41 b1 1d 07 e7 12 64 a8 60 72 75 f0 92 cb a2
			e4 13 e5 ab 7f 28 1a c5 4a 8a bc a5 0a 82 c0 b0
			78 ad 0d ce 1d b0 dd 7a 90 70 24 50 23 73 18 13
			d0 90 14 ce 03 35 24 16 ac 21 b8 34 f7 96 30 61
			63 0c 79 2b b6 79 d8 11 31 d0 02 cc 89 19 f7 90
			e7 db 48 eb fb 9b""");
	final byte[] clientFinishedEncrypted = bytes("""
			56 97 5a df 00 2e 21 91 5d fd ce 64 d7 c4 e3 4a
			be 38 8c 74 06 46 eb 3e c9 55 13 cc 87 23 c3 12
			5b a6 9d a5 f4 4d d7 d4 ff f4 1f 85 d4 52 a1 4c
			87 70 28 c7 e5 15 dd 1d 7e 74 57 c8 2f 83 e7 ce
			2a d2 24 45 45 a1 30 97 cc a1 c8 a3 78 d1 de eb
			""");
	final byte[] serverFinishedEncrypted = bytes("""
			c3 fd b3 84 2a 46 88 db fa fa 5d dc 5c fc 12 a2
			49 cb 83 aa ad 3c 2c fe 03 9d b5 a4 39 9d 8a 0c
			58 be c7 be 4b 90 24 7b d2 a7 d1 35 61 89 a1 b8
			7d 5c dc b4 ac 9e bf 86 81 5d 49 72 88 20 e4 1d
			b8 62 a3 1b 08 56 47 a3 d8 20 14 ed 88 19 f0 32
			""");

	@Test
	void test() throws Exception {
		final byte[] master = bytes("""
				a5 39 a7 83 4f e6 dd 94 58 4b 57 56 cc 3b aa 98
				51 f9 e7 ba 2c 03 78 bb 89 4a 96 31 af 30 50 29
				87 ee 0c e8 6e 76 9d b3 70 a6 98 5a f3 f0 d3 df
				""");

		final CipherSuiteType type = CipherSuiteType.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256;
		final V2SecretCache client = new V2SecretCache();
		final V2SecretCache server = new V2SecretCache();

		client.initialize(type);
		server.initialize(type);

		client.clientRandom(clientRandom);
		client.serverRandom(serverRandom);

		server.clientRandom(clientRandom);
		server.serverRandom(serverRandom);

		client.pms(preMasterSecret);
		server.pms(preMasterSecret);

		client.masterSecret();
		client.keyBlock(type);
		assertArrayEquals(client.master(), master);

		server.masterSecret();
		server.keyBlock(type);
		assertArrayEquals(client.master(), master);
	}

	@Test
	void testKeyExchange() {
		final byte[] server_dh_p = bytes("""
				ff ff ff ff ff ff ff ff c9 0f da a2 21 68 c2 34
				c4 c6 62 8b 80 dc 1c d1 29 02 4e 08 8a 67 cc 74
				02 0b be a6 3b 13 9b 22 51 4a 08 79 8e 34 04 dd
				ef 95 19 b3 cd 3a 43 1b 30 2b 0a 6d f2 5f 14 37
				4f e1 35 6d 6d 51 c2 45 e4 85 b5 76 62 5e 7e c6
				f4 4c 42 e9 a6 37 ed 6b 0b ff 5c b6 f4 06 b7 ed
				ee 38 6b fb 5a 89 9f a5 ae 9f 24 11 7c 4b 1f e6
				49 28 66 51 ec e6 53 81 ff ff ff ff ff ff ff ff
				""");
		final byte[] server_dh_g = new byte[] { 0x02 };
		// g^X mod p
		// server PUBKEY = g^privkey mod p
		final byte[] server_dh_Ys = bytes("""
				78 8d 66 69 7b bf c9 01 f8 2c f0 02 cc 5b 70 cf
				af 53 4e 65 26 19 16 48 21 7d 43 50 2f af a1 8c
				e8 c9 7c c6 52 f9 a9 fc f9 8d 57 35 e9 c2 d6 41
				75 4d 96 15 fa ae 3e 90 b5 47 96 1c 7e e9 10 46
				d7 25 73 f7 c6 f2 7b c0 10 3f 76 ab 5c c5 fd 65
				ec d0 8f 36 c9 28 66 3f 64 78 84 9c 5a 16 17 8e
				78 f5 30 d1 11 f9 d3 19 fa f8 83 2e 97 50 f7 d4
				7a 70 b2 10 c2 db 45 73 e6 ef 8a 1c 27 a2 73 86
				""");

		final byte[] client_dh_PrivateKey = bytes("""
				57 99 8b f0 c8 9b bd 7f 42 3a 57 c8 e6 ad 10 80
				ad 5e 25 7f e9 a3 c0 ea cc 28 21 35 bb f5 88 69
				d0 9c 03 0e a6 d3 9a 5d 93 5a 6b ff 0e aa 93 91
				a2 93 f9 5d fd dd a9 fa 26 e9 4a cd b3 17 b9 ab
				f9 b8 72 38 90 3c 0e 50 b2 b4 4a 40 61 dd 45 64
				f9 d2 cc d3 26 b3 e3 5c ac 02 0d 31 91 2a f5 46
				e3 58 70 6b 62 68 a3 be 93 7d 41 1b 1b a9 73 35
				1b 52 60 3d f8 d1 45 94 3c ff 76 bf a1 9a 07 7d
				""");
		final byte[] client_dh_Ys = bytes("""
				d0 c1 af 38 0b 94 93 df 22 e5 be 3e d6 92 a2 80
				ef 03 f0 75 d1 4c ab a0 83 e8 8f f6 3f 9d c5 3e
				3a 38 4e 3a 67 a2 89 d7 bb 4a 63 41 b1 1d 07 e7
				12 64 a8 60 72 75 f0 92 cb a2 e4 13 e5 ab 7f 28
				1a c5 4a 8a bc a5 0a 82 c0 b0 78 ad 0d ce 1d b0
				dd 7a 90 70 24 50 23 73 18 13 d0 90 14 ce 03 35
				24 16 ac 21 b8 34 f7 96 30 61 63 0c 79 2b b6 79
				d8 11 31 d0 02 cc 89 19 f7 90 e7 db 48 eb fb 9b
				""");

		BigInteger sp = new BigInteger(1, server_dh_p);
		BigInteger sg = new BigInteger(1, server_dh_g);
		BigInteger sy = new BigInteger(1, server_dh_Ys);
		BigInteger cp = new BigInteger(1, client_dh_PrivateKey);

		byte[] pms = sy.modPow(cp, sp).toByteArray();
		assertArrayEquals(preMasterSecret, pms);

		// g^X mod p
		byte[] cy = sg.xor(cp).mod(sp).toByteArray();
		// assertArrayEquals(client_dh_Ys, cy);
	}
}