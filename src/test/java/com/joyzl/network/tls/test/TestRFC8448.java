package com.joyzl.network.tls.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;
import com.joyzl.network.tls.CipherSuite;
import com.joyzl.network.tls.ClientSecrets;
import com.joyzl.network.tls.NamedGroup;
import com.joyzl.network.tls.DeriveSecret;
import com.joyzl.network.tls.ServerSecrets;
import com.joyzl.network.tls.TLSPlaintext;

class TestRFC8448 {

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
	// 1-RTT Messages
	final String ClientHello = "010000c00303cb34ecb1e78163ba1c38c6dacb196a6dffa21a8d9912ec18a2ef6283024dece7000006130113031302010000910000000b0009000006736572766572ff01000100000a00140012001d0017001800190100010101020103010400230000003300260024001d002099381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c002b0003020304000d0020001e040305030603020308040805080604010501060102010402050206020202002d00020101001c00024001";
	final String ServerHello = "020000560303a6af06a4121860dc5e6e60249cd34c95930c8ac5cb1434dac155772ed3e2692800130100002e00330024001d0020c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f002b00020304";
	final String ServerEncryptedExtensions = "080000240022000a00140012001d00170018001901000101010201030104001c0002400100000000";
	final String ServerCertificate = "0b0001b9000001b50001b0308201ac30820115a003020102020102300d06092a864886f70d01010b0500300e310c300a06035504031303727361301e170d3136303733303031323335395a170d3236303733303031323335395a300e310c300a0603550403130372736130819f300d06092a864886f70d010101050003818d0030818902818100b4bb498f8279303d980836399b36c6988c0c68de55e1bdb826d3901a2461eafd2de49a91d015abbc9a95137ace6c1af19eaa6af98c7ced43120998e187a80ee0ccb0524b1b018c3e0b63264d449a6d38e22a5fda430846748030530ef0461c8ca9d9efbfae8ea6d1d03e2bd193eff0ab9a8002c47428a6d35a8d88d79f7f1e3f0203010001a31a301830090603551d1304023000300b0603551d0f0404030205a0300d06092a864886f70d01010b05000381810085aad2a0e5b9276b908c65f73a7267170618a54c5f8a7b337d2df7a594365417f2eae8f8a58c8f8172f9319cf36b7fd6c55b80f21a03015156726096fd335e5e67f2dbf102702e608ccae6bec1fc63a42a99be5c3eb7107c3c54e9b9eb2bd5203b1c3b84e0a8b2f759409ba3eac9d91d402dcc0cc8f8961229ac9187b42b4de10000";
	final String ServerCertificateVerify = "0f000084080400805a747c5d88fa9bd2e55ab085a61015b7211f824cd484145ab3ff52f1fda8477b0b7abc90db78e2d33a5c141a078653fa6bef780c5ea248eeaaa785c4f394cab6d30bbe8d4859ee511f602957b15411ac027671459e46445c9ea58c181e818e95b8c3fb0bf3278409d3be152a3da5043e063dda65cdf5aea20d53dfacd42f74f3";
	final String ServerFinished = "140000209b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718";
	final String ClientFinished = "14000020a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61";
	final String ServerNewSessionTicket = "040000c90000001efad6aac502000000b22c035d829359ee5ff7af4ec900000000262a6494dc486d2c8a34cb33fa90bf1b0070ad3c498883c9367c09a2be785abc55cd226097a3a982117283f82a03a143efd3ff5dd36d64e861be7fd61d2827db279cce145077d454a3664d4e6da4d29ee03725a6a4dafcd0fc67d2aea70529513e3da2677fa5906c5b3f7d8f92f228bda40dda721470f9fbf297b5aea617646fac5c03272e970727c621a79141ef5f7de6505e5bfbc388e93343694093934ae4d3570008002a000400000400";
	// 0-RTT Messages
	final String ClientHelloPrefix = "010001fc03031bc3ceb6bbe39cff938355b5a50adb6db21b7a6af649d7b4bc419d7876487d95000006130113031302010001cd0000000b0009000006736572766572ff01000100000a00140012001d00170018001901000101010201030104003300260024001d0020e4ffb68ac05f8d96c99da26698346c6be16482badddafe051a66b4f18d668f0b002a0000002b0003020304000d0020001e040305030603020308040805080604010501060102010402050206020202002d00020101001c0002400100150057000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002900dd00b800b22c035d829359ee5ff7af4ec900000000262a6494dc486d2c8a34cb33fa90bf1b0070ad3c498883c9367c09a2be785abc55cd226097a3a982117283f82a03a143efd3ff5dd36d64e861be7fd61d2827db279cce145077d454a3664d4e6da4d29ee03725a6a4dafcd0fc67d2aea70529513e3da2677fa5906c5b3f7d8f92f228bda40dda721470f9fbf297b5aea617646fac5c03272e970727c621a79141ef5f7de6505e5bfbc388e93343694093934ae4d357fad6aacb";
	final String ServerHello1 = "0200005c03033ccfd2dec890222763472ae8136777c9d7358777bb66e91ea5122495f559ea2d00130100003400290002000000330024001d0020121761ee42c333e1b9e77b60dd57c2053cd94512ab47f115e86eff50942cea31002b00020304";
	final String EncryptedExtensions1 = "080000280026000a00140012001d00170018001901000101010201030104001c0002400100000000002a0000";
	final String ServerFinished1 = "1400002048d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2";
	final String EndOfEarlyData = "05000000";
	final String ClientFinished1 = "140000207230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d";

	@Test
	void testDeriveSecrets() throws Exception {
		// 根据RFC8448验证密钥推导计划和HKDF

		final DeriveSecret client = new DeriveSecret(CipherSuite.TLS_AES_128_GCM_SHA256);
		final DeriveSecret server = new DeriveSecret(CipherSuite.TLS_AES_128_GCM_SHA256);
		client.initialize(NamedGroup.X25519);
		server.initialize(NamedGroup.X25519);

		// 1-RTT Handshake ////////////////////////////////////////

		// {client} create an ephemeral x25519 key pair
		client.generate();
		client.setPrivateKey(Utility.hex("49af42ba7f7994852d713ef2784bcbcaa7911de26adc5642cb634540e7ea5005"));
		client.setPublicKey(Utility.hex("99381de560e4bd43d23d8e435a7dbafeb3c06e51c13cae4d5413691e529aaf2c"));

		// {client} construct a ClientHello handshake message
		// {client} send handshake record (TLSPlaintext)

		// {server} extract secret "early"
		// salt: 0(all zero octets),IKM: 0(32)
		byte[] early = server.early(null);
		assertArrayEquals(Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), early);

		// {server} create an ephemeral x25519 key pair
		server.generate();
		server.setPrivateKey(Utility.hex("b1580eeadf6dd589b8ef4f2d5652578cc810e9980191ec8d058308cea216a21e"));
		server.setPublicKey(Utility.hex("c9828876112095fe66762bdbf7c672e156d6cc253b833df1dd69b1b04e751f0f"));

