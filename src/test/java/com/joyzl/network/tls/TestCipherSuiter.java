package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Provider;
import java.security.Security;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;

class TestCipherSuiter {

	static {
		try {
			// 尝试动态加载密码算法提供者类
			Class<?> providerClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
			Provider bcProvider = (Provider) providerClass.getDeclaredConstructor().newInstance();
			Security.addProvider(bcProvider);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void testSecretKeySpec() {
		final byte[] key = new byte[16];
		TLS.RANDOM.nextBytes(key);
		final SecretKeySpec spec = new SecretKeySpec(key, "AES");

		assertArrayEquals(key, spec.getEncoded());
	}

	@Test
	void testV13() throws Exception {
		V13(NamedGroup.X25519, CipherSuite.TLS_AES_128_CCM_SHA256);
		V13(NamedGroup.X25519, CipherSuite.TLS_AES_128_CCM_8_SHA256);
		V13(NamedGroup.X25519, CipherSuite.TLS_AES_128_GCM_SHA256);
		V13(NamedGroup.X25519, CipherSuite.TLS_AES_256_GCM_SHA384);
		V13(NamedGroup.X25519, CipherSuite.TLS_CHACHA20_POLY1305_SHA256);

		V13(NamedGroup.X448, CipherSuite.TLS_AES_128_CCM_SHA256);
		V13(NamedGroup.X448, CipherSuite.TLS_AES_128_CCM_8_SHA256);
		V13(NamedGroup.X448, CipherSuite.TLS_AES_128_GCM_SHA256);
		V13(NamedGroup.X448, CipherSuite.TLS_AES_256_GCM_SHA384);
		V13(NamedGroup.X448, CipherSuite.TLS_CHACHA20_POLY1305_SHA256);

		V13(NamedGroup.SECP256R1, CipherSuite.TLS_AES_128_CCM_SHA256);
		V13(NamedGroup.SECP256R1, CipherSuite.TLS_AES_128_CCM_8_SHA256);
		V13(NamedGroup.SECP256R1, CipherSuite.TLS_AES_128_GCM_SHA256);
		V13(NamedGroup.SECP256R1, CipherSuite.TLS_AES_256_GCM_SHA384);
		V13(NamedGroup.SECP256R1, CipherSuite.TLS_CHACHA20_POLY1305_SHA256);
	}

	void V13(short group, short suite) throws Exception {
		final V3KeyExchange clientKE = new V3KeyExchange(group);
		final V3KeyExchange serverKE = new V3KeyExchange(group);

		final V3SecretCache serverSC = new V3SecretCache();
		final V3SecretCache clientSC = new V3SecretCache();

		final V3CipherSuiter server = new V3CipherSuiter();
		final V3CipherSuiter client = new V3CipherSuiter();

		server.suite(suite);
		client.suite(suite);
		serverSC.initialize(server.type());
		clientSC.initialize(client.type());

		serverSC.earlySecret(null);
		clientSC.earlySecret(null);

		serverSC.sharedKey(serverKE.sharedKey(clientKE.publicKey()));
		clientSC.sharedKey(clientKE.sharedKey(serverKE.publicKey()));

		serverSC.serverApplicationTrafficSecret();
		serverSC.clientApplicationTrafficSecret();
		server.encryptReset(serverSC.serverApplicationWriteKey(server.type()), serverSC.serverApplicationWriteIV(server.type()));
		server.decryptReset(serverSC.clientApplicationWriteKey(server.type()), serverSC.clientApplicationWriteIV(server.type()));

		client.encryptReset(clientSC.clientApplicationWriteKey(client.type()), clientSC.clientApplicationWriteIV(client.type()));
		client.decryptReset(clientSC.serverApplicationWriteKey(client.type()), clientSC.serverApplicationWriteIV(client.type()));

		final DataBuffer plain = DataBuffer.instance();
		final DataBuffer temp1 = DataBuffer.instance();
		final DataBuffer temp2 = DataBuffer.instance();

		plain.writeASCIIs("1234567890");

		temp1.replicate(plain);
		server.encryptAEAD(temp1.readable() + server.tagLength());
		server.encryptFinal(temp1, temp2);

		client.decryptAEAD(temp2.readable());
		client.decryptFinal(temp2, temp1);
		assertEquals(temp1, plain);

		client.encryptAEAD(temp1.readable() + server.tagLength());
		client.encryptFinal(temp1, temp2);

		server.decryptAEAD(temp2.readable());
		server.decryptFinal(temp2, temp1);
		assertEquals(temp1, plain);
	}

	@Test
	void testV12() throws Exception {
		aead(CipherSuite.TLS_AES_128_CCM_SHA256);

		block(CipherSuite.TLS_KRB5_WITH_3DES_EDE_CBC_MD5);
		block(CipherSuite.TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA);
		block(CipherSuite.TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA);
		block(CipherSuite.TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA);
		block(CipherSuite.TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA);
		block(CipherSuite.TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA);
		block(CipherSuite.TLS_KRB5_WITH_3DES_EDE_CBC_SHA);
		block(CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA);

		block(CipherSuite.TLS_DH_ANON_WITH_AES_128_CBC_SHA);
		block(CipherSuite.TLS_DH_DSS_WITH_AES_128_CBC_SHA);
		block(CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA);
		block(CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA);
		block(CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA);
		block(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);

		block(CipherSuite.TLS_DH_ANON_WITH_AES_128_CBC_SHA256);
		block(CipherSuite.TLS_DH_DSS_WITH_AES_128_CBC_SHA256);
		block(CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA256);
		block(CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA256);
		block(CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256);
		block(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256);

		block(CipherSuite.TLS_DH_ANON_WITH_AES_256_CBC_SHA);
		block(CipherSuite.TLS_DH_DSS_WITH_AES_256_CBC_SHA);
		block(CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA);
		block(CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA);
		block(CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA);
		block(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA);

		block(CipherSuite.TLS_DH_ANON_WITH_AES_256_CBC_SHA256);
		block(CipherSuite.TLS_DH_DSS_WITH_AES_256_CBC_SHA256);
		block(CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA256);
		block(CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256);
		block(CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256);
		block(CipherSuite.TLS_RSA_WITH_AES_256_CBC_SHA256);

		block(CipherSuite.TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5);
		block(CipherSuite.TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA);
		block(CipherSuite.TLS_KRB5_WITH_DES_CBC_MD5);

		block(CipherSuite.TLS_DH_ANON_WITH_DES_CBC_SHA);
		block(CipherSuite.TLS_DH_DSS_WITH_DES_CBC_SHA);
		block(CipherSuite.TLS_DH_RSA_WITH_DES_CBC_SHA);
		block(CipherSuite.TLS_DHE_DSS_WITH_DES_CBC_SHA);
		block(CipherSuite.TLS_DHE_RSA_WITH_DES_CBC_SHA);
		block(CipherSuite.TLS_KRB5_WITH_DES_CBC_SHA);
		block(CipherSuite.TLS_RSA_WITH_DES_CBC_SHA);

		block(CipherSuite.TLS_DH_ANON_EXPORT_WITH_DES40_CBC_SHA);
		block(CipherSuite.TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA);
		block(CipherSuite.TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA);
		block(CipherSuite.TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA);
		block(CipherSuite.TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA);
		block(CipherSuite.TLS_RSA_EXPORT_WITH_DES40_CBC_SHA);

		block(CipherSuite.TLS_KRB5_WITH_IDEA_CBC_MD5);
		block(CipherSuite.TLS_KRB5_WITH_IDEA_CBC_SHA);
		block(CipherSuite.TLS_RSA_WITH_IDEA_CBC_SHA);

		block(CipherSuite.TLS_KRB5_EXPORT_WITH_RC2_CBC_40_MD5);
		block(CipherSuite.TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5);
		block(CipherSuite.TLS_KRB5_EXPORT_WITH_RC2_CBC_40_SHA);

		stream(CipherSuite.TLS_RSA_WITH_RC4_128_MD5);
		stream(CipherSuite.TLS_DH_ANON_WITH_RC4_128_MD5);
		stream(CipherSuite.TLS_KRB5_WITH_RC4_128_MD5);
		stream(CipherSuite.TLS_RSA_WITH_RC4_128_MD5);
		stream(CipherSuite.TLS_KRB5_WITH_RC4_128_SHA);
		stream(CipherSuite.TLS_RSA_WITH_RC4_128_SHA);
		stream(CipherSuite.TLS_DH_ANON_EXPORT_WITH_RC4_40_MD5);
		stream(CipherSuite.TLS_KRB5_EXPORT_WITH_RC4_40_MD5);
		stream(CipherSuite.TLS_RSA_EXPORT_WITH_RC4_40_MD5);
		stream(CipherSuite.TLS_KRB5_EXPORT_WITH_RC4_40_SHA);
	}

	void aead(short code) throws Exception {
		final V2CipherSuiter client = new V2CipherSuiter();
		final V2CipherSuiter server = new V2CipherSuiter();

		client.suite(code);
		server.suite(code);

		final byte[] client_write_key = new byte[client.keyLength()];
		final byte[] server_write_key = new byte[client.keyLength()];
		final byte[] client_write_IV = new byte[client.ivLength()];
		final byte[] server_write_IV = new byte[server.ivLength()];

		TLS.RANDOM.nextBytes(client_write_key);
		TLS.RANDOM.nextBytes(server_write_key);
		TLS.RANDOM.nextBytes(client_write_IV);
		TLS.RANDOM.nextBytes(server_write_IV);

		client.encryptReset(client_write_key, client_write_IV);
		client.decryptReset(server_write_key, server_write_IV);

		server.encryptReset(server_write_key, server_write_IV);
		server.decryptReset(client_write_key, client_write_IV);

		final DataBuffer plain = DataBuffer.instance();
		plain.writeASCIIs("1234567890");
		final DataBuffer temp = DataBuffer.instance();
		final DataBuffer cipe = DataBuffer.instance();

		temp.replicate(plain);

		client.encryptAEAD((byte) 0, TLS.V12, temp.readable() + client.tagLength());
		client.encryptFinal(temp, cipe);

		server.decryptAEAD((byte) 0, TLS.V12, cipe.readable());
		server.decryptFinal(cipe, temp);

		assertEquals(temp, plain);
	}

	void block(short code) throws Exception {
		final V2CipherSuiter client = new V2CipherSuiter();
		final V2CipherSuiter server = new V2CipherSuiter();

		client.suite(code);
		server.suite(code);

		final byte[] client_write_key = new byte[client.keyLength()];
		final byte[] server_write_key = new byte[client.keyLength()];
		final byte[] client_write_IV = new byte[client.ivLength()];
		final byte[] server_write_IV = new byte[server.ivLength()];

		TLS.RANDOM.nextBytes(client_write_key);
		TLS.RANDOM.nextBytes(server_write_key);
		TLS.RANDOM.nextBytes(client_write_IV);
		TLS.RANDOM.nextBytes(server_write_IV);

		client.encryptReset(client_write_key, client_write_IV);
		client.decryptReset(server_write_key, server_write_IV);

		server.encryptReset(server_write_key, server_write_IV);
		server.decryptReset(client_write_key, client_write_IV);

		final DataBuffer plain = DataBuffer.instance();
		plain.writeASCIIs("1234567890");
		final DataBuffer temp = DataBuffer.instance();
		final DataBuffer cipe = DataBuffer.instance();

		temp.replicate(plain);
		RecordCoder.paddingBlock(temp, client.blockLength());

		client.encryptBlock();
		client.encryptFinal(temp, cipe);

		server.decryptBlock();
		server.decryptFinal(cipe, temp);

		int p = temp.backByte();
		while (p-- > 0) {
			temp.backByte();
		}

		assertEquals(temp, plain);
	}

	void stream(short code) throws Exception {
		final V2CipherSuiter client = new V2CipherSuiter();
		final V2CipherSuiter server = new V2CipherSuiter();

		client.suite(code);
		server.suite(code);

		final byte[] client_write_key = new byte[client.keyLength()];
		final byte[] server_write_key = new byte[client.keyLength()];

		TLS.RANDOM.nextBytes(client_write_key);
		TLS.RANDOM.nextBytes(server_write_key);

		client.encryptReset(client_write_key, null);
		client.decryptReset(server_write_key, null);

		server.encryptReset(server_write_key, null);
		server.decryptReset(client_write_key, null);

		final DataBuffer plain = DataBuffer.instance();
		plain.writeASCIIs("1234567890");
		final DataBuffer temp = DataBuffer.instance();
		final DataBuffer cipe = DataBuffer.instance();

		temp.replicate(plain);

		client.encryptStream();
		client.encryptFinal(temp, cipe);

		server.decryptStream();
		server.decryptFinal(cipe, temp);

		assertEquals(temp, plain);
	}
}