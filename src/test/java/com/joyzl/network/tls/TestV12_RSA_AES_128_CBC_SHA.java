package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.junit.jupiter.api.Test;

import com.joyzl.network.TestHelper;
import com.joyzl.network.buffer.DataBuffer;

/**
 * TLS 1.2 抓包数据验证测试<br>
 * 测试数据来源 https://blog.csdn.net/ayang1986/article/details/132429318
 * 
 * @author ZhangXi 2025年3月22日
 */
public class TestV12_RSA_AES_128_CBC_SHA extends TestHelper {

	final byte[] preMasterSecret = bytes("030306de06b93c702efe62e8b7374c7012b2e98eeae027a8f56af5e7dff0047863277b4b87f73adb8f98f8e3a3ce1641");
	final byte[] clientRandom = bytes("3defe4aeefe2823d83125a7ffaf6e8da55663f9c9b489455e3fa02dccbb4b386");
	final byte[] clientHello = bytes("""
			010001fc03033defe4aeefe2823d83125a7ffaf6e8da55663f9c9b489455e3fa
			02dccbb4b3862038e6cfe069ea51851c6ebab677ece132fcf6893a3d2587e438
			66023950318e0c00203a3a130113021303c02bc02fc02cc030cca9cca8c013c0
			14009c009d002f003501000193dada000000170000ff01000100000a000a0008
			3a3a001d00170018000b00020100002300000010000e000c0268320868747470
			2f312e31000500050100000000000d0012001004030804040105030805050108
			060601001200000033002b00293a3a000100001d00208a44875a4f03a8251d8c
			ab703b0fceb247a06ca7f5f951787c464226e0ac7b7a002d00020101002b000b
			0a8a8a0304030303020301001b00030200024a4a000100001500e50000000000
			0000000000000000000000000000000000000000000000000000000000000000
			0000000000000000000000000000000000000000000000000000000000000000
			0000000000000000000000000000000000000000000000000000000000000000
			0000000000000000000000000000000000000000000000000000000000000000
			0000000000000000000000000000000000000000000000000000000000000000
			0000000000000000000000000000000000000000000000000000000000000000
			0000000000000000000000000000000000000000000000000000000000000000
			""");

	final byte[] serverRandom = bytes("10300d3fc259adedf24883a2003b839455c9ec65ef8d04a3a32d820b2cdb6f7d");
	final byte[] serverHello = bytes("""
			02000035030310300d3fc259adedf24883a2003b839455c9ec65ef8d04a3a32d
			820b2cdb6f7d00002f00000dff010001000023000000170000
			""");
	final byte[] serverCertificate = bytes("""
			0b00037500037200036f3082036b30820253a00302010202040577a6a2300d06
			092a864886f70d01010b05003066310b3009060355040613027a68310b300906
			0355040813026764310b300906035504071302677a3110300e060355040a1307
			646576656c6f703111300f060355040b13086368656e7368756e311830160603
			550403130f6368656e7368756e3133312e636f6d301e170d3233303832313037
			313730345a170d3333303831383037313730345a3066310b3009060355040613
			027a68310b3009060355040813026764310b300906035504071302677a311030
			0e060355040a1307646576656c6f703111300f060355040b13086368656e7368
			756e311830160603550403130f6368656e7368756e3133312e636f6d30820122
			300d06092a864886f70d01010105000382010f003082010a0282010100ac48b0
			4818687a64255bc924ec9d7960f6dd9aab315e3c41de19d237130283c8d9f30a
			c6e5903ffe5129abfbfaa30d0cb78ab5a78854350ab06c6a3300f196ee48a906
			e082f60ba17c29a889779dd0ded8c8cd58b2efc94377de17d20be8efa1a26bb2
			8d6f4bef4f228fe9e87c39dd14922cc7317c29777db2f80df488406134110511
			7dbec06a8abf666309cd18083291a0cc93f4142cc34bfb7273a91bf1429bc3a6
			22afb4d83f63d01c398dbba3706747eccf53df767224be0d11ee0f1f5452535b
			ccd8328bde900ecee39e4f66263385298eca61f489bd7ad4d4c155fc0e5fdf97
			d850d4860d0c865c0112a3faafa890dc747e1eed3bad3b207e2c07f9d9020301
			0001a321301f301d0603551d0e041604140987aef2475083dafc74acab565fea
			834cd6e86b300d06092a864886f70d01010b05000382010100ab060d4e81a2cc
			d40676b9575da6b74a9796d0e3f8da62ad5abb0fa64d6dfb30d60e45801d5f73
			bf666e9db72482fd1385239bedf13a95b1aacefcef5bd33ebf609199e0cc397d
			9b656281c47bc547c94de7eff0ccd237433a1b82952ea87165741923a61f418b
			f841d9960fec804679fcdff1397ffe073c7367230697032d5c5fe9c2c8dbad4f
			d5ccdf7f5225c86bb2b456e4124f9cb515a458e1a2cfb4e5925d53a19a050bf2
			4787b3e77922735d13b1a93408c4e49384073d7a86c0d8ddd6ba5a28629941fd
			10e9d11f7ec3e8252277069340a8aee378b762b19234c412fa32ad6f376caabc
			aeec0be7f591861464811afb3fb4201825a9d88dfafe7da242
			""");
	final byte[] serverCertificateRequest = bytes("""
			0d0000a003010240002e040305030603080708080809080a080b080408050806
			040105010601030302030301020103020202040205020602006a00683066310b
			3009060355040613027a68310b3009060355040813026764310b300906035504
			071302677a3110300e060355040a1307646576656c6f703111300f060355040b
			13086368656e7368756e311830160603550403130f6368656e7368756e313331
			2e636f6d
			""");
	final byte[] serverHelloDone = bytes("0e000000");