		// {server} construct a ServerHello handshake message

		// {server} derive secret for handshake "tls13 derived"
		byte[] derived = server.derive(early);
		assertArrayEquals(Utility.hex("6f2615a108c702c5678f54fc9dbab69716c076189c48250cebeac3576c3611ba"), derived);

		// {server} extract secret "handshake":
		byte[] sharedECDHKey = server.sharedKey(client.publicKey());
		assertArrayEquals(Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"), sharedECDHKey);
		byte[] handshake = server.handshake(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("1dc826e93606aa6fdc0aadc12f741b01046aa6b99f691ed221a9f0ca043fbeac"), handshake);

		// MESSAGE HASH
		server.hash(Utility.hex(ClientHello));
		server.hash(Utility.hex(ServerHello));

		// {server} derive secret "tls13 c hs traffic":
		byte[] sClientHandshakeTraffic = server.clientHandshakeTraffic(handshake, server.hash());
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), sClientHandshakeTraffic);

		// {server} derive secret "tls13 s hs traffic":
		byte[] sServerHandshakeTraffic = server.serverHandshakeTraffic(handshake, server.hash());
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), sServerHandshakeTraffic);

		// {server} derive secret for master "tls13 derived":
		derived = server.derive(handshake);
		assertArrayEquals(Utility.hex("43de77e0c77713859a944db9db2590b53190a65b3ee2e4f12dd7a0bb7ce254b4"), derived);

		// {server} extract secret "master":
		byte[] sMaster = server.master(handshake);
		assertArrayEquals(Utility.hex("18df06843d13a08bf2a449844c5f8a478001bc4d4c627984d5a41da8d0402919"), sMaster);

		// {server} send handshake record (TLSCiphertext)

		// {server} derive write traffic keys for handshake data:
		byte[] key = server.key(sServerHandshakeTraffic);
		byte[] iv = server.iv(sServerHandshakeTraffic);
		assertArrayEquals(Utility.hex("3fce516009c21727d0f2e4e86ee403bc"), key);
		assertArrayEquals(Utility.hex("5d313eb2671276ee13000b30"), iv);

		// {server} construct an EncryptedExtensions handshake message:
		// {server} construct a Certificate handshake message:
		// {server} construct a CertificateVerify handshake message:
		server.hash(Utility.hex(ServerEncryptedExtensions));
		server.hash(Utility.hex(ServerCertificate));
		server.hash(Utility.hex(ServerCertificateVerify));

		// {server} calculate finished "tls13 finished":
		byte[] finished = server.finishedVerifyData(sServerHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), finished);

		// {server} construct a Finished handshake message:
		server.hash(Utility.hex(ServerFinished));

		// {server} send handshake record (TLSCiphertext)

		// {server} derive secret "tls13 c ap traffic":
		byte[] sClientApplicationTraffic = server.clientApplicationTraffic(sMaster, server.hash());
		assertArrayEquals(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"), sClientApplicationTraffic);

		// {server} derive secret "tls13 s ap traffic":
		byte[] sServerApplicationTraffic = server.serverApplicationTraffic(sMaster, server.hash());
		assertArrayEquals(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"), sServerApplicationTraffic);

		// {server} derive secret "tls13 exp master":
		byte[] exporter = server.exporterMaster(sMaster, server.hash());
		assertArrayEquals(Utility.hex("fe22f881176eda18eb8f44529e6792c50c9a3f89452f68d8ae311b4309d3cf50"), exporter);

		// {server} derive write traffic keys for application data:
		key = server.key(sServerApplicationTraffic);
		iv = server.iv(sServerApplicationTraffic);
		assertArrayEquals(Utility.hex("9f02283b6c9c07efc26bb9f2ac92e356"), key);
		assertArrayEquals(Utility.hex("cf782b88dd83549aadf1e984"), iv);

		// {server} derive read traffic keys for handshake data:
		key = server.key(sClientHandshakeTraffic);
		iv = server.iv(sClientHandshakeTraffic);
		assertArrayEquals(Utility.hex("dbfaa693d1762c5b666af5d950258d01"), key);
		assertArrayEquals(Utility.hex("5bd3c71b836e0b76bb73265f"), iv);

		// {client} extract secret "early"
		// (same as server early secret)
		early = client.early(null);
		assertArrayEquals(Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), early);

		// {client} derive secret for handshake "tls13 derived"
		derived = client.derive(early);
		assertArrayEquals(Utility.hex("6f2615a108c702c5678f54fc9dbab69716c076189c48250cebeac3576c3611ba"), derived);

		// {client} extract secret "handshake"
		// (same as server handshake secret)
		sharedECDHKey = client.sharedKey(server.publicKey());
		assertArrayEquals(Utility.hex("8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d"), sharedECDHKey);
		handshake = client.handshake(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("1dc826e93606aa6fdc0aadc12f741b01046aa6b99f691ed221a9f0ca043fbeac"), handshake);

		client.hash(Utility.hex(ClientHello));
		client.hash(Utility.hex(ServerHello));

		// {client} derive secret "tls13 c hs traffic"
		// (same as server)
		byte[] cClientHandshakeTraffic = client.clientHandshakeTraffic(handshake, client.hash());
		assertArrayEquals(Utility.hex("b3eddb126e067f35a780b3abf45e2d8f3b1a950738f52e9600746a0e27a55a21"), cClientHandshakeTraffic);

		// {client} derive secret "tls13 s hs traffic"
		// (same as server)
		byte[] cServerHandshakeTraffic = client.serverHandshakeTraffic(handshake, client.hash());
		assertArrayEquals(Utility.hex("b67b7d690cc16c4e75e54213cb2d37b4e9c912bcded9105d42befd59d391ad38"), cServerHandshakeTraffic);

		// {client} derive secret for master "tls13 derived"
		// (same as server)
		derived = client.derive(handshake);
		assertArrayEquals(Utility.hex("43de77e0c77713859a944db9db2590b53190a65b3ee2e4f12dd7a0bb7ce254b4"), derived);

		// {client} extract secret "master"
		// (same as server master secret)
		byte[] cMaster = client.master(handshake);
		assertArrayEquals(Utility.hex("18df06843d13a08bf2a449844c5f8a478001bc4d4c627984d5a41da8d0402919"), cMaster);

		// {client} derive read traffic keys for handshake data
		// (same as server handshake data write traffic keys)
		key = client.key(cServerHandshakeTraffic);
		iv = client.iv(cServerHandshakeTraffic);
		assertArrayEquals(Utility.hex("3fce516009c21727d0f2e4e86ee403bc"), key);
		assertArrayEquals(Utility.hex("5d313eb2671276ee13000b30"), iv);

		client.hash(Utility.hex(ServerEncryptedExtensions));
		client.hash(Utility.hex(ServerCertificate));
		client.hash(Utility.hex(ServerCertificateVerify));

		// {client} calculate finished "tls13 finished"
		// (same as server)
		finished = client.finishedVerifyData(cServerHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("9b9b141d906337fbd2cbdce71df4deda4ab42c309572cb7fffee5454b78f0718"), finished);

		// {client} derive secret "tls13 c ap traffic"
		// (same as server)
		byte[] cClientApplicationTraffic = client.clientApplicationTraffic(cMaster, server.hash());
		assertArrayEquals(Utility.hex("9e40646ce79a7f9dc05af8889bce6552875afa0b06df0087f792ebb7c17504a5"), cClientApplicationTraffic);

		// {client} derive secret "tls13 s ap traffic"
		// (same as server)
		byte[] cServerApplicationTraffic = client.serverApplicationTraffic(cMaster, server.hash());
		assertArrayEquals(Utility.hex("a11af9f05531f856ad47116b45a950328204b4f44bfb6b3a4b4f1f3fcb631643"), cServerApplicationTraffic);

		client.hash(Utility.hex(ServerFinished));

		// {client} derive secret "tls13 exp master" (same as server)
		exporter = client.exporterMaster(cMaster, client.hash());
		assertArrayEquals(Utility.hex("fe22f881176eda18eb8f44529e6792c50c9a3f89452f68d8ae311b4309d3cf50"), exporter);

		// {client} derive write traffic keys for handshake data
		// (same as server handshake data read traffic keys)
		key = client.key(cClientHandshakeTraffic);
		iv = client.iv(cClientHandshakeTraffic);
		assertArrayEquals(Utility.hex("dbfaa693d1762c5b666af5d950258d01"), key);
		assertArrayEquals(Utility.hex("5bd3c71b836e0b76bb73265f"), iv);

		// {client} derive read traffic keys for application data
		// (same as server application data write traffic keys)
		key = client.key(cServerApplicationTraffic);
		iv = client.iv(cServerApplicationTraffic);
		assertArrayEquals(Utility.hex("9f02283b6c9c07efc26bb9f2ac92e356"), key);
		assertArrayEquals(Utility.hex("cf782b88dd83549aadf1e984"), iv);

		// {client} calculate finished "tls13 finished":
		finished = client.finishedVerifyData(cClientHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), finished);

		// {client} construct a Finished handshake message
		// {client} send handshake record (TLSCiphertext)
		client.hash(Utility.hex(ClientFinished));

		// {client} derive write traffic keys for application data
		key = client.key(cClientApplicationTraffic);
		iv = client.iv(cClientApplicationTraffic);
		assertArrayEquals(Utility.hex("17422dda596ed5d9acd890e3c63f5051"), key);
		assertArrayEquals(Utility.hex("5b78923dee08579033e523d9"), iv);

		// {client} derive secret "tls13 res master"
		byte[] cResumption = client.resumptionMaster(cMaster, client.hash());
		assertArrayEquals(Utility.hex("7df235f2031d2a051287d02b0241b0bfdaf86cc856231f2d5aba46c434ec196c"), cResumption);

		// {server} calculate finished "tls13 finished" (same as client)
		finished = server.finishedVerifyData(sClientHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("a8ec436d677634ae525ac1fcebe11a039ec17694fac6e98527b642f2edd5ce61"), finished);

		// {server} derive read traffic keys for application data
		// (same as client application data write traffic keys)
		key = client.key(sClientApplicationTraffic);
		iv = client.iv(sClientApplicationTraffic);
		assertArrayEquals(Utility.hex("17422dda596ed5d9acd890e3c63f5051"), key);
		assertArrayEquals(Utility.hex("5b78923dee08579033e523d9"), iv);

		server.hash(Utility.hex(ClientFinished));

		// {server} derive secret "tls13 res master"
		// (same as client)
		byte[] sResumption = server.resumptionMaster(sMaster, server.hash());
		assertArrayEquals(Utility.hex("7df235f2031d2a051287d02b0241b0bfdaf86cc856231f2d5aba46c434ec196c"), sResumption);

		// {server} generate resumption secret "tls13 resumption"
		sResumption = server.resumption(sResumption, new byte[] { 0, 0 });
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), sResumption);

		// {server} construct a NewSessionTicket handshake message
		// {server} send handshake record

		// {client} generate resumption secret "tls13 resumption"
		// (same as server)
		cResumption = client.resumption(cResumption, new byte[] { 0, 0 });
		assertArrayEquals(Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"), cResumption);

		// Resumed 0-RTT Handshake ////////////////////////////////////////

		// {client} create an ephemeral x25519 key pair
		client.setPrivateKey(Utility.hex("bff91188283846dd6a2134ef7180ca2b0b14fb10dce707b5098c0dddc813b2df"));
		client.setPublicKey(Utility.hex("e4ffb68ac05f8d96c99da26698346c6be16482badddafe051a66b4f18d668f0b"));

		// {client} extract secret "early"
		early = client.early(cResumption);
		assertArrayEquals(Utility.hex("9b2188e9b2fc6d64d71dc329900e20bb41915000f678aa839cbb797cb7d8332c"), early);

		// {client} construct a ClientHello handshake message

		// {client} calculate PSK binder:
		// PskBinderEntry 的计算方法和 Finished 消息一样
		client.hashReset();
		client.hash(Utility.hex(ClientHelloPrefix));
		// binder hash
		assertArrayEquals(Utility.hex("63224b2e4573f2d3454ca84b9d009a04f6be9e05711a8396473aefa01e924a14"), client.hash());
		// PRK "tls13 res binder"
		byte[] PRK = client.resumptionBinderKey(early);
		assertArrayEquals(Utility.hex("69fe131a3bbad5d63c64eebcc30e395b9d8107726a13d074e389dbc8a4e47256"), PRK);
		// expanded(PRK,info="tls13 finished")
		byte[] expanded = client.expandLabel(PRK, "finished".getBytes(StandardCharsets.US_ASCII), null, client.hashLength());
		assertArrayEquals(Utility.hex("5588673e72cb59c87d220caffe94f2dea9a3b1609f7d50e90a48227db9ed7eaa"), expanded);
		// finished(resumptionBinderKey,hash(ClientHelloPrefix))
		finished = client.finishedVerifyData(PRK, client.hash());
		assertArrayEquals(Utility.hex("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"), finished);

		// {client} send handshake record (TLSPlaintext)
		// payload:ClientHelloPrefix+002120+3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d
		// 可能是补齐PreSharedKey扩展，需要进一步分析

		// 补齐消息哈希
		client.hash(Utility.hex("0021203add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"));

		// {client} derive secret "tls13 c e traffic"
		byte[] clientEarlyTraffic = client.clientEarlyTraffic(early, client.hash());
		assertArrayEquals(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"), clientEarlyTraffic);

		// {client} derive secret "tls13 e exp master"
		byte[] earlyExporterMaster = client.earlyExporterMaster(early, client.hash());
		assertArrayEquals(Utility.hex("b2026866610937d7423e5be90862ccf24c0e6091186d34f812089ff5be2ef7df"), earlyExporterMaster);

		// {client} derive write traffic keys for early application data
		key = client.key(clientEarlyTraffic);
		iv = client.iv(clientEarlyTraffic);
		assertArrayEquals(Utility.hex("920205a5b7bf2115e6fc5c2942834f54"), key);
		assertArrayEquals(Utility.hex("6d475f0993c8e564610db2b9"), iv);

		// {client} send application_data record

		// {server} extract secret "early"
		// (same as client early secret)
		early = server.early(sResumption);
		assertArrayEquals(Utility.hex("9b2188e9b2fc6d64d71dc329900e20bb41915000f678aa839cbb797cb7d8332c"), early);

		// {server} calculate PSK binder
		// (same as client)
		server.hashReset();
		server.hash(Utility.hex(ClientHelloPrefix));
		PRK = server.resumptionBinderKey(early);
		finished = server.finishedVerifyData(PRK, server.hash());
		assertArrayEquals(Utility.hex("3add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"), finished);

		// {server} create an ephemeral x25519 key pair
		server.setPrivateKey(Utility.hex("de5b4476e7b490b2652d338acbf2948066f255f9440e23b98fc69835298dc107"));
		server.setPublicKey(Utility.hex("121761ee42c333e1b9e77b60dd57c2053cd94512ab47f115e86eff50942cea31"));

		// 补齐消息哈希(ClientHello)
		server.hash(Utility.hex("0021203add4fb2d8fdf822a0ca3cf7678ef5e88dae990141c5924d57bb6fa31b9e5f9d"));

		// {server} derive secret "tls13 c e traffic" (same as client)
		clientEarlyTraffic = server.clientEarlyTraffic(early, server.hash());
		assertArrayEquals(Utility.hex("3fbbe6a60deb66c30a32795aba0eff7eaa10105586e7be5c09678d63b6caab62"), clientEarlyTraffic);

		// {server} derive secret "tls13 e exp master" (same as client)
		earlyExporterMaster = server.earlyExporterMaster(early, server.hash());
		assertArrayEquals(Utility.hex("b2026866610937d7423e5be90862ccf24c0e6091186d34f812089ff5be2ef7df"), earlyExporterMaster);

		// {server} construct a ServerHello handshake message
		server.hash(Utility.hex(ServerHello1));

		// {server} derive secret for handshake "tls13 derived"
		derived = server.derive(early);
		assertArrayEquals(Utility.hex("5f1790bbd82c5e7d376ed2e1e52f8e6038c9346db61b43be9a52f77ef3998e80"), derived);

		// {server} extract secret "handshake"
		sharedECDHKey = server.sharedKey(client.publicKey());
		assertArrayEquals(Utility.hex("f44194756ff9ec9d25180635d66ea6824c6ab3bf179977be37f723570e7ccb2e"), sharedECDHKey);
		handshake = server.handshake(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("005cb112fd8eb4ccc623bb88a07c64b3ede1605363fc7d0df8c7ce4ff0fb4ae6"), handshake);

		// {server} derive secret "tls13 c hs traffic"
		sClientHandshakeTraffic = server.clientHandshakeTraffic(handshake, server.hash());
		assertArrayEquals(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"), sClientHandshakeTraffic);

		// {server} derive secret "tls13 s hs traffic"
		sServerHandshakeTraffic = server.serverHandshakeTraffic(handshake, server.hash());
		assertArrayEquals(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"), sServerHandshakeTraffic);

		// {server} derive secret for master "tls13 derived"
		derived = server.derive(handshake);
		assertArrayEquals(Utility.hex("e2f16030251df0874ba19b9aba257610bc6d531c1dd206df0ca6e84ae2a26742"), derived);

		// {server} extract secret "master":
		sMaster = server.master(handshake);
		assertArrayEquals(Utility.hex("e2d32d4ed66dd37897a0e80c84107503ce58bf8aad4cb55a5002d77ecb890ece"), sMaster);

		// {server} send handshake record (TLSPlaintext)
		// payload:ServerHello1

		// {server} derive write traffic keys for handshake data
		key = client.key(sServerHandshakeTraffic);
		iv = client.iv(sServerHandshakeTraffic);
		assertArrayEquals(Utility.hex("27c6bdc0a3dcea39a47326d79bc9e4ee"), key);
		assertArrayEquals(Utility.hex("9569ecdd4d0536705e9ef725"), iv);

		// {server} construct an EncryptedExtensions handshake message
		server.hash(Utility.hex(EncryptedExtensions1));

		// {server} calculate finished "tls13 finished"
		finished = server.finishedVerifyData(sServerHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("48d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2"), finished);

		// {server} construct a Finished handshake message
		server.hash(Utility.hex(ServerFinished1));

		// {server} send handshake record
		// payload:EncryptedExtensions,Finished

		// {server} derive secret "tls13 c ap traffic"
		sClientApplicationTraffic = server.clientApplicationTraffic(sMaster, server.hash());
		assertArrayEquals(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"), sClientApplicationTraffic);

		// {server} derive secret "tls13 s ap traffic"
		sServerApplicationTraffic = server.serverApplicationTraffic(sMaster, server.hash());
		assertArrayEquals(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"), sServerApplicationTraffic);

		// {server} derive secret "tls13 exp master"
		exporter = server.exporterMaster(sMaster, server.hash());
		assertArrayEquals(Utility.hex("3fd93d4ffddc98e64b14dd107aedf8ee4add23f4510f58a4592d0b201bee56b4"), exporter);

		// {server} derive write traffic keys for application data
		key = server.key(sServerApplicationTraffic);
		iv = server.iv(sServerApplicationTraffic);
		assertArrayEquals(Utility.hex("e857c690a34c5a9129d833619684f95e"), key);
		assertArrayEquals(Utility.hex("0685d6b561aab9ef1013faf9"), iv);

		// {server} derive read traffic keys for early application data
		// (same as client early application data write traffic keys)
		key = server.key(clientEarlyTraffic);
		iv = server.iv(clientEarlyTraffic);
		assertArrayEquals(Utility.hex("920205a5b7bf2115e6fc5c2942834f54"), key);
		assertArrayEquals(Utility.hex("6d475f0993c8e564610db2b9"), iv);

		// CLIENT
		client.hash(Utility.hex(ServerHello1));

		// {client} derive secret for handshake "tls13 derived"
		early = client.early(cResumption);
		derived = client.derive(early);
		assertArrayEquals(Utility.hex("5f1790bbd82c5e7d376ed2e1e52f8e6038c9346db61b43be9a52f77ef3998e80"), derived);

		// {client} extract secret "handshake"
		// (same as server handshake secret)
		sharedECDHKey = client.sharedKey(server.publicKey());
		assertArrayEquals(Utility.hex("f44194756ff9ec9d25180635d66ea6824c6ab3bf179977be37f723570e7ccb2e"), sharedECDHKey);
		handshake = client.handshake(early, sharedECDHKey);
		assertArrayEquals(Utility.hex("005cb112fd8eb4ccc623bb88a07c64b3ede1605363fc7d0df8c7ce4ff0fb4ae6"), handshake);

		// {client} derive secret "tls13 c hs traffic"
		// (same as server)
		cClientHandshakeTraffic = client.clientHandshakeTraffic(handshake, client.hash());
		assertArrayEquals(Utility.hex("2faac08f851d35fea3604fcb4de82dc62c9b164a70974d0462e27f1ab278700f"), cClientHandshakeTraffic);

		// {client} derive secret "tls13 s hs traffic"
		// (same as server)
		cServerHandshakeTraffic = client.serverHandshakeTraffic(handshake, client.hash());
		assertArrayEquals(Utility.hex("fe927ae271312e8bf0275b581c54eef020450dc4ecffaa05a1a35d27518e7803"), cServerHandshakeTraffic);

		// {client} derive secret for master "tls13 derived"
		// (same as server)
		derived = client.derive(handshake);
		assertArrayEquals(Utility.hex("e2f16030251df0874ba19b9aba257610bc6d531c1dd206df0ca6e84ae2a26742"), derived);

		// {client} extract secret "master"
		// (same as server master secret)
		cMaster = client.master(handshake);
		assertArrayEquals(Utility.hex("e2d32d4ed66dd37897a0e80c84107503ce58bf8aad4cb55a5002d77ecb890ece"), cMaster);

		// {client} derive read traffic keys for handshake data
		// (same as server handshake data write traffic keys)
		key = client.key(cServerHandshakeTraffic);
		iv = client.iv(cServerHandshakeTraffic);
		assertArrayEquals(Utility.hex("27c6bdc0a3dcea39a47326d79bc9e4ee"), key);
		assertArrayEquals(Utility.hex("9569ecdd4d0536705e9ef725"), iv);

		client.hash(Utility.hex(EncryptedExtensions1));

		// {client} calculate finished "tls13 finished"
		// (same as server)
		client.finishedVerifyData(cServerHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("48d3e0e1b3d907c6acff145e16090388c77b05c050b634ab1a88bbd0dd1a34b2"), finished);

		client.hash(Utility.hex(ServerFinished1));

		// {client} derive secret "tls13 c ap traffic"
		// (same as server)
		cClientApplicationTraffic = client.clientApplicationTraffic(sMaster, client.hash());
		assertArrayEquals(Utility.hex("2abbf2b8e381d23dbebe1dd2a7d16a8bf484cb4950d23fb7fb7fa8547062d9a1"), cClientApplicationTraffic);

		// {client} derive secret "tls13 s ap traffic"
		// (same as server)
		cServerApplicationTraffic = client.serverApplicationTraffic(sMaster, client.hash());
		assertArrayEquals(Utility.hex("cc21f1bf8feb7dd5fa505bd9c4b468a9984d554a993dc49e6d285598fb672691"), cServerApplicationTraffic);

		// {client} derive secret "tls13 exp master"
		// (same as server)
		exporter = client.exporterMaster(cMaster, client.hash());
		assertArrayEquals(Utility.hex("3fd93d4ffddc98e64b14dd107aedf8ee4add23f4510f58a4592d0b201bee56b4"), exporter);

		// {client} construct an EndOfEarlyData handshake message
		// {client} send handshake record
		client.hash(Utility.hex(EndOfEarlyData));

		// {client} derive write traffic keys for handshake data
		key = client.key(cClientHandshakeTraffic);
		iv = client.iv(cClientHandshakeTraffic);
		assertArrayEquals(Utility.hex("b1530806f4adfeac83f1413032bbfa82"), key);
		assertArrayEquals(Utility.hex("eb50c16be7654abf99dd06d9"), iv);

		// {client} derive read traffic keys for application data
		// (same as server application data write traffic keys)
		key = client.key(cServerApplicationTraffic);
		iv = client.iv(cServerApplicationTraffic);
		assertArrayEquals(Utility.hex("e857c690a34c5a9129d833619684f95e"), key);
		assertArrayEquals(Utility.hex("0685d6b561aab9ef1013faf9"), iv);

		// {client} calculate finished "tls13 finished"
		finished = client.finishedVerifyData(cClientHandshakeTraffic, client.hash());
		assertArrayEquals(Utility.hex("7230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d"), finished);

		// {client} construct a Finished handshake message
		// {client} send handshake record
		client.hash(Utility.hex(ClientFinished1));

		// {client} derive write traffic keys for application data
		key = client.key(cClientApplicationTraffic);
		iv = client.iv(cClientApplicationTraffic);
		assertArrayEquals(Utility.hex("3cf122f301c6358ca7989553250efd72"), key);
		assertArrayEquals(Utility.hex("ab1aec26aa78b8fc1176b9ac"), iv);

		// {client} derive secret "tls13 res master":
		cResumption = client.resumptionMaster(cMaster, client.hash());
		assertArrayEquals(Utility.hex("5e95bdf1f89005ea2e9aa0ba85e728e3c19c5fe0c699e3f5bee59faebd0b5406"), cResumption);

		// SERVER
		server.hash(Utility.hex(EndOfEarlyData));

		// {server} derive read traffic keys for handshake data
		// (same as client handshake data write traffic keys)
		key = client.key(sClientHandshakeTraffic);
		iv = client.iv(sClientHandshakeTraffic);
		assertArrayEquals(Utility.hex("b1530806f4adfeac83f1413032bbfa82"), key);
		assertArrayEquals(Utility.hex("eb50c16be7654abf99dd06d9"), iv);

		// {server} calculate finished "tls13 finished"
		// (same as client)
		finished = server.finishedVerifyData(cClientHandshakeTraffic, server.hash());
		assertArrayEquals(Utility.hex("7230a9c952c25cd6138fc5e6628308c41c5335dd81b9f96bcea50fd32bda416d"), finished);

		// {server} derive read traffic keys for application data
		// (same as client application data write traffic keys)
		key = server.key(sClientApplicationTraffic);
		iv = server.iv(sClientApplicationTraffic);
		assertArrayEquals(Utility.hex("3cf122f301c6358ca7989553250efd72"), key);
		assertArrayEquals(Utility.hex("ab1aec26aa78b8fc1176b9ac"), iv);

		server.hash(Utility.hex(ClientFinished1));

		// {server} derive secret "tls13 res master"
		// (same as client)
		sResumption = server.resumptionMaster(sMaster, client.hash());
		assertArrayEquals(Utility.hex("5e95bdf1f89005ea2e9aa0ba85e728e3c19c5fe0c699e3f5bee59faebd0b5406"), sResumption);

		// {client} send application_data record
		// {server} send application_data record
		// {client} send alert record
		// {server} send alert record
		// System.out.println(Utility.hex(cResumption));
	}

	@Test
	void testServer() throws Exception {
		final ServerSecrets secrets = new ServerSecrets(CipherSuite.TLS_AES_128_GCM_SHA256);

		// {server} extract secret "early"
		// final byte[] early = secrets.extract(new byte[0], new byte[32]);
		final byte[] early = secrets.early();
		assertArrayEquals(Utility.hex("33ad0a1c607ec03b09e6cd9893680ce210adf300aa1f2660e1b22e10f170f92a"), early);
		assertArrayEquals(secrets.early(), early);

		// {server} derive secret for handshake "tls13 derived":
		byte[] derived = secrets.derive(early);
		assertArrayEquals(Utility.hex("6f2615a108c702c5678f54fc9dbab69716c076189c48250cebeac3576c3611ba"), derived);

		// {server} ClientPublicKey to ECDH shared key
		final String ecdh_shared = "8bd4054fb55b9d63fdfbacf9f04b9f0d35e6d63f537563efd46272900f89492d";

		// {server} extract secret "handshake":
		// final byte[]
		// handshake=secrets.handshake(derived,Utility.hex(ecdh_shared));
		final byte[] handshake = secrets.handshake(Utility.hex(ecdh_shared));
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
		derived = secrets.derive(handshake);
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
		assertArrayEquals(Utility.hex("dbfaa693d1762c5b666af5d950258d01"), secrets.handshakeTrafficWriteKey());
		assertArrayEquals(Utility.hex("5bd3c71b836e0b76bb73265f"), secrets.handshakeTrafficWriteIv());

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

	@Test
	void testCipher() throws Exception {
		// 1-RTT
		// TLS_AES_128_GCM_SHA256

		final ServerSecrets server = new ServerSecrets(CipherSuite.TLS_AES_128_GCM_SHA256);
		final ClientSecrets client = new ClientSecrets(CipherSuite.TLS_AES_128_GCM_SHA256);

		// {server} send handshake record:
		// payload:EncryptedExtentions+Certificate+CertificateVerify+Finished
		// 17030302a2
		// d1ff334a56f5bff6594a07cc87b580233f500f45e489e7f33af35edf7869fcf4
		// 0aa40aa2b8ea73f848a7ca07612ef9f945cb960b4068905123ea78b111b429ba9191cd05d2a389280f526134aadc7fc78c4b729df828b5ecf7b13bd9aefb0e57f271585b8ea9bb355c7c79020716cfb9b1183ef3ab20e37d57a6b9d7477609aee6e122a4cf51427325250c7d0e509289444c9b3a648f1d71035d2ed65b0e3cdd0cbae8bf2d0b227812cbb360987255cc744110c453baa4fcd610928d809810e4b7ed1a8fd991f06aa6248204797e36a6a73b70a2559c09ead686945ba246ab66e5edd8044b4c6de3fcf2a89441ac66272fd8fb330ef8190579b3684596c960bd596eea520a56a8d650f563aad27409960dca63d3e688611ea5e22f4415cf9538d51a200c27034272968a264ed6540c84838d89f72c24461aad6d26f59ecaba9acbbb317b66d902f4f292a36ac1b639c637ce343117b659622245317b49eeda0c6258f100d7d961ffb138647e92ea330faeea6dfa31c7a84dc3bd7e1b7a6c7178af36879018e3f252107f243d243dc7339d5684c8b0378bf30244da8c87c843f5e56eb4c5e8280a2b48052cf93b16499a66db7cca71e4599426f7d461e66f99882bd89fc50800becca62d6c74116dbd2972fda1fa80f85df881edbe5a37668936
		// b335583b599186dc5c6918a396fa48a181d6b6fa4f9d62d513afbb992f2b992f67f8afe67f76913fa388cb5630c8ca01e0c65d11c66a1e2ac4c85977b7c7a6999bbf10dc35ae69f5515614636c0b9b68c19ed2e31c0b3b66763038ebba42f3b38edc0399f3a9f23faa63978c317fc9fa66a73f60f0504de93b5b845e275592c1
		// 2335ee340bbc4fddd502784016e4b3be7ef04dda49f4b440a30cb5d2af939828fd4ae3794e44f94df5a631ede42c1719
		// bfdabf0253fe5175be898e750edc53370d2b

		byte[] temp;

		byte[] key = Utility.hex("3fce516009c21727d0f2e4e86ee403bc");
		byte[] iv = Utility.hex("5d313eb2671276ee13000b30");
		server.encryptReset(key, iv);
		client.decryptReset(key, iv);

		// {server} encrypt
		server.additionalEncrypt(0x02a2);
		temp = server.encrypt(Utility.hex(ServerEncryptedExtensions));
		assertArrayEquals(temp, Utility.hex("d1ff334a56f5bff6594a07cc87b580233f500f45e489e7f33af35edf7869fcf4"));
		temp = server.encrypt(Utility.hex(ServerCertificate));
		assertArrayEquals(temp, Utility.hex("0aa40aa2b8ea73f848a7ca07612ef9f945cb960b4068905123ea78b111b429ba9191cd05d2a389280f526134aadc7fc78c4b729df828b5ecf7b13bd9aefb0e57f271585b8ea9bb355c7c79020716cfb9b1183ef3ab20e37d57a6b9d7477609aee6e122a4cf51427325250c7d0e509289444c9b3a648f1d71035d2ed65b0e3cdd0cbae8bf2d0b227812cbb360987255cc744110c453baa4fcd610928d809810e4b7ed1a8fd991f06aa6248204797e36a6a73b70a2559c09ead686945ba246ab66e5edd8044b4c6de3fcf2a89441ac66272fd8fb330ef8190579b3684596c960bd596eea520a56a8d650f563aad27409960dca63d3e688611ea5e22f4415cf9538d51a200c27034272968a264ed6540c84838d89f72c24461aad6d26f59ecaba9acbbb317b66d902f4f292a36ac1b639c637ce343117b659622245317b49eeda0c6258f100d7d961ffb138647e92ea330faeea6dfa31c7a84dc3bd7e1b7a6c7178af36879018e3f252107f243d243dc7339d5684c8b0378bf30244da8c87c843f5e56eb4c5e8280a2b48052cf93b16499a66db7cca71e4599426f7d461e66f99882bd89fc50800becca62d6c74116dbd2972fda1fa80f85df881edbe5a37668936"));
		temp = server.encrypt(Utility.hex(ServerCertificateVerify));
		assertArrayEquals(temp, Utility.hex("b335583b599186dc5c6918a396fa48a181d6b6fa4f9d62d513afbb992f2b992f67f8afe67f76913fa388cb5630c8ca01e0c65d11c66a1e2ac4c85977b7c7a6999bbf10dc35ae69f5515614636c0b9b68c19ed2e31c0b3b66763038ebba42f3b38edc0399f3a9f23faa63978c317fc9fa66a73f60f0504de93b5b845e275592c1"));
		temp = server.encrypt(Utility.hex(ServerFinished));
		assertArrayEquals(temp, Utility.hex("2335ee340bbc4fddd502784016e4b3be7ef04dda49f4b440a30cb5d2af939828fd4ae3794e44f94df5a631ede42c1719"));
		temp = server.encrypt(new byte[] { TLSPlaintext.HANDSHAKE });
		temp = server.encryptFinal();
		assertArrayEquals(temp, Utility.hex("bfdabf0253fe5175be898e750edc53370d2b"));
		// {client} decrypt
		client.additionalDecrypt(0x02a2);
		temp = client.decrypt(Utility.hex("d1ff334a56f5bff6594a07cc87b580233f500f45e489e7f33af35edf7869fcf4"));
		temp = client.decrypt(Utility.hex("0aa40aa2b8ea73f848a7ca07612ef9f945cb960b4068905123ea78b111b429ba9191cd05d2a389280f526134aadc7fc78c4b729df828b5ecf7b13bd9aefb0e57f271585b8ea9bb355c7c79020716cfb9b1183ef3ab20e37d57a6b9d7477609aee6e122a4cf51427325250c7d0e509289444c9b3a648f1d71035d2ed65b0e3cdd0cbae8bf2d0b227812cbb360987255cc744110c453baa4fcd610928d809810e4b7ed1a8fd991f06aa6248204797e36a6a73b70a2559c09ead686945ba246ab66e5edd8044b4c6de3fcf2a89441ac66272fd8fb330ef8190579b3684596c960bd596eea520a56a8d650f563aad27409960dca63d3e688611ea5e22f4415cf9538d51a200c27034272968a264ed6540c84838d89f72c24461aad6d26f59ecaba9acbbb317b66d902f4f292a36ac1b639c637ce343117b659622245317b49eeda0c6258f100d7d961ffb138647e92ea330faeea6dfa31c7a84dc3bd7e1b7a6c7178af36879018e3f252107f243d243dc7339d5684c8b0378bf30244da8c87c843f5e56eb4c5e8280a2b48052cf93b16499a66db7cca71e4599426f7d461e66f99882bd89fc50800becca62d6c74116dbd2972fda1fa80f85df881edbe5a37668936"));
		temp = client.decrypt(Utility.hex("b335583b599186dc5c6918a396fa48a181d6b6fa4f9d62d513afbb992f2b992f67f8afe67f76913fa388cb5630c8ca01e0c65d11c66a1e2ac4c85977b7c7a6999bbf10dc35ae69f5515614636c0b9b68c19ed2e31c0b3b66763038ebba42f3b38edc0399f3a9f23faa63978c317fc9fa66a73f60f0504de93b5b845e275592c1"));
		temp = client.decrypt(Utility.hex("2335ee340bbc4fddd502784016e4b3be7ef04dda49f4b440a30cb5d2af939828fd4ae3794e44f94df5a631ede42c1719"));
		temp = client.decrypt(Utility.hex("bfdabf0253fe5175be898e750edc53370d2b"));
		temp = client.decryptFinal();
		assertArrayEquals(Arrays.copyOfRange(temp, 0, 40), Utility.hex(ServerEncryptedExtensions));
		assertArrayEquals(Arrays.copyOfRange(temp, 40, 40 + 445), Utility.hex(ServerCertificate));
		assertArrayEquals(Arrays.copyOfRange(temp, 40 + 445, 40 + 445 + 136), Utility.hex(ServerCertificateVerify));
		assertArrayEquals(Arrays.copyOfRange(temp, 40 + 445 + 136, 40 + 445 + 136 + 36), Utility.hex(ServerFinished));
		assertEquals(temp[temp.length - 1], TLSPlaintext.HANDSHAKE);

		// {client} construct a Finished handshake message:
		// payload:Finished
		// 1703030035
		// 75ec4dc238cce60b298044a71e219c56cc77b0517fe9b93c7a4bfc44d87f38f8
		// 0338ac98fc46deb384bd1caeacab6867d726c40546

		key = Utility.hex("dbfaa693d1762c5b666af5d950258d01");
		iv = Utility.hex("5bd3c71b836e0b76bb73265f");
		client.encryptReset(key, iv);
		server.decryptReset(key, iv);

		// {server} encrypt
		client.additionalEncrypt(0x0035);
		temp = client.encrypt(Utility.hex(ClientFinished));
		assertArrayEquals(temp, Utility.hex("75ec4dc238cce60b298044a71e219c56cc77b0517fe9b93c7a4bfc44d87f38f8"));
		temp = client.encrypt(new byte[] { TLSPlaintext.HANDSHAKE });
		temp = client.encryptFinal();
		assertArrayEquals(temp, Utility.hex("0338ac98fc46deb384bd1caeacab6867d726c40546"));
		// {server} decrypt
		server.additionalDecrypt(0x0035);
		temp = server.decrypt(Utility.hex("75ec4dc238cce60b298044a71e219c56cc77b0517fe9b93c7a4bfc44d87f38f8"));
		temp = server.decrypt(Utility.hex("0338ac98fc46deb384bd1caeacab6867d726c40546"));
		temp = server.decryptFinal();
		assertArrayEquals(Arrays.copyOfRange(temp, 0, 36), Utility.hex(ClientFinished));
		assertEquals(temp[temp.length - 1], TLSPlaintext.HANDSHAKE);

		// {server} construct a NewSessionTicket handshake message:
		// payload:NewSessionTicket
		// 17030300de
		// 3a6b8f90414a97d6959c3487680de5134a2b240e6cffac116e95d41d6af8f6b580dcf3d11d63c758db289a015940252f55713e061dc13e078891a38efbcf5753ad8ef170ad3c7353d16d9da773b9ca7f2b9fa1b6c0d4a3d03f75e09c30ba1e62972ac46f75f7b981be63439b2999ce13064615139891d5e4c5b406f16e3fc181a77ca475840025db2f0a77f81b5ab05b94c01346755f69232c86519d86cbeeac87aac347d143f9605d64f650db4d023e70e952ca49fe5137121c74bc2697687e248746d6df353005f3bce18696129c8153556b3b6c6779b37bf15985684f

		key = Utility.hex("9f02283b6c9c07efc26bb9f2ac92e356");
		iv = Utility.hex("cf782b88dd83549aadf1e984");
		server.encryptReset(key, iv);
		client.decryptReset(key, iv);

		// {server} encrypt
		server.additionalEncrypt(0x00de);
		temp = server.encrypt(Utility.hex(ServerNewSessionTicket));
		assertArrayEquals(temp, Utility.hex("3a6b8f90414a97d6959c3487680de5134a2b240e6cffac116e95d41d6af8f6b580dcf3d11d63c758db289a015940252f55713e061dc13e078891a38efbcf5753ad8ef170ad3c7353d16d9da773b9ca7f2b9fa1b6c0d4a3d03f75e09c30ba1e62972ac46f75f7b981be63439b2999ce13064615139891d5e4c5b406f16e3fc181a77ca475840025db2f0a77f81b5ab05b94c01346755f69232c86519d86cbeeac87aac347d143f9605d64f650db4d023e70e952ca49fe5137121c74bc2697687e"));
		temp = server.encrypt(new byte[] { TLSPlaintext.HANDSHAKE });
		temp = server.encryptFinal();
		assertArrayEquals(temp, Utility.hex("248746d6df353005f3bce18696129c8153556b3b6c6779b37bf15985684f"));
		// {client} decrypt
		client.additionalDecrypt(0x00de);
		temp = client.decrypt(Utility.hex("3a6b8f90414a97d6959c3487680de5134a2b240e6cffac116e95d41d6af8f6b580dcf3d11d63c758db289a015940252f55713e061dc13e078891a38efbcf5753ad8ef170ad3c7353d16d9da773b9ca7f2b9fa1b6c0d4a3d03f75e09c30ba1e62972ac46f75f7b981be63439b2999ce13064615139891d5e4c5b406f16e3fc181a77ca475840025db2f0a77f81b5ab05b94c01346755f69232c86519d86cbeeac87aac347d143f9605d64f650db4d023e70e952ca49fe5137121c74bc2697687e"));
		temp = client.decrypt(Utility.hex("248746d6df353005f3bce18696129c8153556b3b6c6779b37bf15985684f"));
		temp = client.decryptFinal();
		assertArrayEquals(Arrays.copyOfRange(temp, 0, 205), Utility.hex(ServerNewSessionTicket));
		assertEquals(temp[temp.length - 1], TLSPlaintext.HANDSHAKE);

		// {client} send application_data record:
		// 1703030043
		// a23f7054b62c94d0affafe8228ba55cbefacea42f914aa66bcab3f2b9819a8a5b46b395bd54a9a20441e2b62974e1f5a6292a2977014bd1e3deae63aeebb21694915e4
		key = Utility.hex("17422dda596ed5d9acd890e3c63f5051");
		iv = Utility.hex("5b78923dee08579033e523d9");
		client.encryptReset(key, iv);
		server.decryptReset(key, iv);

		// {client} encrypt
		client.additionalEncrypt(0x0043);
		temp = client.encrypt(Utility.hex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031"));
		assertArrayEquals(temp, Utility.hex("a23f7054b62c94d0affafe8228ba55cbefacea42f914aa66bcab3f2b9819a8a5b46b395bd54a9a20441e2b62974e1f5a"));
		temp = client.encrypt(new byte[] { TLSPlaintext.APPLICATION_DATA });
		temp = client.encryptFinal();
		assertArrayEquals(temp, Utility.hex("6292a2977014bd1e3deae63aeebb21694915e4"));
		// {server} decrypt
		server.additionalDecrypt(0x0043);
		temp = server.decrypt(Utility.hex("a23f7054b62c94d0affafe8228ba55cbefacea42f914aa66bcab3f2b9819a8a5b46b395bd54a9a20441e2b62974e1f5a6292a2977014bd1e3deae63aeebb21694915e4"));
		temp = server.decryptFinal();
		assertArrayEquals(Arrays.copyOfRange(temp, 0, temp.length - 1), Utility.hex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031"));
		assertEquals(temp[temp.length - 1], TLSPlaintext.APPLICATION_DATA);

		// {server} send application_data record:
		// 1703030043
		// 2e937e11ef4ac740e538ad36005fc4a46932fc3225d05f82aa1b36e30efaf97d90e6dffc602dcb501a59a8fcc49c4bf2e5f0a21c0047c2abf332540dd032e167c2955d

		// {server} encrypt
		server.additionalEncrypt(0x0043);
		temp = server.encrypt(Utility.hex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031"));
		assertArrayEquals(temp, Utility.hex("2e937e11ef4ac740e538ad36005fc4a46932fc3225d05f82aa1b36e30efaf97d90e6dffc602dcb501a59a8fcc49c4bf2"));
		temp = server.encrypt(new byte[] { TLSPlaintext.APPLICATION_DATA });
		temp = server.encryptFinal();
		assertArrayEquals(temp, Utility.hex("e5f0a21c0047c2abf332540dd032e167c2955d"));
		// {client} decrypt
		client.additionalDecrypt(0x0043);
		temp = client.decrypt(Utility.hex("2e937e11ef4ac740e538ad36005fc4a46932fc3225d05f82aa1b36e30efaf97d90e6dffc602dcb501a59a8fcc49c4bf2e5f0a21c0047c2abf332540dd032e167c2955d"));
		temp = client.decryptFinal();
		assertArrayEquals(Arrays.copyOfRange(temp, 0, temp.length - 1), Utility.hex("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f3031"));
		assertEquals(temp[temp.length - 1], TLSPlaintext.APPLICATION_DATA);

		// {client} send alert record:
		// 1703030013
		// c9872760655666b74d7ff1153efd6db6d0b0e3
		// {client} encrypt
		client.additionalEncrypt(0x0013);
		temp = client.encrypt(Utility.hex("0100"));
		temp = client.encrypt(new byte[] { TLSPlaintext.ALERT });
		temp = client.encryptFinal();
		assertArrayEquals(temp, Utility.hex("c9872760655666b74d7ff1153efd6db6d0b0e3"));

		// {server} send alert record:
		// 1703030013
		// b58fd67166ebf599d24720cfbe7efa7a8864a9
		// {server} encrypt
		server.additionalEncrypt(0x0013);
		temp = server.encrypt(Utility.hex("0100"));
		temp = server.encrypt(new byte[] { TLSPlaintext.ALERT });
		temp = server.encryptFinal();
		assertArrayEquals(temp, Utility.hex("b58fd67166ebf599d24720cfbe7efa7a8864a9"));

		// 0-RTT

		// {client} extract secret "early"
		// temp = client.early(0,PSK=resumption);
		temp = client.extract(new byte[0], Utility.hex("4ecd0eb6ec3b4d87f5d6028f922ca4c5851a277fd41311c9e62d2c9492e1c4f3"));
		assertArrayEquals(temp, Utility.hex("9b2188e9b2fc6d64d71dc329900e20bb41915000f678aa839cbb797cb7d8332c"));

		// System.out.println(Utility.hex(temp));
	}

}