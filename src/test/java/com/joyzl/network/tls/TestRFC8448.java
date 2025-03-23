package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;

class TestRFC8448 {

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

	// RFC8448 Example Handshake Traces for TLS 1.3
	// 勘误：
	// 000d0020001e040305030603020308040805080604010501060102010402050206020202
	// 000d0018001604030503060302030804080508060401050106010201

	// Simple 1-RTT Handshake Messages
	final String ClientHello = "010000c00303cb34ecb1e78163ba1c38c6dacb196a6dffa21a8d9912ec18a2ef6283024dece7000006130113031302010000910000000b0009000006736572766572ff01000100000a00140012001d0017001800190100010101020103010400230000003300260024001d002099381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c002b0003020304000d0020001e040305030603020308040805080604010501060102010402050206020202002d00020101001c00024001";
	final String ServerHello = "020000560303a6af06a4121860dc5e6e60249cd34c95930c8ac5cb1434dac155772ed3e2692800130100002e00330024001d0020c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f002b00020304";
	final String ServerEncryptedExtensions = "080000240022000a00140012001d00170018001901000101010201030104001c0002400100000000";
	final String ServerCertificate = "0b0001b9000001b50001b0308201ac30820115a003020102020102300d06092a864886f70d01010b0500300e310c300a06035504031303727361301e170d3136303733303031323335395a170d3236303733303031323335395a300e310c300a0603550403130372736130819f300d06092a864886f70d010101050003818d0030818902818100b4bb498f8279303d980836399b36c6988c0c68de55e1bdb826d3901a2461eafd2de49a91d015abbc9a95137ace6c1af19eaa6af98c7ced43120998e187a80ee0ccb0524b1b018c3e0b63264d449a6d38e22a5fda430846748030530ef0461c8ca9d9efbfae8ea6d1d03e2bd193eff0ab9a8002c47428a6d35a8d88d79f7f1e3f0203010001a31a301830090603551d1304023000300b0603551d0f0404030205a0300d06092a864886f70d01010b05000381810085aad2a0e5b9276b908c65f73a7267170618a54c5f8a7b337d2df7a594365417f2eae8f8a58c8f8172f9319cf36b7fd6c55b80f21a03015156726096fd335e5e67f2dbf102702e608ccae6bec1fc63a42a99be5c3eb7107c3c54e9b9eb2bd5203b1c3b84e0a8b2f759409ba3eac9d91d402dcc0cc8f8961229ac9187b42b4de10000";
	final String ServerCertificateVerify = "0f000084080400805a747c5d88fa9bd2e55ab085a61015b7211f824cd484145ab3ff52f1fda8477b0b7abc90db78e2d33a5c141a078653fa6bef780c5ea248eeaaa785c4f394cab6d30bbe8d4859ee511f602957b15411ac027671459e46445c9ea58c181e818e95b8c3fb0bf3278409d3be152a3da5043e063dda65cdf5aea20d53dfacd42f74f3";
	final String ServerFinished = "140000209b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718";
	final String ClientFinished = "14000020a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61";
	final String ServerNewSessionTicket = "040000c90000001efad6aac502000000b22c035d829359ee5ff7af4ec900000000262a6494dc486d2c8a34cb33fa90bf1b0070ad3c498883c9367c09a2be785abc55cd226097a3a982117283f82a03a143efd3ff5dd36d64e861be7fd61d2827db279cce145077d454a3664d4e6da4d29ee03725a6a4dafcd0fc67d2aea70529513e3da2677fa5906c5b3f7d8f92f228bda40dda721470f9fbf297b5aea617646fac5c03272e970727c621a79141ef5f7de6505e5bfbc388e93343694093934ae4d3570008002a000400000400";
	// Resumed 0-RTT Handshake Messages
	final String ClientHelloPrefix = "010001fc03031bc3ceb6bbe39cff938355b5a50adb6db21b7a6af649d7b4bc419d7876487d95000006130113031302010001cd0000000b0009000006736572766572ff01000100000a00140012001d00170018001901000101010201030104003300260024001d0020e4ffb68ac05f8d96c99da26698346c6be16482badddafe051a66b4f18d668f0b002a0000002b0003020304000d0020001e040305030603020308040805080604010501060102010402050206020202002d00020101001c0002400100150057000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002900dd00b800b22c035d829359ee5ff7af4ec900000000262a6494dc486d2c8a34cb33fa90bf1b0070ad3c498883c9367c09a2be785abc55cd226097a3a982117283f82a03a143efd3ff5dd36d64e861be7fd61d2827db279cce145077d454a3664d4e6da4d29ee03725a6a4dafcd0fc67d2aea70529513e3da2677fa5906c5b3f7d8f92f228bda40dda721470f9fbf297b5aea617646fac5c03272e970727c621a79141ef5f7de6505e5bfbc388e93343694093934ae4d357fad6aacb";
	final String ServerHello1 = "0200005c03033ccfd2dec890222763472ae8136777c9d7358777bb66e91ea5122495f559ea2d00130100003400290002000000330024001d0020121761ee42c333e1b9e77b60dd57c2053cd94512ab47f115e86eff50942cea31002b00020304";
	final String EncryptedExtensions1 = "080000280026000a00140012001d00170018001901000101010201030104001c0002400100000000002a0000";
	final String ServerFinished1 = "1400002048d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2";
	final String EndOfEarlyData = "05000000";
	final String ClientFinished1 = "140000207230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d";

	@Test
	void testDeriveSecrets() throws Exception {
		// 根据RFC8448验证密钥推导

		final int keyLength = 16;
		final int ivLength = 12;

		final DeriveSecret client = new DeriveSecret("SHA-256", "HmacSHA256");
		final DeriveSecret server = new DeriveSecret("SHA-256", "HmacSHA256");

		final KeyExchange clientKeyExchange = new KeyExchange(NamedGroup.X25519);
		final KeyExchange serverKeyExchange = new KeyExchange(NamedGroup.X25519);

		////////////////////////////////////////////////////////////////////////////////
		// Simple 1-RTT Handshake
		////////////////////////////////////////////////////////////////////////////////

		// {client} create an ephemeral x25519 key pair
		clientKeyExchange.generate();
		clientKeyExchange.setPrivateKey(Utility.hex("49af42ba7f7994852d713ef2784bcbcaa7911de26adc5642cb634540e7ea5005"));
		clientKeyExchange.setPublicKey(Utility.hex("99381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c"));

		// {client} construct a ClientHello handshake message
		// {client} send handshake record (TLSPlaintext)

		// {server} extract secret "early"
		// salt: 0(all zero octets),IKM: 0(32)
		byte[] early = server.v13EarlySecret(null);
		assertArrayEquals(Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), early);

		// {server} create an ephemeral x25519 key pair
		serverKeyExchange.generate();
		serverKeyExchange.setPrivateKey(Utility.hex("b1580eeadf6dd589b8ef4f2d5652578cc810e9980191ec8d058308cea216a21e"));
		serverKeyExchange.setPublicKey(Utility.hex("c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f"));

		// {server} construct a ServerHello handshake message

		// {server} derive secret for handshake "tls13 derived"
		byte[] derived = server.v13DeriveSecret(early);
		assertArrayEquals(Utility.hex("6f2615a108c702c5678f54fc9dbab69716c076189c48250cebeac3576c3611ba"), derived);