	final byte[] clientCertificate = bytes("""
			0b00037500037200036f3082036b30820253a00302010202044f3e3a8e300d06
			092a864886f70d01010b05003066310b3009060355040613027a68310b300906
			0355040813026764310b300906035504071302677a3110300e060355040a1307
			646576656c6f703111300f060355040b13086368656e7368756e311830160603
			550403130f6368656e7368756e3133312e636f6d301e170d3233303832313037
			313831375a170d3233313131393037313831375a3066310b3009060355040613
			027a68310b3009060355040813026764310b300906035504071302677a311030
			0e060355040a1307646576656c6f703111300f060355040b13086368656e7368
			756e311830160603550403130f6368656e7368756e3133312e636f6d30820122
			300d06092a864886f70d01010105000382010f003082010a0282010100ebc113
			8eb2541f228729de60d63391bbf0e7350d45e106a16f6fb8a379c4fffc6b0e72
			acf307376ae17d1ec762e9d403fb7031094a9a8c1a774b71e3f3606a71d7cedd
			1d90e34f7147399efb43ea0422d3490399fc8f15d7e11927cbdffaad3ca4b14b
			a12d4950bb3e1aaf5b6639f7ff3adb52c5864031e339ce5a2e2ca9e192723a13
			39d86c2d4b8c8b8274f9bf4eba49623855fc9612b8163ffb116113745e877658
			0f66d2baa9455246119cc060c0c3fad432546b40a0ca3e5b6492bef7f4f70037
			e4e462f330e3894bfba04e871678ccd9c064a85109a36d70ce900ab5bf394c62
			861813131b5998f29ca87d300a40954a615374f83a72f9fe2ce55ac0b9020301
			0001a321301f301d0603551d0e04160414c92f402c3cd561508d4990b8ba33b6
			caf72a79c1300d06092a864886f70d01010b0500038201010007714ec8a3f234
			b78cd974b20315545f93d7a58c246b472b51835cbe22e5e859aa654543dce168
			86c0f4b621448c8acab14f2673b8aa13a7897a25fd6015b40b4ea8dde8c71093
			6669274edeb5efb83245e50fee15dbe260b1f7f23f55d8a1b49675db6cd7f78a
			0e32e64f2b014d48c4fdb99955e00a0f4ff07d93707bc3264a2efbda95290245
			f73fcc47f91324232415166649cb3d55f3c4543b9b8650fc3782e311050bd665
			941eb0d22ac1b66242438759bb690302f231e71d854f73ffc151f24bfadbfed2
			55e52cce646fe3d0a81973c9cfccbe68b7c20f665377e1b247d1047c774a0f59
			469059a3ff9c13f2cd3959312095e4078fb55854ed591a1954
			""");
	final byte[] clientKeyExchange = bytes("""
			1000010201009497055d91c47657b012a99c3f4aaf6cc2051d5215c955f8c1c0
			b5b5bd1b702c87a6634feed4ac6ddc963f486316464e338743cb4a8881d499dc
			7fe068c39f96e260b27696668ff14c966d48bc746872bbce5bc29532a830c2aa
			94957c5ce0a984d344f39e011acf5a0a82fcd56fa27dde73a83a193af18e6456
			1ae7a0d4eddc7021be89f4244cf6c6ab36241659a3fd679928a73a15fef615fe
			7a2f1a6e1896ecf1d1c74efda6a763a669f30fc6d109ff7bde4e0f7ed812d9bb
			f53ad6bcd45c789c15b1e62ad33706ebfc43ebbea4f675072174f3bd4e47cc7d
			37971b40194b0f5c4c0e1c1e2541a1cded7b03657230c4724b5fcb98f1795bdc
			5823a8fb3891
			""");
	final byte[] clientCertificateVerify = bytes("""
			0f0001040401010015e5034ae9c79f4ba369085900170e2caa014c4a736ac61ef
			dd008e87927947dfd4650869c2fd753b159fcabd08826811d3759134ae34b87be
			9412f97eed89dfe5b1b1bccc252c7df75a6b465d94873a59b49e29332f21a3475
			8d8aee695512db4a763191af13782c0ea7f2a06cee8d439823f4f199096f5f192
			713df729354641e313cf6ec3cb205035aaed81f74f80baef31cfd944a1ce1175c
			d6e761ab317a75d2112d2488f8d2cea7c575da77402659a40a5f2b7fdcce749d7
			9dfb0dc402c9ab44838b347020f68061459c1047f21da88f5b4c06df0661af5f0
			6f6475f97504cae945c5c5f172b508011c562606d4e84c3bd930a7fad3e1b25e4
			648ba91f
			""");
	final byte[] clientFinished = bytes("1400000c83c23dd8f9220c593a267349");
	final byte[] clientFinishedEncrypted = bytes("""
			977c701d3c6f0ae4d284751816a7ab73b40e90b26d209e2d9128031874b8311fd
			f8f50edfef9db071c12ab4fbc41e931268ce046c53f4ab4670988e481fbc335
			""");

