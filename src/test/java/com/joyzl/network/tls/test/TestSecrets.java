package com.joyzl.network.tls.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;
import com.joyzl.network.tls.CipherSuite;
import com.joyzl.network.tls.ClientSecrets;
import com.joyzl.network.tls.ServerSecrets;

class TestSecrets {

	// RFC8448 Example Handshake Traces for TLS 1.3
	// 勘误：
	// 000d0020001e040305030603020308040805080604010501060102010402050206020202
	// 000d0018001604030503060302030804080508060401050106010201

	// x25519 key pair
	final String ClientPrivateKey = "49af42ba7f7994852d713ef2784bcbcaa7911de26adc5642cb634540e7ea5005";
	final String ClientPublicKey = "99381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c";
	// x25519 key pair
	final String ServerPrivateKey = "b1580eeadf6dd589b8ef4f2d5652578cc810e9980191ec8d058308cea216a21e";
	final String ServerPublicKey = "c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f";
	// Messages
	final String ClientHello = "010000c00303cb34ecb1e78163ba1c38c6dacb196a6dffa21a8d9912ec18a2ef6283024dece7000006130113031302010000910000000b0009000006736572766572ff01000100000a00140012001d0017001800190100010101020103010400230000003300260024001d002099381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c002b0003020304000d0020001e040305030603020308040805080604010501060102010402050206020202002d00020101001c00024001";
	final String ServerHello = "020000560303a6af06a4121860dc5e6e60249cd34c95930c8ac5cb1434dac155772ed3e2692800130100002e00330024001d0020c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f002b00020304";
	final String ServerEncryptedExtensions = "080000240022000a00140012001d00170018001901000101010201030104001c0002400100000000";
	final String ServerCertificate = "0b0001b9000001b50001b0308201ac30820115a003020102020102300d06092a864886f70d01010b0500300e310c300a06035504031303727361301e170d3136303733303031323335395a170d3236303733303031323335395a300e310c300a0603550403130372736130819f300d06092a864886f70d010101050003818d0030818902818100b4bb498f8279303d980836399b36c6988c0c68de55e1bdb826d3901a2461eafd2de49a91d015abbc9a95137ace6c1af19eaa6af98c7ced43120998e187a80ee0ccb0524b1b018c3e0b63264d449a6d38e22a5fda430846748030530ef0461c8ca9d9efbfae8ea6d1d03e2bd193eff0ab9a8002c47428a6d35a8d88d79f7f1e3f0203010001a31a301830090603551d1304023000300b0603551d0f0404030205a0300d06092a864886f70d01010b05000381810085aad2a0e5b9276b908c65f73a7267170618a54c5f8a7b337d2df7a594365417f2eae8f8a58c8f8172f9319cf36b7fd6c55b80f21a03015156726096fd335e5e67f2dbf102702e608ccae6bec1fc63a42a99be5c3eb7107c3c54e9b9eb2bd5203b1c3b84e0a8b2f759409ba3eac9d91d402dcc0cc8f8961229ac9187b42b4de10000";
	final String ServerCertificateVerify = "0f000084080400805a747c5d88fa9bd2e55ab085a61015b7211f824cd484145ab3ff52f1fda8477b0b7abc90db78e2d33a5c141a078653fa6bef780c5ea248eeaaa785c4f394cab6d30bbe8d4859ee511f602957b15411ac027671459e46445c9ea58c181e818e95b8c3fb0bf3278409d3be152a3da5043e063dda65cdf5aea20d53dfacd42f74f3";
	final String ServerFinished = "140000209b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718";
	final String ClientFinished = "14000020a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61";

	@Test
	void testServer() throws Exception {
		final ServerSecrets secrets = new ServerSecrets(CipherSuite.TLS_AES_128_GCM_SHA256);

		// {server} extract secret "early"
		final byte[] early = secrets.extract(new byte[0], new byte[32]);
		assertArrayEquals(Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), early);
		assertArrayEquals(secrets.early(), early);