		// {server} extract secret "handshake":
		byte[] sharedECDHKey = serverKeyExchange.sharedKey(clientKeyExchange.publicKey());
		assertArrayEquals(Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"), sharedECDHKey);
		byte[] handshake = server.v13HandshakeSecret(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("1dc826e93606aa6fdc0aadc12f741b01046aa6b99f691ed221a9f0ca043fbeac"), handshake);

		// MESSAGE HASH
		server.hash(Utility.hex(ClientHello));
		server.hash(Utility.hex(ServerHello));

		// System.out.println(Utility.hex(server.hash()));
		// 860c06edc07858ee8e78f0e7428c58edd6b43f2ca3e6e95f02ed063cf0e1cad8

		// {server} derive secret "tls13 c hs traffic":
		byte[] sClientHandshakeTraffic = server.v13ClientHandshakeTrafficSecret(handshake, server.hash());
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), sClientHandshakeTraffic);

		// {server} derive secret "tls13 s hs traffic":
		byte[] sServerHandshakeTraffic = server.v13ServerHandshakeTrafficSecret(handshake, server.hash());
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), sServerHandshakeTraffic);

		// {server} derive secret for master "tls13 derived":
		derived = server.v13DeriveSecret(handshake);
		assertArrayEquals(Utility.hex("43de77e0c77713859a944db9db2590b53190a65b3ee2e4f12dd7a0bb7ce254b4"), derived);

		// {server} extract secret "master":
		byte[] sMaster = server.v13MasterSecret(handshake);
		assertArrayEquals(Utility.hex("18df06843d13a08bf2a449844c5f8a478001bc4d4c627984d5a41da8d0402919"), sMaster);

		// {server} send handshake record (TLSCiphertext)

		// {server} derive write traffic keys for handshake data:
		byte[] key = server.v13WriteKey(sServerHandshakeTraffic, keyLength);
		byte[] iv = server.v13WriteIV(sServerHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("3fce516009c21727d0f2e4e86ee403bc"), key);
		assertArrayEquals(Utility.hex("5d313eb2671276ee13000b30"), iv);

		// {server} construct an EncryptedExtensions handshake message:
		// {server} construct a Certificate handshake message:
		// {server} construct a CertificateVerify handshake message:
		server.hash(Utility.hex(ServerEncryptedExtensions));
		server.hash(Utility.hex(ServerCertificate));
		server.hash(Utility.hex(ServerCertificateVerify));

		// {server} calculate finished "tls13 finished":
		byte[] finished = server.v13FinishedVerifyData(sServerHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), finished);

		// {server} construct a Finished handshake message:
		server.hash(Utility.hex(ServerFinished));

		// {server} send handshake record (TLSCiphertext)

		// {server} derive secret "tls13 c ap traffic":
		byte[] sClientApplicationTraffic = server.v13ClientApplicationTrafficSecret(sMaster, server.hash());
		assertArrayEquals(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"), sClientApplicationTraffic);

		// {server} derive secret "tls13 s ap traffic":
		byte[] sServerApplicationTraffic = server.v13ServerApplicationTrafficSecret(sMaster, server.hash());
		assertArrayEquals(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"), sServerApplicationTraffic);

		// {server} derive secret "tls13 exp master":
		byte[] exporter = server.v13ExporterMasterSecret(sMaster, server.hash());
		assertArrayEquals(Utility.hex("fe22f881176eda18eb8f44529e6792c50c9a3f89452f68d8ae311b4309d3cf50"), exporter);

		// {server} derive write traffic keys for application data:
		key = server.v13WriteKey(sServerApplicationTraffic, keyLength);
		iv = server.v13WriteIV(sServerApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("9f02283b6c9c07efc26bb9f2ac92e356"), key);
		assertArrayEquals(Utility.hex("cf782b88dd83549aadf1e984"), iv);

		// {server} derive read traffic keys for handshake data:
		key = server.v13WriteKey(sClientHandshakeTraffic, keyLength);
		iv = server.v13WriteIV(sClientHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("dbfaa693d1762c5b666af5d950258d01"), key);
		assertArrayEquals(Utility.hex("5bd3c71b836e0b76bb73265f"), iv);

		// {client} extract secret "early"
		// (same as server early secret)
		early = client.v13EarlySecret(null);
		assertArrayEquals(Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), early);

		// {client} derive secret for handshake "tls13 derived"
		derived = client.v13DeriveSecret(early);
		assertArrayEquals(Utility.hex("6f2615a108c702c5678f54fc9dbab69716c076189c48250cebeac3576c3611ba"), derived);

		// {client} extract secret "handshake"
		// (same as server handshake secret)
		sharedECDHKey = clientKeyExchange.sharedKey(serverKeyExchange.publicKey());
		assertArrayEquals(Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"), sharedECDHKey);
		handshake = client.v13HandshakeSecret(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("1dc826e93606aa6fdc0aadc12f741b01046aa6b99f691ed221a9f0ca043fbeac"), handshake);

		client.hash(Utility.hex(ClientHello));
		client.hash(Utility.hex(ServerHello));

		// {client} derive secret "tls13 c hs traffic"
		// (same as server)
		byte[] cClientHandshakeTraffic = client.v13ClientHandshakeTrafficSecret(handshake, client.hash());
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), cClientHandshakeTraffic);

		// {client} derive secret "tls13 s hs traffic"
		// (same as server)
		byte[] cServerHandshakeTraffic = client.v13ServerHandshakeTrafficSecret(handshake, client.hash());
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), cServerHandshakeTraffic);

		// {client} derive secret for master "tls13 derived"
		// (same as server)
		derived = client.v13DeriveSecret(handshake);
		assertArrayEquals(Utility.hex("43de77e0c77713859a944db9db2590b53190a65b3ee2e4f12dd7a0bb7ce254b4"), derived);

		// {client} extract secret "master"
		// (same as server master secret)
		byte[] cMaster = client.v13MasterSecret(handshake);
		assertArrayEquals(Utility.hex("18df06843d13a08bf2a449844c5f8a478001bc4d4c627984d5a41da8d0402919"), cMaster);

		// {client} derive read traffic keys for handshake data
		// (same as server handshake data write traffic keys)
		key = client.v13WriteKey(cServerHandshakeTraffic, keyLength);
		iv = client.v13WriteIV(cServerHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("3fce516009c21727d0f2e4e86ee403bc"), key);
		assertArrayEquals(Utility.hex("5d313eb2671276ee13000b30"), iv);

		client.hash(Utility.hex(ServerEncryptedExtensions));
		client.hash(Utility.hex(ServerCertificate));
		client.hash(Utility.hex(ServerCertificateVerify));

		// {client} calculate finished "tls13 finished"
		// (same as server)
		finished = client.v13FinishedVerifyData(cServerHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), finished);

		// {client} derive secret "tls13 c ap traffic"
		// (same as server)
		byte[] cClientApplicationTraffic = client.v13ClientApplicationTrafficSecret(cMaster, server.hash());
		assertArrayEquals(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"), cClientApplicationTraffic);

		// {client} derive secret "tls13 s ap traffic"
		// (same as server)
		byte[] cServerApplicationTraffic = client.v13ServerApplicationTrafficSecret(cMaster, server.hash());
		assertArrayEquals(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"), cServerApplicationTraffic);

		client.hash(Utility.hex(ServerFinished));

		// {client} derive secret "tls13 exp master" (same as server)
		exporter = client.v13ExporterMasterSecret(cMaster, client.hash());
		assertArrayEquals(Utility.hex("fe22f881176eda18eb8f44529e6792c50c9a3f89452f68d8ae311b4309d3cf50"), exporter);

		// {client} derive write traffic keys for handshake data
		// (same as server handshake data read traffic keys)
		key = client.v13WriteKey(cClientHandshakeTraffic, keyLength);
		iv = client.v13WriteIV(cClientHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("dbfaa693d1762c5b666af5d950258d01"), key);
		assertArrayEquals(Utility.hex("5bd3c71b836e0b76bb73265f"), iv);

		// {client} derive read traffic keys for application data
		// (same as server application data write traffic keys)
		key = client.v13WriteKey(cServerApplicationTraffic, keyLength);
		iv = client.v13WriteIV(cServerApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("9f02283b6c9c07efc26bb9f2ac92e356"), key);
		assertArrayEquals(Utility.hex("cf782b88dd83549aadf1e984"), iv);

		// {client} calculate finished "tls13 finished":
		finished = client.v13FinishedVerifyData(cClientHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), finished);

		// {client} construct a Finished handshake message
		// {client} send handshake record (TLSCiphertext)
		client.hash(Utility.hex(ClientFinished));

		// {client} derive write traffic keys for application data
		key = client.v13WriteKey(cClientApplicationTraffic, keyLength);
		iv = client.v13WriteIV(cClientApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("17422dda596ed5d9acd890e3c63f5051"), key);
		assertArrayEquals(Utility.hex("5b78923dee08579033e523d9"), iv);

		// {client} derive secret "tls13 res master"
		byte[] cResumption = client.v13ResumptionMasterSecret(cMaster, client.hash());
		assertArrayEquals(Utility.hex("7df235f2031d2a051287d02b0241b0bfdaf86cc856231f2d5aba46c434ec196c"), cResumption);

		// {server} calculate finished "tls13 finished" (same as client)
		finished = server.v13FinishedVerifyData(sClientHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), finished);

		// {server} derive read traffic keys for application data
		// (same as client application data write traffic keys)
		key = client.v13WriteKey(sClientApplicationTraffic, keyLength);
		iv = client.v13WriteIV(sClientApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("17422dda596ed5d9acd890e3c63f5051"), key);
		assertArrayEquals(Utility.hex("5b78923dee08579033e523d9"), iv);

		server.hash(Utility.hex(ClientFinished));

		// {server} derive secret "tls13 res master"
		// (same as client)
		byte[] sResumption = server.v13ResumptionMasterSecret(sMaster, server.hash());
		assertArrayEquals(Utility.hex("7df235f2031d2a051287d02b0241b0bfdaf86cc856231f2d5aba46c434ec196c"), sResumption);

		// {server} generate resumption secret "tls13 resumption"
		sResumption = server.v13ResumptionSecret(sResumption, new byte[] { 0, 0 });
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), sResumption);

		// {server} construct a NewSessionTicket handshake message
		// {server} send handshake record

		// {client} generate resumption secret "tls13 resumption"
		// (same as server)
		cResumption = client.v13ResumptionSecret(cResumption, new byte[] { 0, 0 });
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), cResumption);

		////////////////////////////////////////////////////////////////////////////////
		// Resumed 0-RTT Handshake
		////////////////////////////////////////////////////////////////////////////////

		// {client} create an ephemeral x25519 key pair
		clientKeyExchange.setPrivateKey(Utility.hex("bff91188283846dd6a2134ef7180ca2b0b14fb10dce707b5098c0dddc813b2df"));
		clientKeyExchange.setPublicKey(Utility.hex("e4ffb68ac05f8d96c99da26698346c6be16482badddafe051a66b4f18d668f0b"));

		// {client} extract secret "early"
		early = client.v13EarlySecret(cResumption);
		assertArrayEquals(Utility.hex("9b2188e9b2fc6d64d71dc329900e20bb41915000f678aa839cbb797cb7d8332c"), early);

		// {client} construct a ClientHello handshake message

		// {client} calculate PSK binder:
		// PskBinderEntry 的计算方法和 Finished 消息一样
		client.hashReset();
		client.hash(Utility.hex(ClientHelloPrefix));
		// binder hash
		assertArrayEquals(Utility.hex("63224b2e4573f2d3454ca84b9d009a04f6be9e05711a8396473aefa01e924a14"), client.hash());
		// PRK "tls13 res binder"
		byte[] PRK = client.v13ResumptionBinderKey(early);
		assertArrayEquals(Utility.hex("69fe131a3bbad5d63c64eebcc30e395b9d8107726a13d074e389dbc8a4e47256"), PRK);
		// expanded(PRK,info="tls13 finished")
		byte[] expanded = client.v13ExpandLabel(PRK, "finished".getBytes(StandardCharsets.US_ASCII), null, client.hmacLength());
		assertArrayEquals(Utility.hex("5588673e72cb59c87d220caffe94f2dea9a3b1609f7d50e90a48227db9ed7eaa"), expanded);
		// finished(resumptionBinderKey,hash(ClientHelloPrefix))
		finished = client.v13FinishedVerifyData(PRK, client.hash());
		assertArrayEquals(Utility.hex("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"), finished);

		// {client} send handshake record (TLSPlaintext)
		// payload:ClientHelloPrefix+002120+3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d
		// 补齐PreSharedKey扩展，PskBinderEntry binders<33..2^16-1>;
		// uint16[uint8|vd]
		// FE 20 vd
		// NewSessionTicket
		// 040000c9 4+201
		// 0000001e 4 uint32 ticket_lifetime;
		// fad6aac5 4 uint32 ticket_age_add;
		// 020000 2+2 opaque ticket_nonce<0..255>;
		// 00b2 178 opaque ticket<1..2^16-1>;
		// ticket:2c035d829359ee5ff7af4ec900000000262a6494dc486d2c8a34cb33fa90bf1b0070ad3c498883c9367c09a2be785abc55cd226097a3a982117283f82a03a143efd3ff5dd36d64e861be7fd61d2827db279cce145077d454a3664d4e6da4d29ee03725a6a4dafcd0fc67d2aea70529513e3da2677fa5906c5b3f7d8f92f228bda40dda721470f9fbf297b5aea617646fac5c03272e970727c621a79141ef5f7de6505e5bfbc388e93343694093934ae4d357
		// 0008002a000400000400 extensions<0..2^16-2>;
		// ClientHello payload:512-prefix:477=35
		// 010001fc 508=512-4 长度已包含 binders

		// 补齐消息哈希
		client.hash(Utility.hex("0021203add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"));

		// {client} derive secret "tls13 c e traffic"
		byte[] clientEarlyTraffic = client.v13ClientEarlyTrafficSecret(early, client.hash());
		assertArrayEquals(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"), clientEarlyTraffic);

		// {client} derive secret "tls13 e exp master"
		byte[] earlyExporterMaster = client.v13EarlyExporterMasterSecret(early, client.hash());
		assertArrayEquals(Utility.hex("b2026866610937d7423e5be90862ccf24c0e6091186d34f812089ff5be2ef7df"), earlyExporterMaster);

		// {client} derive write traffic keys for early application data
		key = client.v13WriteKey(clientEarlyTraffic, keyLength);
		iv = client.v13WriteIV(clientEarlyTraffic, ivLength);
		assertArrayEquals(Utility.hex("920205a5b7bf2115e6fc5c2942834f54"), key);
		assertArrayEquals(Utility.hex("6d475f0993c8e564610db2b9"), iv);

		// {client} send application_data record

		// {server} extract secret "early"
		// (same as client early secret)
		early = server.v13EarlySecret(sResumption);
		assertArrayEquals(Utility.hex("9b2188e9b2fc6d64d71dc329900e20bb41915000f678aa839cbb797cb7d8332c"), early);

		// {server} calculate PSK binder
		// (same as client)
		server.hashReset();
		server.hash(Utility.hex(ClientHelloPrefix));
		PRK = server.v13ResumptionBinderKey(early);
		finished = server.v13FinishedVerifyData(PRK, server.hash());
		assertArrayEquals(Utility.hex("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"), finished);

		// {server} create an ephemeral x25519 key pair
		serverKeyExchange.setPrivateKey(Utility.hex("de5b4476e7b490b2652d338acbf2948066f255f9440e23b98fc69835298dc107"));
		serverKeyExchange.setPublicKey(Utility.hex("121761ee42c333e1b9e77b60dd57c2053cd94512ab47f115e86eff50942cea31"));

		// 补齐消息哈希(ClientHello)
		server.hash(Utility.hex("0021203add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"));

		// {server} derive secret "tls13 c e traffic" (same as client)
		clientEarlyTraffic = server.v13ClientEarlyTrafficSecret(early, server.hash());
		assertArrayEquals(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"), clientEarlyTraffic);

		// {server} derive secret "tls13 e exp master" (same as client)
		earlyExporterMaster = server.v13EarlyExporterMasterSecret(early, server.hash());
		assertArrayEquals(Utility.hex("b2026866610937d7423e5be90862ccf24c0e6091186d34f812089ff5be2ef7df"), earlyExporterMaster);

		// {server} construct a ServerHello handshake message
		server.hash(Utility.hex(ServerHello1));

		// {server} derive secret for handshake "tls13 derived"
		derived = server.v13DeriveSecret(early);
		assertArrayEquals(Utility.hex("5f1790bbd82c5e7d376ed2e1e52f8e6038c9346db61b43be9a52f77ef3998e80"), derived);

		// {server} extract secret "handshake"
		sharedECDHKey = serverKeyExchange.sharedKey(clientKeyExchange.publicKey());
		assertArrayEquals(Utility.hex("f44194756ff9ec9d25180635d66ea6824c6ab3bf179977be37f723570e7ccb2e"), sharedECDHKey);
		handshake = server.v13HandshakeSecret(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("005cb112fd8eb4ccc623bb88a07c64b3ede1605363fc7d0df8c7ce4ff0fb4ae6"), handshake);

		// {server} derive secret "tls13 c hs traffic"
		sClientHandshakeTraffic = server.v13ClientHandshakeTrafficSecret(handshake, server.hash());
		assertArrayEquals(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"), sClientHandshakeTraffic);

		// {server} derive secret "tls13 s hs traffic"
		sServerHandshakeTraffic = server.v13ServerHandshakeTrafficSecret(handshake, server.hash());
		assertArrayEquals(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"), sServerHandshakeTraffic);

		// {server} derive secret for master "tls13 derived"
		derived = server.v13DeriveSecret(handshake);
		assertArrayEquals(Utility.hex("e2f16030251df0874ba19b9aba257610bc6d531c1dd206df0ca6e84ae2a26742"), derived);

		// {server} extract secret "master":
		sMaster = server.v13MasterSecret(handshake);
		assertArrayEquals(Utility.hex("e2d32d4ed66dd37897a0e80c84107503ce58bf8aad4cb55a5002d77ecb890ece"), sMaster);

		// {server} send handshake record (TLSPlaintext)
		// payload:ServerHello1

		// {server} derive write traffic keys for handshake data
		key = client.v13WriteKey(sServerHandshakeTraffic, keyLength);
		iv = client.v13WriteIV(sServerHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("27c6bdc0a3dcea39a47326d79bc9e4ee"), key);
		assertArrayEquals(Utility.hex("9569ecdd4d0536705e9ef725"), iv);

		// {server} construct an EncryptedExtensions handshake message
		server.hash(Utility.hex(EncryptedExtensions1));

		// {server} calculate finished "tls13 finished"
		finished = server.v13FinishedVerifyData(sServerHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("48d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2"), finished);

		// {server} construct a Finished handshake message
		server.hash(Utility.hex(ServerFinished1));

		// {server} send handshake record
		// payload:EncryptedExtensions,Finished

		// {server} derive secret "tls13 c ap traffic"
		sClientApplicationTraffic = server.v13ClientApplicationTrafficSecret(sMaster, server.hash());
		assertArrayEquals(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"), sClientApplicationTraffic);

		// {server} derive secret "tls13 s ap traffic"
		sServerApplicationTraffic = server.v13ServerApplicationTrafficSecret(sMaster, server.hash());
		assertArrayEquals(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"), sServerApplicationTraffic);

		// {server} derive secret "tls13 exp master"
		exporter = server.v13ExporterMasterSecret(sMaster, server.hash());
		assertArrayEquals(Utility.hex("3fd93d4ffddc98e64b14dd107aedf8ee4add23f4510f58a4592d0b201bee56b4"), exporter);

		// {server} derive write traffic keys for application data
		key = server.v13WriteKey(sServerApplicationTraffic, keyLength);
		iv = server.v13WriteIV(sServerApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("e857c690a34c5a9129d833619684f95e"), key);
		assertArrayEquals(Utility.hex("0685d6b561aab9ef1013faf9"), iv);

		// {server} derive read traffic keys for early application data
		// (same as client early application data write traffic keys)
		key = server.v13WriteKey(clientEarlyTraffic, keyLength);
		iv = server.v13WriteIV(clientEarlyTraffic, ivLength);
		assertArrayEquals(Utility.hex("920205a5b7bf2115e6fc5c2942834f54"), key);
		assertArrayEquals(Utility.hex("6d475f0993c8e564610db2b9"), iv);

		// CLIENT
		client.hash(Utility.hex(ServerHello1));

		// {client} derive secret for handshake "tls13 derived"
		early = client.v13EarlySecret(cResumption);
		derived = client.v13DeriveSecret(early);
		assertArrayEquals(Utility.hex("5f1790bbd82c5e7d376ed2e1e52f8e6038c9346db61b43be9a52f77ef3998e80"), derived);

		// {client} extract secret "handshake"
		// (same as server handshake secret)
		sharedECDHKey = clientKeyExchange.sharedKey(serverKeyExchange.publicKey());
		assertArrayEquals(Utility.hex("f44194756ff9ec9d25180635d66ea6824c6ab3bf179977be37f723570e7ccb2e"), sharedECDHKey);
		handshake = client.v13HandshakeSecret(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("005cb112fd8eb4ccc623bb88a07c64b3ede1605363fc7d0df8c7ce4ff0fb4ae6"), handshake);

		// {client} derive secret "tls13 c hs traffic"
		// (same as server)
		cClientHandshakeTraffic = client.v13ClientHandshakeTrafficSecret(handshake, client.hash());
		assertArrayEquals(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"), cClientHandshakeTraffic);

		// {client} derive secret "tls13 s hs traffic"
		// (same as server)
		cServerHandshakeTraffic = client.v13ServerHandshakeTrafficSecret(handshake, client.hash());
		assertArrayEquals(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"), cServerHandshakeTraffic);

		// {client} derive secret for master "tls13 derived"
		// (same as server)
		derived = client.v13DeriveSecret(handshake);
		assertArrayEquals(Utility.hex("e2f16030251df0874ba19b9aba257610bc6d531c1dd206df0ca6e84ae2a26742"), derived);

		// {client} extract secret "master"
		// (same as server master secret)
		cMaster = client.v13MasterSecret(handshake);
		assertArrayEquals(Utility.hex("e2d32d4ed66dd37897a0e80c84107503ce58bf8aad4cb55a5002d77ecb890ece"), cMaster);

		// {client} derive read traffic keys for handshake data
		// (same as server handshake data write traffic keys)
		key = client.v13WriteKey(cServerHandshakeTraffic, keyLength);
		iv = client.v13WriteIV(cServerHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("27c6bdc0a3dcea39a47326d79bc9e4ee"), key);
		assertArrayEquals(Utility.hex("9569ecdd4d0536705e9ef725"), iv);

		client.hash(Utility.hex(EncryptedExtensions1));

		// {client} calculate finished "tls13 finished"
		// (same as server)
		client.v13FinishedVerifyData(cServerHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("48d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2"), finished);

		client.hash(Utility.hex(ServerFinished1));

		// {client} derive secret "tls13 c ap traffic"
		// (same as server)
		cClientApplicationTraffic = client.v13ClientApplicationTrafficSecret(sMaster, client.hash());
		assertArrayEquals(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"), cClientApplicationTraffic);

		// {client} derive secret "tls13 s ap traffic"
		// (same as server)
		cServerApplicationTraffic = client.v13ServerApplicationTrafficSecret(sMaster, client.hash());
		assertArrayEquals(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"), cServerApplicationTraffic);

		// {client} derive secret "tls13 exp master"
		// (same as server)
		exporter = client.v13ExporterMasterSecret(cMaster, client.hash());
		assertArrayEquals(Utility.hex("3fd93d4ffddc98e64b14dd107aedf8ee4add23f4510f58a4592d0b201bee56b4"), exporter);

		// {client} construct an EndOfEarlyData handshake message
		// {client} send handshake record
		client.hash(Utility.hex(EndOfEarlyData));

		// {client} derive write traffic keys for handshake data
		key = client.v13WriteKey(cClientHandshakeTraffic, keyLength);
		iv = client.v13WriteIV(cClientHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("b1530806f4adfeac83f1413032bbfa82"), key);
		assertArrayEquals(Utility.hex("eb50c16be7654abf99dd06d9"), iv);

		// {client} derive read traffic keys for application data
		// (same as server application data write traffic keys)
		key = client.v13WriteKey(cServerApplicationTraffic, keyLength);
		iv = client.v13WriteIV(cServerApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("e857c690a34c5a9129d833619684f95e"), key);
		assertArrayEquals(Utility.hex("0685d6b561aab9ef1013faf9"), iv);

		// {client} calculate finished "tls13 finished"
		finished = client.v13FinishedVerifyData(cClientHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("7230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d"), finished);

		// {client} construct a Finished handshake message
		// {client} send handshake record
		client.hash(Utility.hex(ClientFinished1));

		// {client} derive write traffic keys for application data
		key = client.v13WriteKey(cClientApplicationTraffic, keyLength);
		iv = client.v13WriteIV(cClientApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("3cf122f301c6358ca7989553250efd72"), key);
		assertArrayEquals(Utility.hex("ab1aec26aa78b8fc1176b9ac"), iv);

		// {client} derive secret "tls13 res master":
		cResumption = client.v13ResumptionMasterSecret(cMaster, client.hash());
		assertArrayEquals(Utility.hex("5e95bdf1f89005ea2e9aa0ba85e728e3c19c5fe0c699e3f5bee59faebd0b5406"), cResumption);

		// SERVER
		server.hash(Utility.hex(EndOfEarlyData));

		// {server} derive read traffic keys for handshake data
		// (same as client handshake data write traffic keys)
		key = client.v13WriteKey(sClientHandshakeTraffic, keyLength);
		iv = client.v13WriteIV(sClientHandshakeTraffic, ivLength);
		assertArrayEquals(Utility.hex("b1530806f4adfeac83f1413032bbfa82"), key);
		assertArrayEquals(Utility.hex("eb50c16be7654abf99dd06d9"), iv);

		// {server} calculate finished "tls13 finished"
		// (same as client)
		finished = server.v13FinishedVerifyData(cClientHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("7230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d"), finished);

		// {server} derive read traffic keys for application data
		// (same as client application data write traffic keys)
		key = server.v13WriteKey(sClientApplicationTraffic, keyLength);
		iv = server.v13WriteIV(sClientApplicationTraffic, ivLength);
		assertArrayEquals(Utility.hex("3cf122f301c6358ca7989553250efd72"), key);
		assertArrayEquals(Utility.hex("ab1aec26aa78b8fc1176b9ac"), iv);

		server.hash(Utility.hex(ClientFinished1));

		// {server} derive secret "tls13 res master"
		// (same as client)
		sResumption = server.v13ResumptionMasterSecret(sMaster, client.hash());
		assertArrayEquals(Utility.hex("5e95bdf1f89005ea2e9aa0ba85e728e3c19c5fe0c699e3f5bee59faebd0b5406"), sResumption);

		// {client} send application_data record
		// {server} send application_data record
		// {client} send alert record
		// {server} send alert record
		// System.out.println(Utility.hex(cResumption));
	}

	@Test
	void testHelloRetryRequestDeriveSecrets() throws Exception {
		final DeriveSecret client = new DeriveSecret("SHA-256", "HmacSHA256");
		final DeriveSecret server = new DeriveSecret("SHA-256", "HmacSHA256");

		// ClientHello (180 octets):
		String ClientHello1 = """
				01 00 00 b0 03 03 b0 b1 c5 a5 aa 37 c5
				91 9f 2e d1 d5 c6 ff f7 fc b7 84 97 16 94 5a 2b 8c ee 92 58 a3
				46 67 7b 6f 00 00 06 13 01 13 03 13 02 01 00 00 81 00 00 00 0b
				00 09 00 00 06 73 65 72 76 65 72 ff 01 00 01 00 00 0a 00 08 00
				06 00 1d 00 17 00 18 00 33 00 26 00 24 00 1d 00 20 e8 e8 e3 f3
				b9 3a 25 ed 97 a1 4a 7d ca cb 8a 27 2c 62 88 e5 85 c6 48 4d 05
				26 2f ca d0 62 ad 1f 00 2b 00 03 02 03 04 00 0d 00 20 00 1e 04
				03 05 03 06 03 02 03 08 04 08 05 08 06 04 01 05 01 06 01 02 01
				04 02 05 02 06 02 02 02 00 2d 00 02 01 01 00 1c 00 02 40 01""";
		client.hash(bytes(ClientHello1));
		server.hash(bytes(ClientHello1));

		// ServerHello (176 octets):HelloRetryRequest
		String ServerHello1 = """
				02 00 00 ac 03 03 cf 21 ad 74 e5 9a 61
				11 be 1d 8c 02 1e 65 b8 91 c2 a2 11 16 7a bb 8c 5e 07 9e 09 e2
				c8 a8 33 9c 00 13 01 00 00 84 00 33 00 02 00 17 00 2c 00 74 00
				72 71 dc d0 4b b8 8b c3 18 91 19 39 8a 00 00 00 00 ee fa fc 76
				c1 46 b8 23 b0 96 f8 aa ca d3 65 dd 00 30 95 3f 4e df 62 56 36
				e5 f2 1b b2 e2 3f cc 65 4b 1b 5b 40 31 8d 10 d1 37 ab cb b8 75
				74 e3 6e 8a 1f 02 5f 7d fa 5d 6e 50 78 1b 5e da 4a a1 5b 0c 8b
				e7 78 25 7d 16 aa 30 30 e9 e7 84 1d d9 e4 c0 34 22 67 e8 ca 0c
				af 57 1f b2 b7 cf f0 f9 34 b0 00 2b 00 02 03 04""";

		client.retry();
		client.hash(bytes(ServerHello1));
		server.retry();
		server.hash(bytes(ServerHello1));

		// ClientHello (512 octets):
		String ClientHello2 = """
				01 00 01 fc 03 03 b0 b1 c5 a5 aa 37 c5
				91 9f 2e d1 d5 c6 ff f7 fc b7 84 97 16 94 5a 2b 8c ee 92 58 a3
				46 67 7b 6f 00 00 06 13 01 13 03 13 02 01 00 01 cd 00 00 00 0b
				00 09 00 00 06 73 65 72 76 65 72 ff 01 00 01 00 00 0a 00 08 00
				06 00 1d 00 17 00 18 00 33 00 47 00 45 00 17 00 41 04 a6 da 73
				92 ec 59 1e 17 ab fd 53 59 64 b9 98 94 d1 3b ef b2 21 b3 de f2
				eb e3 83 0e ac 8f 01 51 81 26 77 c4 d6 d2 23 7e 85 cf 01 d6 91
				0c fb 83 95 4e 76 ba 73 52 83 05 34 15 98 97 e8 06 57 80 00 2b
				00 03 02 03 04 00 0d 00 20 00 1e 04 03 05 03 06 03 02 03 08 04
				08 05 08 06 04 01 05 01 06 01 02 01 04 02 05 02 06 02 02 02 00
				2c 00 74 00 72 71 dc d0 4b b8 8b c3 18 91 19 39 8a 00 00 00 00
				ee fa fc 76 c1 46 b8 23 b0 96 f8 aa ca d3 65 dd 00 30 95 3f 4e
				df 62 56 36 e5 f2 1b b2 e2 3f cc 65 4b 1b 5b 40 31 8d 10 d1 37
				ab cb b8 75 74 e3 6e 8a 1f 02 5f 7d fa 5d 6e 50 78 1b 5e da 4a
				a1 5b 0c 8b e7 78 25 7d 16 aa 30 30 e9 e7 84 1d d9 e4 c0 34 22
				67 e8 ca 0c af 57 1f b2 b7 cf f0 f9 34 b0 00 2d 00 02 01 01 00
				1c 00 02 40 01 00 15 00 af 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
				00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00""";
		client.hash(bytes(ClientHello2));
		server.hash(bytes(ClientHello2));

		// {server} extract secret "early":
		assertArrayEquals(server.v13EarlySecret(null), Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"));

		// ServerHello (123 octets):
		String ServerHello2 = """
				02 00 00 77 03 03 bb 34 1d 84 7f d7 89
				c4 7c 38 71 72 dc 0c 9b f1 47 fc ca cb 50 43 d8 6c a4 c5 98 d3
				ff 57 1b 98 00 13 01 00 00 4f 00 33 00 45 00 17 00 41 04 58 3e
				05 4b 7a 66 67 2a e0 20 ad 9d 26 86 fc c8 5b 5a d4 1a 13 4a 0f
				03 ee 72 b8 93 05 2b d8 5b 4c 8d e6 77 6f 5b 04 ac 07 d8 35 40
				ea b3 e3 d9 c5 47 bc 65 28 c4 31 7d 29 46 86 09 3a 6c ad 7d 00
				2b 00 02 03 04""";
		client.hash(bytes(ServerHello2));
		server.hash(bytes(ServerHello2));

		// {server} derive secret for handshake "tls13 derived":
		byte[] handshake = server.v13HandshakeSecret(//
			Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), //
			Utility.hex("c142ce13ca11b5c2233652e63ad3d97844f1621fbfb9de69d547dc8fedeabeb4"));
		assertArrayEquals(Utility.hex("ce022e5e6e81e50736d773f2d3adfce8220d049bf510f0dbfac927ef4243b148"), handshake);

		// HASH
		assertArrayEquals(Utility.hex("8aa8e828ec2f8a884fec95a3139de01c15a3daa7ff5bfc3f4bfcc21b438d7bf8"), server.hash());
		assertArrayEquals(Utility.hex("8aa8e828ec2f8a884fec95a3139de01c15a3daa7ff5bfc3f4bfcc21b438d7bf8"), client.hash());

		// {server} derive secret "tls13 c hs traffic":
		byte[] clientHandshakeTraffic = server.v13ClientHandshakeTrafficSecret(handshake, server.hash());
		assertArrayEquals(Utility.hex("158aa7ab8855073582b41d674b4055cabcc534728f659314861b4e08e2011566"), clientHandshakeTraffic);

		// {server} derive secret "tls13 s hs traffic":
		byte[] serverHandshakeTraffic = server.v13ServerHandshakeTrafficSecret(handshake, server.hash());
		assertArrayEquals(Utility.hex("3403e781e2af7b6508da28574f6e95a1abf162de83a97927c37672a4a0cef8a1"), serverHandshakeTraffic);

	}

	@Test
	void testSecretCache() throws Exception {
		// 根据RFC8448验证密钥获取与缓存

		final SecretCache clientSecrets = new SecretCache("SHA-256", "HmacSHA256");
		final SecretCache serverSecrets = new SecretCache("SHA-256", "HmacSHA256");

		final KeyExchange clientKeyExchange = new KeyExchange(NamedGroup.X25519);
		final KeyExchange serverKeyExchange = new KeyExchange(NamedGroup.X25519);

		byte[] temp;

		////////////////////////////////////////////////////////////////////////////////
		// Simple 1-RTT Handshake
		////////////////////////////////////////////////////////////////////////////////

		clientKeyExchange.setPrivateKey(Utility.hex("49af42ba7f7994852d713ef2784bcbcaa7911de26adc5642cb634540e7ea5005"));
		clientKeyExchange.setPublicKey(Utility.hex("99381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c"));

		serverKeyExchange.setPrivateKey(Utility.hex("b1580eeadf6dd589b8ef4f2d5652578cc810e9980191ec8d058308cea216a21e"));
		serverKeyExchange.setPublicKey(Utility.hex("c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f"));

		// {client} construct a ClientHello handshake message
		// {client} send handshake record (TLSPlaintext)
		clientSecrets.hash(buffer(ClientHello));
		// {server} received
		serverSecrets.hash(buffer(ClientHello));

		// {server} construct a ServerHello handshake message
		// {server} send handshake record (TLSPlaintext)
		serverSecrets.hash(buffer(ServerHello));

		// {server} 更新共享密钥，导出握手流量密钥
		temp = serverKeyExchange.sharedKey(clientKeyExchange.publicKey());
		serverSecrets.v13SharedKey(temp);
		assertArrayEquals(Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"), temp);
		temp = serverSecrets.v13ServerHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), temp);
		temp = serverSecrets.v13ClientHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), temp);

		// {client} received
		clientSecrets.hash(buffer(ServerHello));

		// {client} 更新共享密钥，导出握手流量密钥
		temp = clientKeyExchange.sharedKey(serverKeyExchange.publicKey());
		clientSecrets.v13SharedKey(temp);
		assertArrayEquals(Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"), temp);
		temp = clientSecrets.v13ClientHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), temp);
		temp = clientSecrets.v13ServerHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), temp);

		// {server} construct an EncryptedExtensions handshake message
		// {server} construct a Certificate handshake message
		// {server} construct a CertificateVerify handshake message
		serverSecrets.hash(buffer(ServerEncryptedExtensions));
		serverSecrets.hash(buffer(ServerCertificate));
		serverSecrets.hash(buffer(ServerCertificateVerify));

		// {server} 计算已发送的握手消息的完成校验码
		temp = serverSecrets.v13ServerFinished();
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), temp);

		// {server} construct a Finished handshake message
		// {server} send handshake record (TLSCiphertext)
		// payload:EncryptedExtentions+Certificate+CertificateVerify+Finished
		serverSecrets.hash(buffer(ServerFinished));

		// {server} 导出应用流量密钥
		temp = serverSecrets.v13ClientApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"), temp);
		temp = serverSecrets.v13ServerApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"), temp);

		// {client} received
		clientSecrets.hash(buffer(ServerEncryptedExtensions));
		clientSecrets.hash(buffer(ServerCertificate));
		clientSecrets.hash(buffer(ServerCertificateVerify));

		// {client} 校验服务端发送的握手完成校验码
		// 注意：在ServerFinished消息参与哈希之前获取校验码
		temp = clientSecrets.v13ServerFinished();
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), temp);
		clientSecrets.hash(buffer(ServerFinished));

		// {client} 计算已发送的握手消息的完成校验码
		// 注意：在ServerFinished消息参与哈希之后获取校验码
		temp = clientSecrets.v13ClientFinished();
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), temp);

		// {client} 导出应用流量密钥
		temp = clientSecrets.v13ClientApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"), temp);
		temp = clientSecrets.v13ServerApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"), temp);

		// {client} construct a Finished handshake message
		// {client} send handshake record (TLSCiphertext)
		clientSecrets.hash(buffer(ClientFinished));

		// {server} received
		temp = serverSecrets.v13ClientFinished();
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), temp);
		serverSecrets.hash(buffer(ClientFinished));

		// {server} construct a NewSessionTicket handshake message
		// {server} send handshake record (TLSCiphertext)
		// {server} 通过TicketNonce生成恢复密钥
		temp = serverSecrets.v13ResumptionMasterSecret();
		temp = serverSecrets.v13ResumptionSecret(new byte[] { 0, 0 });
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), temp);
		// {client} received
		// {client} 通过TicketNonce生成恢复密钥
		temp = clientSecrets.v13ResumptionMasterSecret();
		temp = clientSecrets.v13ResumptionSecret(new byte[] { 0, 0 });
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), temp);

		// {client} send application_data record (TLSCiphertext)
		// {server} send application_data record (TLSCiphertext)

		// {client} send alert record (TLSCiphertext)
		// {server} send alert record (TLSCiphertext)

		////////////////////////////////////////////////////////////////////////////////
		// Resumed 0-RTT Handshake
		////////////////////////////////////////////////////////////////////////////////

		clientKeyExchange.setPrivateKey(Utility.hex("bff91188283846dd6a2134ef7180ca2b0b14fb10dce707b5098c0dddc813b2df"));
		clientKeyExchange.setPublicKey(Utility.hex("e4ffb68ac05f8d96c99da26698346c6be16482badddafe051a66b4f18d668f0b"));

		serverKeyExchange.setPrivateKey(Utility.hex("de5b4476e7b490b2652d338acbf2948066f255f9440e23b98fc69835298dc107"));
		serverKeyExchange.setPublicKey(Utility.hex("121761ee42c333e1b9e77b60dd57c2053cd94512ab47f115e86eff50942cea31"));

		clientSecrets.v13Reset(temp);
		serverSecrets.v13Reset(temp);

		// {client} construct a ClientHello handshake message
		// ClientHello最后一个扩展的PskBinderEntry待计算后获得
		clientSecrets.hash(buffer(ClientHelloPrefix));

		// {client} calculate PSK binder
		temp = clientSecrets.v13ResumptionBinderKey();
		assertArrayEquals(Utility.hex("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"), temp);

		// {client} send handshake record (TLSPlaintext)
		// payload:ClientHelloPrefix+PskBinderEntry
		clientSecrets.hash(buffer("002120"));
		clientSecrets.hash(buffer("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"));

		// {client} 早期流量密钥
		temp = clientSecrets.v13ClientEarlyTrafficSecret();
		assertArrayEquals(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"), temp);
		temp = clientSecrets.v13EarlyExporterMasterSecret();
		assertArrayEquals(Utility.hex("b2026866610937d7423e5be90862ccf24c0e6091186d34f812089ff5be2ef7df"), temp);

		// {client} send application_data record (TLSCiphertext)

		// {server} received ClientHello
		// 接收后判定为0-RTT分两段执行消息哈希
		serverSecrets.hash(buffer(ClientHelloPrefix));
		// 验证 binder
		temp = serverSecrets.v13ResumptionBinderKey();
		assertArrayEquals(Utility.hex("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"), temp);

		serverSecrets.hash(buffer("002120"));
		serverSecrets.hash(buffer("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"));
		// {server} 早期流量密钥
		temp = serverSecrets.v13ClientEarlyTrafficSecret();
		assertArrayEquals(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"), temp);
		temp = serverSecrets.v13EarlyExporterMasterSecret();
		assertArrayEquals(Utility.hex("b2026866610937d7423e5be90862ccf24c0e6091186d34f812089ff5be2ef7df"), temp);

		// {server} construct a ServerHello handshake message
		// {server} send handshake record (TLSPlaintext)
		serverSecrets.hash(buffer(ServerHello1));

		// {server} 获取握手流量密钥
		temp = serverKeyExchange.sharedKey(clientKeyExchange.publicKey());
		serverSecrets.v13SharedKey(temp);
		temp = serverSecrets.v13ServerHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"), temp);
		temp = serverSecrets.v13ClientHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"), temp);

		// {server} construct an EncryptedExtensions handshake message
		serverSecrets.hash(buffer(EncryptedExtensions1));

		// {server} 计算握手完成校验码
		temp = serverSecrets.v13ServerFinished();
		assertArrayEquals(Utility.hex("48d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2"), temp);

		// {server} construct a Finished handshake message
		serverSecrets.hash(buffer(ServerFinished1));
		// {server} send handshake record (TLSCiphertext)
		// payload:EncryptedExtensions+Finished

		// {server} 获取应用流量密钥
		temp = serverSecrets.v13ServerApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"), temp);
		temp = serverSecrets.v13ClientApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"), temp);

		// {client} received ServerHello
		clientSecrets.hash(buffer(ServerHello1));

		// {client} 获取握手流量密钥
		temp = clientKeyExchange.sharedKey(serverKeyExchange.publicKey());
		clientSecrets.v13SharedKey(temp);
		temp = clientSecrets.v13ServerHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"), temp);
		temp = clientSecrets.v13ClientHandshakeTrafficSecret();
		assertArrayEquals(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"), temp);

		// {client} received EncryptedExtensions
		clientSecrets.hash(buffer(EncryptedExtensions1));
		// {client} received Finished
		// {client} 验证服务端发送的握手完成验证码
		temp = clientSecrets.v13ServerFinished();
		assertArrayEquals(Utility.hex("48d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2"), temp);
		clientSecrets.hash(buffer(ServerFinished1));

		// {client} 获取应用流量密钥
		temp = clientSecrets.v13ServerApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"), temp);
		temp = clientSecrets.v13ClientApplicationTrafficSecret();
		assertArrayEquals(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"), temp);

		// {client} construct an EndOfEarlyData handshake message
		// {client} send handshake record
		clientSecrets.hash(buffer(EndOfEarlyData));

		// {client} 计算握手完成校验码
		temp = clientSecrets.v13ClientFinished();
		assertArrayEquals(Utility.hex("7230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d"), temp);

		// {client} construct a Finished handshake message
		// {client} send handshake record
		clientSecrets.hash(buffer(ClientFinished1));

		// {server} received EndOfEarlyData
		serverSecrets.hash(buffer(EndOfEarlyData));

		// {server} received ClientFinished
		// {server} 验证客户端发送的握手完成验证码
		temp = serverSecrets.v13ClientFinished();
		assertArrayEquals(Utility.hex("7230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d"), temp);
		serverSecrets.hash(Utility.hex(ClientFinished1));

		// {client} send application_data record
		// {server} send application_data record
		// {client} send alert record
		// {server} send alert record
	}

	@Test
	void testCipher() throws Exception {
		// 根据RFC8448验证消息加密与解密

		final CipherSuiter server = new CipherSuiter(CipherSuite.TLS_AES_128_GCM_SHA256);
		final CipherSuiter client = new CipherSuiter(CipherSuite.TLS_AES_128_GCM_SHA256);
		final DataBuffer buffer = DataBuffer.instance();
		final DataBuffer temp = DataBuffer.instance();

		////////////////////////////////////////////////////////////////////////////////
		// Simple 1-RTT Handshake
		////////////////////////////////////////////////////////////////////////////////

		// {server} send handshake record:
		// payload:EncryptedExtentions+Certificate+CertificateVerify+Finished
		final DataBuffer plain1 = DataBuffer.instance();
		plain1.write(Utility.hex(ServerEncryptedExtensions));
		plain1.write(Utility.hex(ServerCertificate));
		plain1.write(Utility.hex(ServerCertificateVerify));
		plain1.write(Utility.hex(ServerFinished));
		plain1.write(Record.HANDSHAKE);
		// encrypt:17030302a2 ...
		final DataBuffer cipher1 = buffer("""
				d1ff334a56f5bf
				f6594a07cc87b580233f500f45e489e7f33af35edf
				7869fcf40aa40aa2b8ea73f848a7ca07612ef9f945
				cb960b4068905123ea78b111b429ba9191cd05d2a3
				89280f526134aadc7fc78c4b729df828b5ecf7b13b
				d9aefb0e57f271585b8ea9bb355c7c79020716cfb9
				b1183ef3ab20e37d57a6b9d7477609aee6e122a4cf
				51427325250c7d0e509289444c9b3a648f1d71035d
				2ed65b0e3cdd0cbae8bf2d0b227812cbb360987255
				cc744110c453baa4fcd610928d809810e4b7ed1a8f
				d991f06aa6248204797e36a6a73b70a2559c09ead6
				86945ba246ab66e5edd8044b4c6de3fcf2a89441ac
				66272fd8fb330ef8190579b3684596c960bd596eea
				520a56a8d650f563aad27409960dca63d3e688611e
				a5e22f4415cf9538d51a200c27034272968a264ed6
				540c84838d89f72c24461aad6d26f59ecaba9acbbb
				317b66d902f4f292a36ac1b639c637ce343117b659
				622245317b49eeda0c6258f100d7d961ffb138647e
				92ea330faeea6dfa31c7a84dc3bd7e1b7a6c7178af
				36879018e3f252107f243d243dc7339d5684c8b037
				8bf30244da8c87c843f5e56eb4c5e8280a2b48052c
				f93b16499a66db7cca71e4599426f7d461e66f9988
				2bd89fc50800becca62d6c74116dbd2972fda1fa80
				f85df881edbe5a37668936b335583b599186dc5c69
				18a396fa48a181d6b6fa4f9d62d513afbb992f2b99
				2f67f8afe67f76913fa388cb5630c8ca01e0c65d11
				c66a1e2ac4c85977b7c7a6999bbf10dc35ae69f551
				5614636c0b9b68c19ed2e31c0b3b66763038ebba42
				f3b38edc0399f3a9f23faa63978c317fc9fa66a73f
				60f0504de93b5b845e275592c12335ee340bbc4fdd
				d502784016e4b3be7ef04dda49f4b440a30cb5d2af
				939828fd4ae3794e44f94df5a631ede42c1719bfda
				bf0253fe5175be898e750edc53370d2b""");

		// RESET KEY
		server.v13EncryptReset(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"));
		client.v13DecryptReset(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"));
		buffer.clear();

		// {server} encrypt
		temp.replicate(plain1);
		server.v13EncryptAEAD(0x02a2);
		server.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher1);

		// {client} decrypt
		client.v13DecryptAEAD(0x02a2);
		client.decryptFinal(buffer);
		assertEquals(buffer, plain1);

		// RESET KEY
		client.v13EncryptReset(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"));
		server.v13DecryptReset(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"));
		buffer.clear();

		// {client} send handshake record
		// payload:Finished 36+1
		final DataBuffer plain2 = DataBuffer.instance();
		plain2.write(Utility.hex(ClientFinished));
		plain2.write(Record.HANDSHAKE);
		// encrypt:1703030035 ...
		final DataBuffer cipher2 = buffer("""
				75ec4dc238cce6
				0b298044a71e219c56cc77b0517fe9b93c7a4bfc44
				d87f38f80338ac98fc46deb384bd1caeacab6867d7
				26c40546""");

		// {client} encrypt
		temp.replicate(plain2);
		client.v13EncryptAEAD(0x0035);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher2);

		// {server} decrypt
		server.v13DecryptAEAD(0x0035);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain2);

		// RESET KEY
		server.v13EncryptReset(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"));
		client.v13DecryptReset(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"));
		buffer.clear();

		// {server} send handshake record:
		// payload:NewSessionTicket
		final DataBuffer plain3 = DataBuffer.instance();
		plain3.write(Utility.hex(ServerNewSessionTicket));
		plain3.write(Record.HANDSHAKE);
		// encrypt:17030300de ...
		final DataBuffer cipher3 = buffer("""
				3a6b8f90414a97
				d6959c3487680de5134a2b240e6cffac116e95d41d
				6af8f6b580dcf3d11d63c758db289a015940252f55
				713e061dc13e078891a38efbcf5753ad8ef170ad3c
				7353d16d9da773b9ca7f2b9fa1b6c0d4a3d03f75e0
				9c30ba1e62972ac46f75f7b981be63439b2999ce13
				064615139891d5e4c5b406f16e3fc181a77ca47584
				0025db2f0a77f81b5ab05b94c01346755f69232c86
				519d86cbeeac87aac347d143f9605d64f650db4d02
				3e70e952ca49fe5137121c74bc2697687e248746d6
				df353005f3bce18696129c8153556b3b6c6779b37b
				f15985684f""");

		// {server} encrypt
		temp.replicate(plain3);
		server.v13EncryptAEAD(0x00de);
		server.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher3);

		// {client} decrypt
		client.v13DecryptAEAD(0x00de);
		client.decryptFinal(buffer);
		assertEquals(buffer, plain3);

		// RESET KEY
		client.v13EncryptReset(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"));
		server.v13DecryptReset(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"));
		buffer.clear();

		// {client} send application_data record
		// payload:50
		final DataBuffer plain4 = buffer("""
				000102030405060708090a0b0c0d0e
				0f101112131415161718191a1b1c1d1e1f20212223
				2425262728292a2b2c2d2e2f3031""");
		plain4.write(Record.APPLICATION_DATA);
		// encrypt:1703030043 ...
		final DataBuffer cipher4 = buffer("""
				a23f7054b62c94
				d0affafe8228ba55cbefacea42f914aa66bcab3f2b
				9819a8a5b46b395bd54a9a20441e2b62974e1f5a62
				92a2977014bd1e3deae63aeebb21694915e4""");

		// {client} encrypt
		temp.replicate(plain4);
		client.v13EncryptAEAD(0x0043);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher4);

		// {server} decrypt
		server.v13DecryptAEAD(0x0043);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain4);

		// {server} send application_data record
		// payload:50
		final DataBuffer plain5 = buffer("""
				000102030405060708090a0b0c0d0e
				0f101112131415161718191a1b1c1d1e1f20212223
				2425262728292a2b2c2d2e2f3031""");
		plain5.write(Record.APPLICATION_DATA);
		// encrypt:1703030043 ...
		final DataBuffer cipher5 = buffer("""
				2e937e11ef4ac7
				40e538ad36005fc4a46932fc3225d05f82aa1b36e3
				0efaf97d90e6dffc602dcb501a59a8fcc49c4bf2e5
				f0a21c0047c2abf332540dd032e167c2955d""");

		buffer.clear();
		// {server} encrypt
		temp.replicate(plain5);
		server.v13EncryptAEAD(0x0043);
		server.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher5);

		// {client} decrypt
		client.v13DecryptAEAD(0x0043);
		client.decryptFinal(buffer);
		assertEquals(buffer, plain5);

		// {client} send alert record
		// payload:2
		final DataBuffer plain6 = buffer("0100");
		plain6.write(Record.ALERT);
		// encrypt:1703030013 ...
		final DataBuffer cipher6 = buffer("c9872760655666b74d7ff1153efd6db6d0b0e3");

		buffer.clear();
		// {client} encrypt
		temp.replicate(plain6);
		client.v13EncryptAEAD(0x0013);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher6);

		// {server} send alert record:
		// payload:2
		final DataBuffer plain7 = buffer("0100");
		plain7.write(Record.ALERT);
		// encrypt:1703030013 ...
		final DataBuffer cipher7 = buffer("b58fd67166ebf599d24720cfbe7efa7a8864a9");

		buffer.clear();
		// {server} encrypt
		temp.replicate(plain7);
		server.v13EncryptAEAD(0x0013);
		server.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher7);

		////////////////////////////////////////////////////////////////////////////////
		// Resumed 0-RTT Handshake
		////////////////////////////////////////////////////////////////////////////////

		// RESET KEY
		client.v13EncryptReset(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"));
		server.v13DecryptReset(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"));

		// {client} send application_data record
		// payload:6
		final DataBuffer plain10 = buffer("414243444546");
		plain10.write(Record.APPLICATION_DATA);
		// encrypt:1703030017 ...
		final DataBuffer cipher10 = buffer("ab1df420e75c457a7cc5d2844f76d5aee4b4edbf049be0");

		buffer.clear();
		// {client} encrypt
		temp.replicate(plain10);
		client.v13EncryptAEAD(0x0017);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher10);

		// {server} decrypt
		server.v13DecryptAEAD(0x0017);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain10);

		// RESET KEY
		server.v13EncryptReset(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"));
		client.v13DecryptReset(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"));

		// {server} send handshake record
		// payload:EncryptedExtensions+Finished
		final DataBuffer plain11 = DataBuffer.instance();
		plain11.write(Utility.hex(EncryptedExtensions1));
		plain11.write(Utility.hex(ServerFinished1));
		plain11.write(Record.HANDSHAKE);
		// encrypt:1703030061 ...
		final DataBuffer cipher11 = buffer("""
				dc48237b4b879f
				50d0d4d262ea8b4716eb40ddc1eb957e11126e8a71
				49c2d012d37a7115957e64ce30008b9e0323f2c05a
				9c1c77b4f37849a695ab255060a33fee770ca95cb8
				486bfd0843b87024865ca35cc41c4e515c64dcb136
				9f98635bc7a5""");

		buffer.clear();
		// {server} encrypt
		temp.replicate(plain11);
		server.v13EncryptAEAD(0x0061);
		server.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher11);

		// {client} decrypt
		client.v13DecryptAEAD(0x0061);
		client.decryptFinal(buffer);
		assertEquals(buffer, plain11);

		// {client} send handshake record
		// payload:EndOfEarlyData
		final DataBuffer plain12 = buffer("05000000");
		plain12.write(Record.HANDSHAKE);
		// encrypt:1703030015 ...
		final DataBuffer cipher12 = buffer("aca6fc944841298df99593725f9bf9754429b12f09");
		// 注意：此消息用 client_early_traffic_secret 密钥

		buffer.clear();
		// {client} encrypt
		temp.replicate(plain12);
		client.v13EncryptAEAD(0x0015);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher12);

		// {server} decrypt
		server.v13DecryptAEAD(0x0015);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain12);

		// RESET KEY
		client.v13EncryptReset(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"));
		server.v13DecryptReset(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"));

		// {client} send handshake record
		// payload:Finished
		final DataBuffer plain13 = buffer("140000207230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d");
		plain13.write(Record.HANDSHAKE);
		// encrypt:1703030035
		final DataBuffer cipher13 = buffer("""
				00f8b467d14cf2
				2a4b3f0b6ae0d8e6cc8d08e0db3515ef5c2bdf1922
				eafbb70009964716d834fb70c3d2a56c5b1f5f6bdb
				a6c333cf""");

		buffer.clear();
		// {client} encrypt
		temp.replicate(plain13);
		client.v13EncryptAEAD(0x0035);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher13);

		// {server} decrypt
		server.v13DecryptAEAD(0x0035);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain13);

		// RESET KEY
		client.v13EncryptReset(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"));
		server.v13DecryptReset(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"));

		server.v13EncryptReset(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"));
		client.v13DecryptReset(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"));

		// {server} derive write traffic keys for application data:
		// PRK(32octets):cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691
		// System.out.println(Utility.hex(temp));

		// {client} derive write traffic keys for application data:
		// PRK(32octets):2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1

		// {client} send application_data record:
		// payload:50
		final DataBuffer plain14 = buffer("""
						000102030405060708090a0b0c0d0e
				0f101112131415161718191a1b1c1d1e1f20212223
				2425262728292a2b2c2d2e2f3031""");
		plain14.write(Record.APPLICATION_DATA);
		// encrypt:1703030043
		final DataBuffer cipher14 = buffer("""
				b1cebce242aa20
				1be9ae5e1cb2a9aa4b33d4e866af1edb0689192377
				41aa031d7a74d491c99b9d4e232b74206bc6fbaa04
				fe78be44a9b4f54320a17eb76992afac3103""");
		buffer.clear();

		// {client} encrypt
		temp.replicate(plain14);
		client.v13EncryptAEAD(0x0043);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher14);

		// {server} decrypt
		server.v13DecryptAEAD(0x0043);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain14);

		// {server} send application_data record:
		// payload:50
		final DataBuffer plain15 = buffer("""
				000102030405060708090a0b0c0d0e
				0f101112131415161718191a1b1c1d1e1f20212223
				2425262728292a2b2c2d2e2f3031""");
		plain15.write(Record.APPLICATION_DATA);
		// encrypt:1703030043
		final DataBuffer cipher15 = buffer("""
				275e9f20acff57
				bc000657d3867df039cccf79047884cf75771746f7
				40b5a83f462a0954c3581393a203a25a7dd14141ef
				1a37900cdb62ff62dee1ba39ab2590cbf194""");
		buffer.clear();

		// {server} encrypt
		temp.replicate(plain15);
		server.v13EncryptAEAD(0x0043);
		server.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher15);

		// {client} decrypt
		client.v13DecryptAEAD(0x0043);
		client.decryptFinal(buffer);
		assertEquals(buffer, plain15);

		// {client} send alert record
		// payload:2
		final DataBuffer plain16 = buffer("0100");
		plain16.write(Record.ALERT);
		// encrypt:1703030013
		final DataBuffer cipher16 = buffer("""
				0facce3246bdfc
				6369838d6a82ae6de5d422dc""");
		buffer.clear();

		// {client} encrypt
		temp.replicate(plain16);
		client.v13EncryptAEAD(0x0013);
		client.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher16);

		// {server} decrypt
		server.v13DecryptAEAD(0x0013);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain16);

		// {server} send alert record:
		// payload:2
		final DataBuffer plain17 = buffer("0100");
		plain17.write(Record.ALERT);
		// encrypt:1703030013
		final DataBuffer cipher17 = buffer("""
				5b18af444e8e1e
				ec7158fb62d8f2577d37ba5d""");
		buffer.clear();

		// {server} encrypt
		temp.replicate(plain17);
		server.v13EncryptAEAD(0x0013);
		server.encryptFinal(temp, buffer);
		assertEquals(buffer, cipher17);

		// {client} decrypt
		client.v13DecryptAEAD(0x0013);
		client.decryptFinal(buffer);
		assertEquals(buffer, plain17);

		// 额外的测试

		// 额外的少量测试
		final DataBuffer plain = DataBuffer.instance();
		plain.write(Utility.hex(ClientFinished));
		buffer.clear();
		client.v13EncryptAEAD(plain.readable() + 16);
		temp.replicate(plain);
		client.encryptFinal(temp, buffer);

		server.v13DecryptAEAD(buffer.readable());
		server.decryptFinal(buffer);
		assertEquals(buffer, plain);

		// 额外的满载测试
		plain.clear();
		for (int i = 0; i < 16; i++) {
			for (int v = 0; v < 1024; v++) {
				plain.write(v);
			}
		}

		buffer.clear();
		temp.replicate(plain);
		client.v13EncryptAEAD(16 * 1024);
		client.encryptFinal(temp, buffer);
		System.out.println(buffer);
		server.v13DecryptAEAD(16 * 1024);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain);

		buffer.clear();
		temp.replicate(plain);
		client.v13EncryptAEAD(16 * 1024);
		client.encryptFinal(temp, buffer);
		System.out.println(buffer);
		server.v13DecryptAEAD(16 * 1024);
		server.decryptFinal(buffer);
		assertEquals(buffer, plain);
	}

	@Test
	void testSignature() throws Exception {
		// Certificate (445 octets):
		// 0b -------HandshakeType 1Byte
		// 00 01 b9 -Length 3Byte(uint24) 441
		// 00 -------context 0
		// 00 01 b5 -Length 437
		// 00 -------X509
		// 01 b0 ----Length 432 cert_data
		// 30 82
		// 01 ac 30 82 01 15 a0 03 02 01 02 02 01 02 30 0d 06 09 2a 86 48
		// 86 f7 0d 01 01 0b 05 00 30 0e 31 0c 30 0a 06 03 55 04 03 13 03
		// 72 73 61 30 1e 17 0d 31 36 30 37 33 30 30 31 32 33 35 39 5a 17
		// 0d 32 36 30 37 33 30 30 31 32 33 35 39 5a 30 0e 31 0c 30 0a 06
		// 03 55 04 03 13 03 72 73 61 30 81 9f 30 0d 06 09 2a 86 48 86 f7
		// 0d 01 01 01 05 00 03 81 8d 00 30 81 89 02 81 81 00 b4 bb 49 8f
		// 82 79 30 3d 98 08 36 39 9b 36 c6 98 8c 0c 68 de 55 e1 bd b8 26
		// d3 90 1a 24 61 ea fd 2d e4 9a 91 d0 15 ab bc 9a 95 13 7a ce 6c
		// 1a f1 9e aa 6a f9 8c 7c ed 43 12 09 98 e1 87 a8 0e e0 cc b0 52
		// 4b 1b 01 8c 3e 0b 63 26 4d 44 9a 6d 38 e2 2a 5f da 43 08 46 74
		// 80 30 53 0e f0 46 1c 8c a9 d9 ef bf ae 8e a6 d1 d0 3e 2b d1 93
		// ef f0 ab 9a 80 02 c4 74 28 a6 d3 5a 8d 88 d7 9f 7f 1e 3f 02 03
		// 01 00 01 a3 1a 30 18 30 09 06 03 55 1d 13 04 02 30 00 30 0b 06
		// 03 55 1d 0f 04 04 03 02 05 a0 30 0d 06 09 2a 86 48 86 f7 0d 01
		// 01 0b 05 00 03 81 81 00 85 aa d2 a0 e5 b9 27 6b 90 8c 65 f7 3a
		// 72 67 17 06 18 a5 4c 5f 8a 7b 33 7d 2d f7 a5 94 36 54 17 f2 ea
		// e8 f8 a5 8c 8f 81 72 f9 31 9c f3 6b 7f d6 c5 5b 80 f2 1a 03 01
		// 51 56 72 60 96 fd 33 5e 5e 67 f2 db f1 02 70 2e 60 8c ca e6 be
		// c1 fc 63 a4 2a 99 be 5c 3e b7 10 7c 3c 54 e9 b9 eb 2b d5 20 3b
		// 1c 3b 84 e0 a8 b2 f7 59 40 9b a3 ea c9 d9 1d 40 2d cc 0c c8 f8
		// 96 12 29 ac 91 87 b4 2b 4d e1
		// 00 00-----Extensions

		// CertificateVerify (136 octets):
		// 0f HandshakeType 1Byte
		// 00 00 84 Length 3Byte(uint24) 132
		// 08 04 Algorithm 2Byte
		// 00 80 Length 2Byte 128
		// 5a 74 7c
		// 5d 88 fa 9b d2 e5 5a b0 85 a6 10 15 b7 21 1f 82 4c d4 84 14 5a
		// b3 ff 52 f1 fd a8 47 7b 0b 7a bc 90 db 78 e2 d3 3a 5c 14 1a 07
		// 86 53 fa 6b ef 78 0c 5e a2 48 ee aa a7 85 c4 f3 94 ca b6 d3 0b
		// be 8d 48 59 ee 51 1f 60 29 57 b1 54 11 ac 02 76 71 45 9e 46 44
		// 5c 9e a5 8c 18 1e 81 8e 95 b8 c3 fb 0b f3 27 84 09 d3 be 15 2a
		// 3d a5 04 3e 06 3d da 65 cd f5 ae a2 0d 53 df ac d4 2f 74 f3

		final String modulus = "b4bb498f8279303d980836399b36c6988c0c68de55e1bdb826d3901a2461eafd2de49a91d015abbc9a95137ace6c1af19eaa6af98c7ced43120998e187a80ee0ccb0524b1b018c3e0b63264d449a6d38e22a5fda430846748030530ef0461c8ca9d9efbfae8ea6d1d03e2bd193eff0ab9a8002c47428a6d35a8d88d79f7f1e3f";
		final String privateExponent = "04dea705d43a6ea7209dd8072111a83c81e322a59278b33480641eaf7c0a6985b8e31c44f6de62e1b4c2309f6126e77b7c41e923314bbfa3881305dc1217f16c819ce538e922f369828d0e57195d8c8488460207b2faa726bcf708bbd7db7f679f893492fc2a622e08970aac441ce4e0c3088df25ae679233df8a3bda2ff9941";
		final String publicExponent = "010001";
		final String prime1 = "e435fb7cc83737756dacea96ab7f59a2cc1069db7deb190e17e33a532b273f30a327aa0aaabc58cd67466af9845fadc675fe094af92c4bd1f2c1bc33dd2e0515";
		final String prime2 = "cabd3bc0e0438664c8d4cc9f99977a94d9bbfead8e43870abae3f7eb8b4e0eee8af1d9b4719ba6196cf2cbbaeeebf8b3490afe9e9ffa74a88aa51fc645629303";
		final String exponent1 = "3f57345c27fe1b687e6e761627b78b1b826433dd760fa0bea6a6acf39490aa1b47cda4869d68f584dd5b5029bd32093b8258661fe715025e5d70a45a08d3d319";
		final String exponent2 = "183da01363bd2f2885cacbdc9964bf4764f1517636f86401286f71893c52ccfe40a6c23d0d086b47c6fb10d8fd1041e04def7e9a40ce957c417794e10412d139";
		final String coefficient = "839ca9a085e4286b2c90e466997a2c681f21339aa3477814e4dec11833050ed50dd13cc038048a43c59b2acc416889c037665fe5afa605969f8c01dfa5ca969d";

		final DataBuffer cert = buffer("""
				3082
				01ac30820115a003020102020102300d06092a8648
				86f70d01010b0500300e310c300a06035504031303
				727361301e170d3136303733303031323335395a17
				0d3236303733303031323335395a300e310c300a06
				03550403130372736130819f300d06092a864886f7
				0d010101050003818d0030818902818100b4bb498f
				8279303d980836399b36c6988c0c68de55e1bdb826
				d3901a2461eafd2de49a91d015abbc9a95137ace6c
				1af19eaa6af98c7ced43120998e187a80ee0ccb052
				4b1b018c3e0b63264d449a6d38e22a5fda43084674
				8030530ef0461c8ca9d9efbfae8ea6d1d03e2bd193
				eff0ab9a8002c47428a6d35a8d88d79f7f1e3f0203
				010001a31a301830090603551d1304023000300b06
				03551d0f0404030205a0300d06092a864886f70d01
				010b05000381810085aad2a0e5b9276b908c65f73a
				7267170618a54c5f8a7b337d2df7a594365417f2ea
				e8f8a58c8f8172f9319cf36b7fd6c55b80f21a0301
				5156726096fd335e5e67f2dbf102702e608ccae6be
				c1fc63a42a99be5c3eb7107c3c54e9b9eb2bd5203b
				1c3b84e0a8b2f759409ba3eac9d91d402dcc0cc8f8
				961229ac9187b42b4de1
						""");

		final DataBufferInput inpupt = new DataBufferInput(cert);
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		Certificate certificate = certificateFactory.generateCertificate(inpupt);
		System.out.println(certificate);

		// Server

		final RSAPrivateCrtKeySpec privateKeySpec = new RSAPrivateCrtKeySpec(//
			new BigInteger(modulus, 16), //
			new BigInteger(publicExponent, 16), //
			new BigInteger(privateExponent, 16), //
			new BigInteger(prime1, 16), //
			new BigInteger(prime2, 16), //
			new BigInteger(exponent1, 16), //
			new BigInteger(exponent2, 16), //
			new BigInteger(coefficient, 16));
		final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(//
			new BigInteger(modulus, 16), //
			new BigInteger(publicExponent, 16));

		final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		final PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		final PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

		// Transcript-Hash(Handshake Context, Certificate)
		final TranscriptHash transcriptHash = new TranscriptHash();
		transcriptHash.digest("SHA-256");
		transcriptHash.hash(Utility.hex(ClientHello));
		transcriptHash.hash(Utility.hex(ServerHello));
		transcriptHash.hash(Utility.hex(ServerEncryptedExtensions));
		transcriptHash.hash(Utility.hex(ServerCertificate));
		// System.out.println(Utility.hex(transcriptHash.hash()));

		// RSA_PSS_RSAE_SHA256(0x0804)
		final Signaturer signaturer = new Signaturer();
		signaturer.scheme(SignatureScheme.RSA_PSS_RSAE_SHA256);
		// 生成签名
		final byte[] sign = signaturer.singServer(privateKey, transcriptHash.hash());
		// System.out.println(Utility.hex(sign));
		// 验证签名
		assertEquals(signaturer.verifyServer(publicKey, transcriptHash.hash(), sign), true);
	}

	@Test
	void testSignatureSchemes() {
		final Signaturer signaturer = new Signaturer();
		for (short scheme : SignatureScheme.ALL) {
			try {
				signaturer.scheme(scheme);
			} catch (Exception e) {
				System.out.println(SignatureScheme.name(scheme) + ":" + e.getMessage());
			}
		}
	}

	@Test
	void testCipherSuiters() throws Exception {
		final CipherSuiter cipherSuiter = new CipherSuiter();
		for (short suite : CipherSuite.ALL) {
			try {
				cipherSuiter.suite(suite);
			} catch (Exception e) {
				System.out.println(CipherSuite.name(suite) + ":" + e.getMessage());
			}
		}
	}

	/**
	 * 多行字符串转换为字节数据
	 */
	DataBuffer buffer(String data) throws IOException {
		data = data.replaceAll("\\s*", "");
		final DataBuffer buffer = DataBuffer.instance();
		buffer.write(Utility.hex(data));
		return buffer;
	}

	/**
	 * 多行字符串转换为字节数据
	 */
	byte[] bytes(String data) {
		data = data.replaceAll("\\s*", "");
		return Utility.hex(data);
	}
}