	final byte[] serverNewSessionTicket = bytes("""
			04000426000151800420c0e58b07b19ba9b6bda00376f24d0a6f8cfdb9de8cb2f
			307938fc9c36f4b5eb683fe89d57786887a8474ce03f5e30c3d7854a1be76cec2
			474a5b6bb8ec683fb07b60f6b13e35a510a5cb3e392644a33026b3b06b96cccbc
			8dbf020458c776e743a5c4f78328fbdf64e0141c5a0ca85938b28993ebbffff98
			26cf824e0ef2cc04be6f9df409ad0618bcb902ac4395a5b05b4a731093b4a0599
			4b4eba5643fed5b40077edf333bb61cbc6ae2a2e7bfff6a2c85813c7078c8703c
			11c33896c7dad6fdb11858057896fb8b8536204a5b8e9414335758ac545476827
			f53e10c681463b5d972de7a63fd0d2a76980daa390f1bfddfa931b7fc8301bb83
			21047ea3c027a2cadd50e6c960126e340538e72de3814f794c305f3215ba29ca6
			2a12f7ce15d8b823873d055e147937f6b80051b9c6fe16088c0c0f901939fc0fb
			1f544d7b8bac6f6e5f9971f0b3402249e11663bd44fa57121ba3823b496983c52
			28ce382cc147d031c65ccb596c80644d113506be5bc55dfce23d3a5852b354dd1
			ca35a586e0e4d1c0a940352366b72952334cbee1334488cc9dbfede451ac292cb
			8314ddcec35ca3905bf687a9cc3ec813693387dd5abdc3052cd0982231011bb51
			0c007bcb4de0883ea944655cbb698bf8c440d3c149b2d08c96073ec481f0d3995
			a0ecca0e5281b0de5bec3d0774641512aa228882ee872f3de74309bbb007d50a1
			4d0a27249af675c9e9435e6252f737ecec7a63f3538b40105a64601f7f4a56f39
			b031debc992b741e12c05718717e0accc555c60b97f31880d6700ee489fd89c54
			321130caff9c3bd51c3432f11286cbadddf84efd3124ec4115ea70623fa918a60
			8056e424b37901f5c44307dc9e0c9f17558dfc5aa2b15c0f26e372d3c8a8fdd6c
			0d28d37ab1396bf4c246588c66cef9bbfe01f2ac36f8de7509b13181f1205d75c
			6a20d6e6e84d7694cd407fcc38db1da873291613dd357ea4e8bfcabc6239812bd
			1952a0dccf67c68ec9efcb22a60a2af69b7932c70dc7e5b1e89e7f48a4da03e9a
			db360c7a78e13bcf0002023f0355f5a3dd05c4902a7fdd6fcb42197a1a118968b
			efbc4925137bb3aa5f97a234a95afa0b066d681af36ca023a4b97892117e3002f
			e3a64610b63fa15ac2045c1438ae784f82b5e708fa23f41d2c5224bbdd4a0cbd2
			b588bc2473efca06ce5cc174612b4a14b8f309adfaceba3b6467f63a1b8d3fc35
			c8b085ed189d3ff7f79c66102ecd1e34dcd35ecd4fbb4b5b1b45e1bb24b6bf4e9
			7189ce7019462851c55d8bae1679b0d1532d983d58f3cc3311e56816cbef7141b
			87febd792e10bb6c4ae649ceb0f87194be1e62a648b483abe89252c32bb46594f
			3acce08c6fee727ac0e62e7a1f2d970a78452c08776d203adfdb0eecefbfa096d
			9bdad8df8e0912a2768ce37a24a98231e64668c25de5785d7263cc913423bdbd1
			fbdf00ec3933ab4ad8b2298ff5bb5e1524e38a5710b88b157a68
			""");