		// {server} derive secret for handshake "tls13 derived":
		byte[] derived = secrets.derived(early);
		assertArrayEquals(Utility.hex("6f2615a108c702c5678f54fc9dbab69716c076189c48250cebeac3576c3611ba"), derived);
		// {server} extract secret "handshake":
		final String ecdh_shared = "8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d";
		final byte[] handshake = secrets.handshake(derived, Utility.hex(ecdh_shared));
		assertArrayEquals(Utility.hex("1dc826e93606aa6fdc0aadc12f741b01046aa6b99f691ed221a9f0ca043fbeac"), handshake);
		assertArrayEquals(secrets.handshake(Utility.hex(ecdh_shared)), handshake);

		// {server} derive secret "tls13 c hs traffic":
		secrets.hash(Utility.hex(ClientHello));
		secrets.hash(Utility.hex(ServerHello));
		byte[] hash = secrets.hash();

		final byte[] clientHandshakeTraffic = secrets.clientHandshakeTraffic(handshake, hash);
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), clientHandshakeTraffic);
		assertArrayEquals(secrets.clientHandshakeTraffic(), clientHandshakeTraffic);

		// {server} derive secret "tls13 s hs traffic":
		final byte[] serverHandshakeTraffic = secrets.serverHandshakeTraffic(handshake, hash);
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), serverHandshakeTraffic);
		assertArrayEquals(secrets.serverHandshakeTraffic(), serverHandshakeTraffic);

		// {server} derive secret for master "tls13 derived":
		derived = secrets.derived(handshake);
		assertArrayEquals(Utility.hex("43de77e0c77713859a944db9db2590b53190a65b3ee2e4f12dd7a0bb7ce254b4"), derived);

		// {server} extract secret "master":
		final byte[] master = secrets.extract(derived, new byte[32]);
		assertArrayEquals(Utility.hex("18df06843d13a08bf2a449844c5f8a478001bc4d4c627984d5a41da8d0402919"), master);
		assertArrayEquals(secrets.master(), master);

		// {server} derive write traffic keys for handshake data:
		final byte[] key = secrets.key(serverHandshakeTraffic);
		final byte[] iv = secrets.iv(serverHandshakeTraffic);
		assertArrayEquals(Utility.hex("3fce516009c21727d0f2e4e86ee403bc"), key);
		assertArrayEquals(Utility.hex("5d313eb2671276ee13000b30"), iv);
		assertArrayEquals(secrets.handshakeTrafficWriteKey(), key);
		assertArrayEquals(secrets.handshakeTrafficWriteIv(), iv);

		// calculate finished "tls13 finished":
		// secrets.hash(Utility.hex(ClientHello));
		// secrets.hash(Utility.hex(ServerHello));
		secrets.hash(Utility.hex(ServerEncryptedExtensions));
		secrets.hash(Utility.hex(ServerCertificate));
		secrets.hash(Utility.hex(ServerCertificateVerify));
		hash = secrets.hash();
		final byte[] finished = secrets.finishedVerifyData(serverHandshakeTraffic, hash);
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), finished);
		assertArrayEquals(secrets.serverFinished(), finished);

		// {server} derive secret "tls13 c ap traffic":
		// {server} derive secret "tls13 s ap traffic":
		// secrets.hash(Utility.hex(ClientHello));
		// secrets.hash(Utility.hex(ServerHello));
		// secrets.hash(Utility.hex(ServerEncryptedExtensions));
		// secrets.hash(Utility.hex(ServerCertificate));
		// secrets.hash(Utility.hex(ServerCertificateVerify));
		secrets.hash(Utility.hex(ServerFinished));
		hash = secrets.hash();
		// System.out.println(Utility.hex(hash));
		final byte[] clientApplicationTraffic = secrets.clientApplicationTraffic(master, hash);
		assertArrayEquals(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"), clientApplicationTraffic);
		assertArrayEquals(secrets.clientApplicationTraffic(), clientApplicationTraffic);
		final byte[] serverApplicationTraffic = secrets.serverApplicationTraffic(master, hash);
		assertArrayEquals(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"), serverApplicationTraffic);
		assertArrayEquals(secrets.serverApplicationTraffic(), serverApplicationTraffic);

		// {server} derive secret "tls13 exp master":
		final byte[] exporterMaster = secrets.exporterMaster(master, hash);
		assertArrayEquals(Utility.hex("fe22f881176eda18eb8f44529e6792c50c9a3f89452f68d8ae311b4309d3cf50"), exporterMaster);
		assertArrayEquals(secrets.exporterMaster(), exporterMaster);

		// {server} derive write traffic keys for application data:
		final byte[] writeKey = secrets.key(serverApplicationTraffic);
		final byte[] writeIv = secrets.iv(serverApplicationTraffic);
		assertArrayEquals(Utility.hex("9f02283b6c9c07efc26bb9f2ac92e356"), writeKey);
		assertArrayEquals(Utility.hex("cf782b88dd83549aadf1e984"), writeIv);
		assertArrayEquals(secrets.applicationTrafficWriteKey(), writeKey);
		assertArrayEquals(secrets.applicationTrafficWriteIv(), writeIv);

		// {server} derive read traffic keys for handshake data:
		final byte[] readKey = secrets.key(clientHandshakeTraffic);
		final byte[] readIv = secrets.iv(clientHandshakeTraffic);
		assertArrayEquals(Utility.hex("dbfaa693d1762c5b666af5d950258d01"), readKey);
		assertArrayEquals(Utility.hex("5bd3c71b836e0b76bb73265f"), readIv);
		assertArrayEquals(secrets.handshakeTrafficReadKey(), readKey);
		assertArrayEquals(secrets.handshakeTrafficReadIv(), readIv);

		// {server} calculate finished "tls13 finished" (same as client)
		// secrets.hash(Utility.hex(ClientHello));
		// secrets.hash(Utility.hex(ServerHello));
		// secrets.hash(Utility.hex(ServerEncryptedExtensions));
		// secrets.hash(Utility.hex(ServerCertificate));
		// secrets.hash(Utility.hex(ServerCertificateVerify));
		// secrets.hash(Utility.hex(ServerFinished));
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), secrets.clientFinished());

		// {server} derive read traffic keys for application data (same as
		// client application data write traffic keys)
		assertArrayEquals(Utility.hex("17422dda596ed5d9acd890e3c63f5051"), secrets.applicationTrafficReadKey());
		assertArrayEquals(Utility.hex("5b78923dee08579033e523d9"), secrets.applicationTrafficReadIv());

		// {server} derive secret "tls13 res master" (same as client)
		// secrets.hash(Utility.hex(ClientHello));
		// secrets.hash(Utility.hex(ServerHello));
		// secrets.hash(Utility.hex(ServerEncryptedExtensions));
		// secrets.hash(Utility.hex(ServerCertificate));
		// secrets.hash(Utility.hex(ServerCertificateVerify));
		// secrets.hash(Utility.hex(ServerFinished));
		secrets.hash(Utility.hex(ClientFinished));
		assertArrayEquals(Utility.hex("7df235f2031d2a051287d02b0241b0bfdaf86cc856231f2d5aba46c434ec196c"), secrets.resumptionMaster());

		// {server} generate resumption secret "tls13 resumption":
		final byte[] nonce = new byte[] { 0, 0 };
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), secrets.resumption(nonce));

	}

	/*-
	 * {client} extract secret "early" (same as server early secret)
	 * {client} extract secret "handshake" (same as server handshake secret)
	 * {client} derive secret "tls13 c hs traffic" (same as server)
	 * {client} derive secret "tls13 s hs traffic" (same as server)
	 * {client} derive secret for master "tls13 derived" (same as server)
	 * {client} extract secret "master" (same as server master secret)
	 * {client} derive read traffic keys for handshake data (same as server handshake data write traffic keys)
	 * {client} calculate finished "tls13 finished" (same as server)
	 * {client} derive secret "tls13 c ap traffic" (same as server)
	 * {client} derive secret "tls13 s ap traffic" (same as server)
	 * {client} derive secret "tls13 exp master" (same as server)
	 * {client} derive write traffic keys for handshake data (same as server handshake data read traffic keys)
	 * {client} derive read traffic keys for application data (same as server application data write traffic keys)
	 */

	@Test
	void testClient() throws Exception {
		final ClientSecrets secrets = new ClientSecrets(CipherSuite.TLS_AES_128_GCM_SHA256);

		assertArrayEquals(Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), secrets.early());
		secrets.handshake(Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"));
		assertArrayEquals(Utility.hex("1dc826e93606aa6fdc0aadc12f741b01046aa6b99f691ed221a9f0ca043fbeac"), secrets.handshake());

		secrets.hash(Utility.hex(ClientHello));
		secrets.hash(Utility.hex(ServerHello));
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), secrets.clientHandshakeTraffic());
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), secrets.serverHandshakeTraffic());
		assertArrayEquals(Utility.hex("18df06843d13a08bf2a449844c5f8a478001bc4d4c627984d5a41da8d0402919"), secrets.master());
		assertArrayEquals(Utility.hex("3fce516009c21727d0f2e4e86ee403bc"), secrets.handshakeTrafficReadKey());
		assertArrayEquals(Utility.hex("5d313eb2671276ee13000b30"), secrets.handshakeTrafficReadIv());

		// secrets.hash(Utility.hex(ClientHello));
		// secrets.hash(Utility.hex(ServerHello));
		secrets.hash(Utility.hex(ServerEncryptedExtensions));
		secrets.hash(Utility.hex(ServerCertificate));
		secrets.hash(Utility.hex(ServerCertificateVerify));
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), secrets.serverFinished());

		// secrets.hash(Utility.hex(ClientHello));
		// secrets.hash(Utility.hex(ServerHello));
		// secrets.hash(Utility.hex(ServerEncryptedExtensions));
		// secrets.hash(Utility.hex(ServerCertificate));
		// secrets.hash(Utility.hex(ServerCertificateVerify));
		secrets.hash(Utility.hex(ServerFinished));
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), secrets.clientFinished());

		// {client} derive read traffic keys for application data:
		assertArrayEquals(Utility.hex("9f02283b6c9c07efc26bb9f2ac92e356"), secrets.applicationTrafficReadKey());
		assertArrayEquals(Utility.hex("cf782b88dd83549aadf1e984"), secrets.applicationTrafficReadIv());

		// {client} derive write traffic keys for application data:
		assertArrayEquals(Utility.hex("17422dda596ed5d9acd890e3c63f5051"), secrets.applicationTrafficWriteKey());
		assertArrayEquals(Utility.hex("5b78923dee08579033e523d9"), secrets.applicationTrafficWriteIv());

		// {client} derive secret "tls13 res master":
		// secrets.hash(Utility.hex(ClientHello));
		// secrets.hash(Utility.hex(ServerHello));
		// secrets.hash(Utility.hex(ServerEncryptedExtensions));
		// secrets.hash(Utility.hex(ServerCertificate));
		// secrets.hash(Utility.hex(ServerCertificateVerify));
		// secrets.hash(Utility.hex(ServerFinished));
		secrets.hash(Utility.hex(ClientFinished));
		assertArrayEquals(Utility.hex("7df235f2031d2a051287d02b0241b0bfdaf86cc856231f2d5aba46c434ec196c"), secrets.resumptionMaster());

		// {client} generate resumption secret "tls13 resumption" (same as
		// server)
		final byte[] nonce = new byte[] { 0, 0 };
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), secrets.resumption(nonce));
	}

	void temp() throws Exception {
		// ServerPrivateKey+ClientPublickey:8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d
		final PKCS8EncodedKeySpec serverSpec = new PKCS8EncodedKeySpec(Utility.hex(ServerPrivateKey), "X25519");
		final X509EncodedKeySpec clientSpec = new X509EncodedKeySpec(Utility.hex(ClientPublicKey), "X25519");
		System.out.println(serverSpec.getAlgorithm());
		KeyFactory factory = KeyFactory.getInstance("X25519");
		PublicKey publicKey = factory.generatePublic(clientSpec);
		PrivateKey privatekey = factory.generatePrivate(serverSpec);

		KeyAgreement ka = KeyAgreement.getInstance("X25519");
		ka.init(publicKey);
		ka.doPhase(privatekey, true);
		byte[] secret = ka.generateSecret();
		System.out.println(Utility.hex(secret));
	}
}