	final byte[] serverFinished = bytes("1400000cf3bd666f0519f59dd6293522");
	final byte[] serverFinishedEncrypted = bytes("""
			19815312fc7c55bd42d5990938a4b36d556535c3e3bd2e79a48cb6d16a02869c10
			dcc6b86072aed08222b959a35e76e3796a94c7e35d2ccad6f73077bb10d9bc
			""");

	final byte[] clientRequestEncrypted = bytes("""
			38baa62e9854384bcf779481425ad34d2967c1da990234072125bda8b07de9e282
			05a6d7236c7b5a79ad0284e89383933f2612d2a26eee388e0fb1c53f9fbbc22ae3
			5a523a84a707dcc549b442d51b57ff7505176a1ed4bf556db86a74c7f4534a8511
			d23e9d358affba368c22b4ab84e7a4fd16738115607a44a04284d7edbedccaf07c
			384437655d5790cfbe0197cac44012379c05e941a65bcb976834a89005f76b8bb6
			447894b373421722171e763edfeec38e75cacc26de687888970f3f794983420a0f
			ef1765eef1ac8daea977032a9d34dae1ccb2edd3c5fa091c82be684afd5fe8b390
			8c3e9dd7a6a0afa80a924dc9b59837853c20e51ca8c974dc330a472525a429b7a6
			a35d17a82064a7310c35cbe76c9b8864007547bf3a7d85f976c7d716630ebb1a1b
			82e80483a21a509b0644c2d7490f54104aa5063b59438f94bfd18abd8b803086f0
			6fecaa022b76f580679dd459b3574acb92a5c14d846233ade08bab4cf3b77086d7
			c050674d119519a0b0acde2cafc299b086029f90bc8fdab79499abfe228b9406d4
			70473847a7328530a49c24af7f8d67d7af710e38d63e06902d0a5d543ab4ac8010
			328941a71f218964c93708bc6573bd5b6db9e3371af47eccf00b516d8d3f95ffb2
			1ff8f16d3bddebc9e16f868b83fe002bdbaebf2f3f9e373878f69648b52a93faa9
			433ae15ad842c34196fdd24a4bd0e8ff8aed355cdb450dc7bdcad09cdc8c8bfeea
			7368c68cb985ae720414264c21a03eeba50456d57bd95cb902224bfc5210921ebc
			0d4d0b5d328c7011aa8156dae7e49309de0366cf2d87b16aa3efa63df9f71b9d99
			537f4c6f29ef8853a0884b4bc467a3ffa173d4a7ee4a0bcc827f0cf8593e
			""");
	final byte[] serverResponseEncrypted = bytes("""
			8926bcd7a79659d2d6822ece0b66ee590cfd96f0863f5b74f6456cdbd2cce4db87
			8c73785f1edb412e0ecb97d54fce74300c6a673af621b3c849ede3013de046e80c
			99c7594b34b5033dee5a077fdac3d9c0bf602f284e09cce080527e75379360efef
			954bbe94c62440c5cf80158d49c340cec0fcfb5d016c7bdeed2ee3161e537f5935
			770ac787dba20a41620d400e2e0291decd09d28d31d145760aaf25d07aef1ceeab
			764534eb703ddf0242decfec59536fe86cb19b0d67c3938587900842608dbff5bb
			5d9d471cc4e85fa0d2a5
			""");

	@Test
	void test() throws Exception {
		final V2CipherSuiter client = new V2CipherSuiter(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
		final V2CipherSuiter server = new V2CipherSuiter(CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA);
		client.pms(preMasterSecret);
		server.pms(preMasterSecret);

		// rfc7627 SHA256
		final byte[] session_hash = bytes("82d6bc8d19874cca5fc04db6f9ea9cdda99a15db212243d25bddb77aa49822be");

		client.hash(clientHello);
		client.hash(serverHello);
		client.hash(serverCertificate);
		client.hash(serverCertificateRequest);
		client.hash(serverHelloDone);
		client.hash(clientCertificate);
		client.hash(clientKeyExchange);
		assertArrayEquals(client.hash(), session_hash);

		server.hash(clientHello);
		server.hash(serverHello);
		server.hash(serverCertificate);
		server.hash(serverCertificateRequest);
		server.hash(serverHelloDone);
		server.hash(clientCertificate);
		server.hash(clientKeyExchange);
		assertArrayEquals(server.hash(), session_hash);

		final byte[] master_secret = bytes("7bb571f36c38efba3eb95f5871391830116985671f8cb10de01c71c74319c49f9d2baaf1071490a19999ea56bf1e03dc");
		client.masterSecret();
		assertArrayEquals(client.master(), master_secret);
		server.masterSecret();
		assertArrayEquals(server.master(), master_secret);

		// 104=MAC(20)*2+KEY(16)*2+IV(16)*2
		assertEquals(client.keyBlockLength(), 104);
		client.keyBlock(serverRandom, clientRandom);
		assertEquals(server.keyBlockLength(), 104);
		server.keyBlock(serverRandom, clientRandom);

		// client_write_MAC_key[20]：0d19a4197332db8266200724604c063d8cf116ff
		assertArrayEquals(client.clientWriteMACKey(), bytes("0d19a4197332db8266200724604c063d8cf116ff"));
		// server_write_MAC_key[20]：958513064070ba5137176bb621b27b80cc4c26e0
		assertArrayEquals(client.serverWriteMACKey(), bytes("958513064070ba5137176bb621b27b80cc4c26e0"));
		// client_write_key[16]：60381c0e233f4e1896bf61147da34652
		assertArrayEquals(client.clientWriteKey(), bytes("60381c0e233f4e1896bf61147da34652"));
		// server_write_key[16]：b824d8d1b789ddc05a94a4debb81d40f
		assertArrayEquals(client.serverWriteKey(), bytes("b824d8d1b789ddc05a94a4debb81d40f"));

		// 重置密钥

		client.encryptReset(client.clientWriteKey(), client.clientWriteIV());
		client.encryptMACKey(client.clientWriteMACKey());

		server.encryptReset(server.serverWriteKey(), server.serverWriteIV());
		server.encryptMACKey(server.serverWriteMACKey());

		client.decryptReset(client.serverWriteKey(), client.serverWriteIV());
		client.decryptMACKey(client.serverWriteMACKey());

		server.decryptReset(server.clientWriteKey(), server.clientWriteIV());
		server.decryptMACKey(server.clientWriteMACKey());

		client.hash(clientCertificateVerify);
		server.hash(clientCertificateVerify);

		final DataBuffer buffer1 = DataBuffer.instance();
		final DataBuffer buffer2 = DataBuffer.instance();

		// Client Finished

		byte[] vd = bytes("83c23dd8f9220c593a267349");
		byte[] iv = bytes("977c701d3c6f0ae4d284751816a7ab73");
		byte[] pd = bytes("0b0b0b0b0b0b0b0b0b0b0b0b");
		byte[] mc = bytes("2ec78d22bfea15e8f20cdade865c49d7424828e3");
		byte[] temp;

		// VerfyData SHA256
		assertArrayEquals(client.clientFinished(), vd);
		assertArrayEquals(server.clientFinished(), vd);

		// MAC SHA1
		buffer1.write(clientFinished);
		temp = client.encryptMAC(Record.HANDSHAKE, TLS.V12, buffer1, buffer1.readable());
		assertArrayEquals(mc, temp);

		buffer2.write(iv);
		client.encryptBlock(iv);
		client.encryptUpdate(buffer1, buffer2, buffer1.readable());
		client.encryptUpdate(mc, buffer2);
		client.encryptUpdate(pd, buffer2);
		client.encryptFinal(buffer2);

		buffer1.write(clientFinishedEncrypted);
		assertEquals(buffer1, buffer2);

		buffer2.clear();
		buffer1.readFully(iv);
		server.decryptBlock(iv);
		server.decryptFinal(buffer1, buffer2);

		// MAC SHA1
		temp = server.decryptMAC(Record.HANDSHAKE, TLS.V12, buffer2, 16);
		assertArrayEquals(mc, temp);

		buffer2.skipBytes(4);
		buffer2.readFully(vd);
		assertArrayEquals(server.clientFinished(), vd);
		buffer2.readFully(mc);
		assertArrayEquals(mc, temp);
		buffer2.readFully(temp = new byte[pd.length]);
		assertArrayEquals(pd, temp);

		client.hash(clientFinished);
		client.hash(serverNewSessionTicket);
		server.hash(clientFinished);
		server.hash(serverNewSessionTicket);

		// Server Finished

		vd = bytes("f3bd666f0519f59dd6293522");
		iv = bytes("19815312fc7c55bd42d5990938a4b36d");
		pd = bytes("0b0b0b0b0b0b0b0b0b0b0b0b");
		mc = bytes("4643eccf7fb0d847e83af62d58693b7f6e4e8b4c");

		// VerfyData SHA256
		assertArrayEquals(client.serverFinished(), vd);
		assertArrayEquals(server.serverFinished(), vd);

		// MAC SHA1
		buffer1.write(serverFinished);
		temp = server.encryptMAC(Record.HANDSHAKE, TLS.V12, buffer1, buffer1.readable());
		assertArrayEquals(mc, temp);

		buffer2.write(iv);
		server.encryptBlock(iv);
		server.encryptUpdate(buffer1, buffer2, buffer1.readable());
		server.encryptUpdate(mc, buffer2);
		server.encryptUpdate(pd, buffer2);
		server.encryptFinal(buffer2);

		buffer1.write(serverFinishedEncrypted);
		assertEquals(buffer1, buffer2);

		buffer2.clear();
		buffer1.readFully(iv);
		client.decryptBlock(iv);
		client.decryptFinal(buffer1, buffer2);

		// MAC SHA1
		temp = client.decryptMAC(Record.HANDSHAKE, TLS.V12, buffer2, 16);
		assertArrayEquals(mc, temp);

		buffer2.skipBytes(4);
		buffer2.readFully(vd);
		assertArrayEquals(client.serverFinished(), vd);
		buffer2.readFully(mc);
		assertArrayEquals(mc, temp);
		buffer2.readFully(temp = new byte[pd.length]);
		assertArrayEquals(pd, temp);

		// request

		mc = bytes("e44662cd09a0b7be7bcb744f62a8a35f0bdb2ccd");

		buffer1.write(clientRequestEncrypted);
		buffer1.readFully(iv);
		server.decryptBlock(iv);
		server.decryptFinal(buffer1, buffer2);
		// Remove Padding
		buffer2.backSkip(buffer2.backByte());
		// MAC SHA1
		temp = server.decryptMAC(Record.APPLICATION_DATA, TLS.V12, buffer2, buffer2.readable() - 20);
		assertArrayEquals(mc, temp);

		// response

		mc = bytes("dcb3c3f98a2c7645b3be4ece10648040f70d42dc");

		buffer2.clear();
		buffer1.write(serverResponseEncrypted);
		buffer1.readFully(iv);
		client.decryptBlock(iv);
		client.decryptFinal(buffer1, buffer2);
		// Remove Padding
		buffer2.backSkip(buffer2.backByte());
		// MAC SHA1
		temp = client.decryptMAC(Record.APPLICATION_DATA, TLS.V12, buffer2, buffer2.readable() - 20);
		assertArrayEquals(mc, temp);
	}

	final String serverPrivateKey = """
			-----BEGIN RSA PRIVATE KEY-----
			MIIEowIBAAKCAQEArEiwSBhoemQlW8kk7J15YPbdmqsxXjxB3hnSNxMCg8jZ8wrG
			5ZA//lEpq/v6ow0Mt4q1p4hUNQqwbGozAPGW7kipBuCC9guhfCmoiXed0N7YyM1Y
			su/JQ3feF9IL6O+homuyjW9L708ij+nofDndFJIsxzF8KXd9svgN9IhAYTQRBRF9
			vsBqir9mYwnNGAgykaDMk/QULMNL+3JzqRvxQpvDpiKvtNg/Y9AcOY27o3BnR+zP
			U992ciS+DRHuDx9UUlNbzNgyi96QDs7jnk9mJjOFKY7KYfSJvXrU1MFV/A5f35fY
			UNSGDQyGXAESo/qvqJDcdH4e7TutOyB+LAf52QIDAQABAoIBABFqc6+GECG3N/OU
			XNAyhw+fRP2aqq3tB3eNQqvglxN9XVLtVvfj4iQyTjUdjtOmwQ9YzU43QNPv1a/P
			+IHy6iACZlvUnEiFGnjzuR7UYba4oE+EOqA0DzNrzZ641A1jFALYQj977cAuTr8f
			lSnhq5ClyjMEtJl4g9NwsfBzSZbJwxcodWPfjUhoIBNWyuFyI1/1bpt8rJrjaqBz
			N0/ygvwuok7sr2wYZYBzlbqblIk6v6u3hgjTaY6y87sps2HsoRPCbjnKZcH202K4
			+Jq4xJaT3zJA0LBIK0bZr6EqJCo+rjqVF+dPBqq7Va0f/bqtVoYS2BxamuSANd9q
			QdsLiwECgYEA376m//zoxMty6AO8xYQftzJzHZq2RcE4QdwkQ/H8lAZ12S9HpjIO
			vAKAglmVSBOEc60OugQIKW5t5q1LagkjJjhCEPiI37cdkEGEwzIDMuxnJyoYK2uJ
			cORrKgWMcD+2Ld1BkbotCgw1UoWIN4SDBm5fz7MJvypm9AM0j5iv+6ECgYEAxR7d
			UAgOZXzvgOeYN61fRdl8fYP+AXGb5+4BQqSyg4B7I90tt3HegthSzSUMsaqcqb43
			jEaMvx5bcADuuG+NoG1SBHRgpwRWc3kxHOsksk3pqoq6F12ipFfOiaknUk2uYuFm
			6VS8Cuiwi5Mr40Ad7X2vIUREXy1mdwBZn+2sEzkCgYAyT/vbmnJ5Nkqbc6OyQpij
			KFwMbyrHmsBcxvT8tmCWpLFBaklRTPZVPrbJGP3hnEnvfR3PpHJ1ZpieA5/usylN
			Qd9RU6HrtJCYTLNe9VMU02YKv4N52A+q7CJQ67h90UXVIGjLLTNNjBs9eDt+SHTy
			MEWpPdhWPWGzQzlBhRMloQKBgCwjnCT2mNqq3ip8MX6OKN5IM/MbIj6KhQGrLscQ
			gBSoKWq/dSQ1sECWVfwvxqbl8EymFmQnzA5jqs/qtYnBGPKKaCotMRNVClKzGYdD
			NCVlAuS4SbE/u1KXgt0abn6kfF8R3+xxl+XNcvLeEp7BVbI/SX7mfXTegqo0/tyr
			6LMpAoGBAKdX/djhWnKtCA6OATjP9iEIYBkMKaCaBiP6VdtrdsTNPzTbvLHOIkxs
			ObYs3KM5QZ6fAlMzul9Xr7VdOhREzKsBG+uJEf8x6PTlvsyKoJ+ahwN57gMIr92M
			Y2b+7Nh2WaHxDaKPRvoIA7SBmuK0upO1uv1xa0CssOjkCT0Lnc3F
			-----END RSA PRIVATE KEY-----
			""";
	final String clientPrivateKey = """
			-----BEGIN RSA PRIVATE KEY-----
			MIIEogIBAAKCAQEA68ETjrJUHyKHKd5g1jORu/DnNQ1F4Qahb2+4o3nE//xrDnKs
			8wc3auF9Hsdi6dQD+3AxCUqajBp3S3Hj82BqcdfO3R2Q409xRzme+0PqBCLTSQOZ
			/I8V1+EZJ8vf+q08pLFLoS1JULs+Gq9bZjn3/zrbUsWGQDHjOc5aLiyp4ZJyOhM5
			2GwtS4yLgnT5v066SWI4VfyWErgWP/sRYRN0Xod2WA9m0rqpRVJGEZzAYMDD+tQy
			VGtAoMo+W2SSvvf09wA35ORi8zDjiUv7oE6HFnjM2cBkqFEJo21wzpAKtb85TGKG
			GBMTG1mY8pyofTAKQJVKYVN0+Dpy+f4s5VrAuQIDAQABAoIBAHtu3peCxMp8YHpK
			ZTchTvcwvU0oguK7fwCCZRCqa7t/ZGnvHqArshcysjjQOfilOeSGrBXqSpp6LOWC
			XtAJNhIe5L8egMKS0INzJsr1luvNdAQxb4ktPUmHII2Wj9GGrE6qSSe9NzqenniI
			QXl3dmHaZgSjSJQJyqd0ZADy/4sLCaSu5mvAu0G4V9sSDJBgEm2EVKEsH+RQmNeZ
			M10jzx/44N16AGMOkKW6/yYas32VZay3wu57zllWtErwF9fDaqCt4yZ5lr0NARgy
			mxnK1Z78J/8bTnVcae5DUdc3X1/wamL5KWpF/OEXEv0tmuUkmSOVJqSGuzgUvjeT
			BgHzZRECgYEA9lB0u6f/u2QkIcSWx8WpUAW5ZFBNSsuT9PpORjgmTecep9f7PsVI
			dvSDNC6A3vLsjE+OIIXH1xRf4wid6kPXYfQm102visY7Mafi+ykTW64Btv9Wcnzt
			RqtmOB+LclQW5rxnYFdM5CrVdZSyKSAR4y1qft88vsxrWnyV5TzcUAUCgYEA9QZR
			BRqm7fUjeqUEWDsKyv7nOKBa3jLhsOWvVoYUagKTCRvxG2I+vfpyNQLx6NfFrLCR
			THgUtlF9fwpfzHcS+ldtdYmlD0aarEDsthgfSw7NxY8Q0N4F29KtjNYxUWICfY5C
			z/V3XfirzH4D3QrjsDG7SuzGPBAR/ICXXiFNcCUCgYAzxKkkH6UB74QgjL7b8dEp
			/mn+iVoVVEAehtUYsQhr6x1oTV6CzNf0GGPS/cscEJZizv2iHDvqGmAkyVTXPbbw
			4W2gZrV0R0F9qDlubC2jYSj/Gx9CJLuB1NhBB3A5cS73A7X+a5q/gbknYz8moZTg
			2FS/oY+U0kXl2LVwXqA3OQKBgDotosUse+BaaW8wiWSNuFVUywAGCy1hNXd0Qfqw
			YV5+d/0ctlYDMNSZB88QgPIBcb08O3PFu11C4iJtCywYRdhNM/9JjHihEg6+Z7Yg
			6iU/QZqcCqkdXpTAZFbhcNpbBC5dvf100j/s0JYAgdhVQpTOaiMzLyqJwkuLGOXP
			0HxFAoGAeYlJHLvvxo2Cl9uwUcJZImLRxZQezbzhw5nqcudtXiKOsCPI84LdNWPo
			12/v73XhMHxyefb5GBRkrCIPmWOVNdgxev0h4Qq1BCPq11zISrx3PObqGJ+C5qjf
			xO6oDoU3lvJ6f1/yepfMWgwoC0UwxJisy6QH+w4Umi8B0RLw570=
			-----END RSA PRIVATE KEY-----
			""";

	@Test
	void testKey() throws Exception {
		// getClientPrivateKey();
		// getServerPrivateKey();
	}

	PrivateKey getServerPrivateKey() throws Exception {
		final PEM pem = PEM.loadText(serverPrivateKey);
		final String algorithm = pem.getLabel().substring(0, pem.getLabel().length() - 12);
		final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pem.getData());
		final KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePrivate(keySpec);
	}

	PrivateKey getClientPrivateKey() throws Exception {
		final PEM pem = PEM.loadText(clientPrivateKey);
		final String algorithm = pem.getLabel().substring(0, pem.getLabel().length() - 12);
		final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pem.getData());
		final KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePrivate(keySpec);
	}
}