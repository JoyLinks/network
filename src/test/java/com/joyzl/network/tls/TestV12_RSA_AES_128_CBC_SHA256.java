package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.junit.jupiter.api.Test;

import com.joyzl.network.TestHelper;
import com.joyzl.network.buffer.DataBuffer;

/**
 * TLS 1.2 抓包数据验证测试<br>
 * 测试数据来源 https://blog.csdn.net/wzj_whut/article/details/86626529
 * 
 * @author ZhangXi 2025年3月21日
 */
public class TestV12_RSA_AES_128_CBC_SHA256 extends TestHelper {

	final byte[] serverPrivateKey = bytes("""
			308204c0020100300d06092a864886f70d0101010500048
			204aa308204a60201000282010100cd79c60c71f8c034c1
			c7a91af59a964254e15bad10c051767d912bde0b8faf612
			d6d28442e357165203b3cb907c14da3c8c613f9003e95c2
			56553b8cd8b865827300dc7af513bf74f282718566fdbc3
			4234304a04fd7bc965488c291deb3a40413836c27a0d852
			d5b148dde907a39cf251dc0ee272ac38853d9b76340c02b
			386bb066d8ef0d21f0956dbd8b42f7076303497b00e8303
			595862bb4d5e1b75ded5f95abe652592d7078d3cd2cb7e4
			6062751596d7c3c00fb3dfa9e9991cd49cfce11d7be063e
			18504819d60da3550697a1a6fa2c557bba91187d75749cd
			0c8a58c00aff304aed787d1323c07cdf13fcc1d38883382
			dad329f8f0c868c98a6321eb02030100010282010100b98
			2a4e51d8d08f358b4cb9f5478d20a6719e3ac17c09a8bd1
			085d43a6ca7dfbcd888cdf299d0498d985c1e807f165086
			73f364f0c02753134c2d4f5efc0639d67f932162ce83c65
			f880febf036238261663ee6c5249a3b151f6fb3bb98614a
			1bd805d085636986ae819b2882e66704f72caac0194a89a
			8ae9394f79e4121843fe477821613221b2e4f97ba5b44a8
			380dcb1f389875b7e06a5d488af9546fb8521fcba01f083
			1e274cbab8570ba5ecb87b5e56c72f4c6424c07bc051d6b
			a5815831926128eb49b4e9748723f1aac12bd5d0b189001
			e5dac41ff22e96cbbba546bd8c7ecdb60639d2fcea711a7
			645dd0b2f70aaab0db490b16fe01c928b59de9902818100
			ffabd05765c4806bf6d2225e0575874a8bfc57ef7aa5702
			3b6d609f0d38453342e12012d35abf7397465278f62b929
			9b2bec8e57e39cb0e85701fe6b1382e8668ca66558d01a7
			5ea00f335f5bc723d360a3ff0fbb68bc1376572ac0e7a8b
			4f3cab28190e5ef24f6a391e4ecbe9f17cb03da071c723d
			3bb299047a06f2221e65f02818100cdbd6e81fc2da7742f
			fb1a5dd4ba672a80a14311fe27ef622ddd93bec4a7b1012
			d06d85e8a80eebd181399bafd3f3705f9533b4ce703eb01
			93386ef1a6b57a2beccfcd7cedf678c6b1bd7326d83fb3a
			1fc43e6bc2f68eebc91e9458328ef18558b23f2ecf9debd
			5fee384acd2b410e7987899454cbef90c616f2db061cd4f
			7f502818100b94943e0c2c0e8acd5a02e05e380ec3e4b3a
			e2638c87aec04335d4d87201b8e6dce8716fd930ba272a4
			250ac2a48b46d5f24bd77d65d6d1bc44a8e9dfcf2e707c2
			b0a84bf7ece53d63d8de672b5dc31c91cb0b12f09551be9
			ac7c3076f29dc8fc41e7822210f0c1c982e6dbe7bd9659a
			b7deb9f62ef1b7f70437d47e15c7699302818100cad7a76
			a549d166a011c4ae493e5abed45a0b8a5b6b4dcd9296b3a
			4d6d49ffc3af06feb04751d30f6ffb7327dea342b68b18b
			a70999b4d49e242536fc0f34f9e9afe4e148bf0e326d2fa
			7bdba27d3bb7cb258f099e9c90342e8f94cf39be7179759
			35de0b2821ea7c4108fff345411d754001ad477b49c4211
			42231b25b5fb3902818100c64c6245cda082a48448c7b57
			b611b2c67d0b688177dc076092051427348ce21c0c266fc
			ae90f9db61e48183b0683032f7e2f29d7901adb4ca87656
			6e4e29b3a67605f005268a482a5cca5f79ae8bb3210b709
			ce7d8c31ec604f8eb5cf144dee6603dba466ebf73a9f741
			e4a704dd60de8a2c11e216e688304e93d0917ad3877
			""");
	final byte[] serverPublicKey = bytes("""
			30820122300d06092a864886f70d01010105000382010f0
			03082010a0282010100cd79c60c71f8c034c1c7a91af59a
			964254e15bad10c051767d912bde0b8faf612d6d28442e3
			57165203b3cb907c14da3c8c613f9003e95c256553b8cd8
			b865827300dc7af513bf74f282718566fdbc34234304a04
			fd7bc965488c291deb3a40413836c27a0d852d5b148dde9
			07a39cf251dc0ee272ac38853d9b76340c02b386bb066d8
			ef0d21f0956dbd8b42f7076303497b00e8303595862bb4d
			5e1b75ded5f95abe652592d7078d3cd2cb7e46062751596
			d7c3c00fb3dfa9e9991cd49cfce11d7be063e18504819d6
			0da3550697a1a6fa2c557bba91187d75749cd0c8a58c00a
			ff304aed787d1323c07cdf13fcc1d38883382dad329f8f0
			c868c98a6321eb0203010001
			""");
	final byte[] preMasterSecret = bytes("""
			03035f6030502ce275df0a4c136562e55ddb0f08365ea9e
			7c2b42550284a4bd9664b4cc893febc4844cfd6e231de53
			7b
			""");
	final byte[] clientRandom = bytes("""
			5c 48 6a 33 93 14 eb 56 d2 7d 86 07 56 83 80 95
			ff 1e 3c 32 b4 21 d0 70 ab e2 fb a0 6a b2 6a 3f
			""");
	final byte[] serverRandom = bytes("""
			5c 48 6a 36 a6 57 51 c6 13 a2 88 df 39 a0 42 02
			9c 7b 35 cb 4f 55 5d 91 3c 7f 89 e2 c6 21 e2 eb
			""");

	final byte[] clientHello = bytes("""
			01 00 00 4a 03 03 5c 48 6a 33 93 14 eb 56 d2 7d
			86 07 56 83 80 95 ff 1e 3c 32 b4 21 d0 70 ab e2
			fb a0 6a b2 6a 3f 00 00 02 00 3c 01 00 00 1f 00
			0d 00 16 00 14 06 03 06 01 05 03 05 01 04 03 04
			01 04 02 02 03 02 01 02 02 ff 01 00 01 00""");
	final byte[] serverHello = bytes("""
			02 00 00 4d 03 03 5c 48 6a 36 a6 57 51 c6 13 a2
			88 df 39 a0 42 02 9c 7b 35 cb 4f 55 5d 91 3c 7f
			89 e2 c6 21 e2 eb 20 5c 48 6a 36 14 88 53 20 1e
			0f ef 8a 7d 37 04 9e a6 7f 3c 41 1d 4e 32 fe f5
			1e 97 b9 f3 61 11 e5 00 3c 00 00 05 ff 01 00 01
			00""");
	final byte[] certificate = bytes("""
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
	final byte[] serverHelloDone = bytes("0e 00 00 00");
	final byte[] clientKeyExchange = bytes("""
			10 00 01 02 01 00 69 8d 0e 95 67 40 6b ae 0f cb
			7e c1 59 34 c4 93 ba a1 ae bb 64 e7 84 e3 54 bc
			1e ed 9c 8a 96 7a f1 51 58 56 28 9b 1a a8 fa a2
			e5 0b 2c 0c 20 36 37 55 9c 9d 5f 1a 76 19 18 6e
			65 f0 e5 88 19 99 04 b4 49 31 af ce 30 08 22 f5
			89 ed cf 32 5b 01 32 b8 76 7e 0d 27 98 f2 df 7e
			6c 88 5f 96 14 fd 4d ff e5 b6 0d 50 da e6 72 f3
			c9 18 52 10 e8 d9 c8 c6 78 03 b6 bf 07 11 60 95
			14 77 34 6d c0 c9 4c 5d 4c 25 25 78 68 70 62 3f
			a9 21 35 f5 f0 46 34 21 3a e0 63 56 a0 84 64 e5
			9e d4 b9 fe 1a c4 d4 6d 31 70 4f da ae c5 ee 70
			62 db 16 00 b7 f4 c0 3b ce ad 44 fd a6 1a eb 7f
			8b 1c 03 91 47 b9 15 69 5e 70 e8 6b d1 82 ae d8
			b6 76 c3 c5 ba 56 e2 87 ee bc 73 c4 3c 7a 67 8a
			2f 14 e4 ea d4 67 73 8b ea 9a 7c fb ac e4 a3 d9
			cf 4e 81 ba b4 0c 2d 87 02 fa 82 f6 f3 53 b6 f6
			45 26 26 1a 1d 02""");

	final byte[] clientFinished = bytes("""
			14 00 00 0c cc 4d 39 01 b6 55 af cd 8d b7 e5 c3
			""");
	final byte[] clientFinishedMAC = bytes("""
			b2 34 3e d5 ed b8 d0 4a ed 27 2e bd 70 fa ca a4
			1b 3f 30 ff b7 e3 f3 5a 4f 3d 37 b5 b1 4e 22 cc
			""");
	final byte[] clientFinishedEncrypted = bytes("""
			22 96 1f c2 56 c4 12 48 b0 91 b6 e5 7c b8 1f 0a
			40 e9 b6 ee b6 25 76 71 58 37 d4 37 f9 65 e7 a5
			80 94 bb b3 d8 78 c5 a9 c4 b8 29 94 27 da 82 41
			e8 22 d6 38 55 48 51 17 6c 8e e8 44 9e 6a 9c aa
			68 14 66 eb 95 7d 73 9d cc 47 ac ae 69 c9 b6 73
			""");

	final byte[] serverFinished = bytes("""
			14 00 00 0c b8 99 57 98 49 a6 31 54 43 e8 ed 0a
			""");
	final byte[] serverFinishedMAC = bytes("""
			6b e5 24 fa 24 a9 ed a0 4a 96 f1 6c a7 e0 8b 6f
			9b f0 1d db 65 31 e7 bd 52 07 fd fc 0b 69 6b 84
			""");
	final byte[] serverFinishedEncrypted = bytes("""
			13 d7 58 d3 6b 1b 48 b3 e0 a9 2d 62 e0 48 93 00
			1b db b8 1a 24 c0 8e 9d 68 44 a1 24 87 2a 12 e7
			5e 77 d9 5d d9 23 df 3e e6 56 c5 48 e4 6c a3 bf
			02 76 6c 4f 89 c2 02 f6 73 84 6c 3c 7c 32 cb fd
			5d 2c 80 2e b2 14 6c e6 59 39 c6 af 6f c3 57 57
			""");

	final byte[] request = bytes("""
			47 45 54 20 2f 20 48 54 54 50 2f 31 2e 31 0d 0a
			48 6f 73 74 3a 20 31 31 35 2e 32 38 2e 39 34 2e
			31 30 30 0d 0a 41 63 63 65 70 74 3a 20 74 65 78
			74 2f 68 74 6d 6c 0d 0a 41 63 63 65 70 74 2d 45
			6e 63 6f 64 69 6e 67 3a 20 67 7a 69 70 2c 20 64
			65 66 6c 61 74 65 0d 0a 43 6f 6e 6e 65 63 74 69
			6f 6e 3a 20 6b 65 65 70 2d 61 6c 69 76 65 0d 0a
			55 70 67 72 61 64 65 2d 49 6e 73 65 63 75 72 65
			2d 52 65 71 75 65 73 74 73 3a 20 31 0d 0a 43 6f
			6e 74 65 6e 74 2d 4c 65 6e 67 74 68 3a 20 30 0d
			0a 0d 0a
			""");
	final byte[] requestEncrypted = bytes("""
			a1 25 35 e9 f6 82 6b a1 8f 05 c6 84 bc c0 85 ed
			b2 b8 f3 0d 2a 4b 95 64 54 6c 54 e0 68 47 fc 57
			45 8f 25 6d a3 5a d6 a2 eb b4 42 a8 11 cf 5b 0d
			4a c1 b0 4f d6 40 75 44 ac 78 94 da ca 58 8d 2a
			12 a9 48 b6 4d 4c 4f 64 df 67 24 96 76 d6 b7 d2
			43 c3 ef 50 60 0a cf 3c ba a5 0e 64 4b 4d da ad
			1b 3d f3 e0 ed 9d b1 e4 ae 7b 2b 40 58 0f 74 32
			0c ee 69 34 35 e2 45 9f 2f 62 aa d8 e8 02 00 34
			63 bd ef 68 bc a4 0c 3a 56 38 ca 9c e4 9a 76 1c
			28 63 60 d4 2e a6 da aa 4f 30 b0 2d a1 83 83 21
			b0 07 13 20 bb 54 4c 42 35 fa b7 86 f6 3f fd df
			ee 1d 8d 0d 1b 23 e7 14 73 bd 30 ce 79 9b db d5
			c2 71 e8 06 fa 4a 2b 2a d0 d1 8e 76 5a 61 75 5f
			f0 04 44 ad 36 63 65 48 6d 7b d3 70 bb 36 69 03
			""");

	final byte[] response = bytes("""
			48 54 54 50 2f 31 2e 31 20 34 30 34 20 4e 6f 74
			20 46 6f 75 6e 64 0d 0a 43 6f 6e 74 65 6e 74 2d
			54 79 70 65 3a 20 74 65 78 74 2f 68 74 6d 6c 0d
			0a 43 6f 6e 74 65 6e 74 2d 4c 65 6e 67 74 68 3a
			20 35 34 34 0d 0a 53 65 72 76 65 72 3a 20 4a 65
			74 74 79 28 39 2e 34 2e 36 2e 76 32 30 31 37 30
			35 33 31 29 0d 0a 0d 0a 3c 48 54 4d 4c 3e 0a 3c
			48 45 41 44 3e 0a 3c 54 49 54 4c 45 3e 45 72 72
			6f 72 20 34 30 34 20 2d 20 4e 6f 74 20 46 6f 75
			6e 64 3c 2f 54 49 54 4c 45 3e 0a 3c 42 4f 44 59
			3e 0a 3c 48 32 3e 45 72 72 6f 72 20 34 30 34 20
			2d 20 4e 6f 74 20 46 6f 75 6e 64 2e 3c 2f 48 32
			3e 0a 4e 6f 20 63 6f 6e 74 65 78 74 20 6f 6e 20
			74 68 69 73 20 73 65 72 76 65 72 20 6d 61 74 63
			68 65 64 20 6f 72 20 68 61 6e 64 6c 65 64 20 74
			68 69 73 20 72 65 71 75 65 73 74 2e 3c 42 52 3e
			43 6f 6e 74 65 78 74 73 20 6b 6e 6f 77 6e 20 74
			6f 20 74 68 69 73 20 73 65 72 76 65 72 20 61 72
			65 3a 20 3c 75 6c 3e 3c 6c 69 3e 3c 61 20 68 72
			65 66 3d 22 2f 65 71 75 65 73 22 3e 2f 65 71 75
			65 73 26 6e 62 73 70 3b 2d 2d 2d 3e 26 6e 62 73
			70 3b 6f 2e 65 2e 6a 2e 77 2e 57 65 62 41 70 70
			43 6f 6e 74 65 78 74 40 37 61 35 32 66 32 61 32
			7b 2f 65 71 75 65 73 2c 66 69 6c 65 3a 2f 2f 2f
			68 6f 6d 65 2f 69 63 76 73 73 2d 70 72 6f 6a 65
			63 74 2f 6a 65 74 74 79 2f 6a 65 74 74 79 2d 62
			61 73 65 2f 77 65 62 61 70 70 73 2f 65 71 75 65
			73 2f 2c 41 56 41 49 4c 41 42 4c 45 7d 7b 2f 65
			71 75 65 73 7d 3c 2f 61 3e 3c 2f 6c 69 3e 0a 3c
			2f 75 6c 3e 3c 68 72 3e 3c 61 20 68 72 65 66 3d
			22 68 74 74 70 3a 2f 2f 65 63 6c 69 70 73 65 2e
			6f 72 67 2f 6a 65 74 74 79 22 3e 3c 69 6d 67 20
			62 6f 72 64 65 72 3d 30 20 73 72 63 3d 22 2f 66
			61 76 69 63 6f 6e 2e 69 63 6f 22 2f 3e 3c 2f 61
			3e 26 6e 62 73 70 3b 3c 61 20 68 72 65 66 3d 22
			68 74 74 70 3a 2f 2f 65 63 6c 69 70 73 65 2e 6f
			72 67 2f 6a 65 74 74 79 22 3e 50 6f 77 65 72 65
			64 20 62 79 20 4a 65 74 74 79 3a 2f 2f 20 39 2e
			34 2e 36 2e 76 32 30 31 37 30 35 33 31 3c 2f 61
			3e 3c 68 72 2f 3e 0a 0a 3c 2f 42 4f 44 59 3e 0a
			3c 2f 48 54 4d 4c 3e 0a
			""");
	final byte[] responseEncrypted = bytes("""
			de 79 1f 4f 91 2d 93 49 a4 a1 5e bd 76 a5 4a 17
			75 e8 2e 30 78 f6 bb 1e 6f 6d a3 8f 39 06 ea 52
			82 1c f1 a7 45 ab fa 7d 2a f5 26 cf 80 6b dc 92
			8b 5d 45 a4 e3 1c df 6e e7 67 e8 54 a3 e9 a4 c1
			8c 93 6a ed a5 7f 67 bb 90 96 84 fc 08 c2 16 be
			9e c7 ec e9 b3 0e 5b 05 bf 14 a4 78 21 b3 fc 1b
			97 1d 38 ee ca 14 b0 67 81 8e 5f 3a 41 f9 03 5a
			36 70 4b c0 c8 d1 a2 b4 ac b6 b0 1a c9 e2 6e 03
			00 92 87 6d d2 63 bb c3 e2 4e 42 5a e9 f3 99 b2
			49 6b 14 ca 4e f3 4e 2d b0 78 46 a5 0d 25 e8 29
			3e b3 ba dc e6 e1 8a 2d d1 ba 73 5f ac 5b 60 05
			8a 05 9b bf 84 07 87 c7 7f 6f fd fd a0 2d a4 3a
			13 c3 35 59 67 89 b9 22 5e a8 ee b2 a8 42 fa d5
			8d 67 6b 53 11 16 0d 5d 62 0f fa f3 4f ed e9 f3
			cd 21 70 87 54 a9 a3 85 45 ec c1 61 a7 1f d7 a9
			1f 51 6c c8 93 b6 70 52 15 aa 9b d4 91 f7 1c 6e
			e5 a4 e3 cd 63 d2 fd 19 ed 41 2b aa 98 8b 7d ba
			0e 5b d4 a3 19 b8 f8 71 1c e5 ce 65 eb 95 aa 6e
			02 48 55 0e 2e f1 7e 95 af d3 71 5b a6 27 38 4c
			90 7d d8 97 50 fa ec cd 25 c4 71 04 9d 84 c2 4f
			a6 90 25 ec 33 65 54 27 58 a0 c1 48 74 7c 04 40
			35 37 4b 37 b9 0e f0 e0 29 75 67 11 cc 22 26 1b
			d4 20 be 55 67 21 c9 b9 db f9 e7 5f 14 cb 3a 30
			3d 04 02 37 61 a1 fc 43 7a 7b 98 3f b3 6d c6 88
			35 e7 62 b5 8e ba e8 03 20 fc f9 18 d6 26 8a 11
			7b b1 47 05 47 3b 83 d4 6f 8e bc 34 75 76 a0 33
			46 55 fd 16 2b 79 99 78 91 93 4c 85 1e b4 c4 50
			f1 cf c3 50 0a 8b b6 9c ce 59 37 5a fb 11 3e 15
			44 93 c1 3f 21 68 ed ab d7 10 92 67 9d 29 64 4c
			1f 2c 3e b2 00 1a 83 52 d9 56 6d 9a ff 66 af a6
			62 00 84 e1 68 67 94 e0 d4 ea a7 12 ac a2 60 c7
			04 55 20 42 9d 37 7e 3c dd 3b 24 ba c1 33 d0 b2
			bc 71 90 97 8b 2e c4 cc 3f 84 98 e0 05 8e 03 7c
			3c 60 2d 9a ac 2f 71 13 97 b6 2f 0e ef ee eb 60
			e8 a7 b6 51 fb 5c bb da e0 6b e6 63 d3 e7 e8 2b
			e1 ff ac 1b 67 18 29 8b 3a 3e 39 2d 8d e3 41 e8
			d1 83 4b d2 22 b2 f6 49 99 9b 7d 93 ab 75 89 c2
			8c 05 d7 d8 c3 b6 8e 52 75 08 cf e5 74 aa 0f a5
			26 9d 3a 3c 0c 86 d5 3f 63 67 43 66 12 a6 23 7f
			44 99 ee 58 8a 56 b0 cf c7 66 d1 42 8b 6e 42 07
			93 16 c7 0f 77 1a 76 8d f1 f0 b3 24 92 a1 59 fb
			58 07 8c d3 96 6c 32 a8 da 26 af 6d 15 7f 21 14
			8c 7e d9 82 92 fa 13 66 a6 11 d6 0f b9 b6 d5 83
			f2 83 77 b8 c2 fe f5 ee a9 cb dc 0f d4 ef 98 e0
			""");

	@Test
	void test() throws Exception {
		final CipherSuiteType type = CipherSuiteType.TLS_RSA_WITH_AES_128_CBC_SHA256;
		final V2SecretCache clientSecret = new V2SecretCache();
		final V2SecretCache serverSecret = new V2SecretCache();

		clientSecret.initialize(type);
		serverSecret.initialize(type);

		clientSecret.clientRandom(clientRandom);
		clientSecret.serverRandom(serverRandom);

		serverSecret.clientRandom(clientRandom);
		serverSecret.serverRandom(serverRandom);

		clientSecret.pms(preMasterSecret);
		clientSecret.masterSecret();
		clientSecret.keyBlock(type);

		serverSecret.pms(preMasterSecret);
		serverSecret.masterSecret();
		serverSecret.keyBlock(type);

		// 验证密钥导出

		final byte[] master = bytes("""
				e1 e6 95 01 1e 6e 42 9b 4d 20 a1 a1 fa c7 cc 67
				df cc 53 55 1d 26 37 53 17 7f 09 66 4a 18 a8 b3
				6e bd 5f 51 94 dc fc 1e 2f 1b 15 36 98 7a 6a d7
				""");
		assertArrayEquals(clientSecret.master(), master);
		assertArrayEquals(serverSecret.master(), master);
		final byte[] block = bytes("""
				30 5e 8f 6c bd 27 12 67 b1 eb 37 28 af 4d 91 d2
				92 cb a2 55 7f 99 96 49 db df 7f 78 f7 a6 7d a6
				1d 76 90 54 64 c9 84 3c 16 58 6a 07 af e1 3f 8c
				92 c3 22 41 2f 47 04 75 37 b3 ba 09 2f 27 16 25
				bd c9 9e 7e eb 6a 3c c5 48 2e 6d 4c 42 92 d4 9a
				08 50 41 8f e7 a3 d8 ff 56 c4 7f c4 75 57 24 eb
				a6 33 f4 01 98 7e db fe 58 74 f9 56 0b 9a e5 02
				43 41 9d d9 e4 3d fa 81 97 4e ca 88 3b 79 b5 fd
								""");
		assertArrayEquals(clientSecret.block(), block);
		assertArrayEquals(serverSecret.block(), block);

		assertArrayEquals(serverSecret.clientWriteMACKey(type), clientSecret.clientWriteMACKey(type));
		assertArrayEquals(serverSecret.serverWriteMACKey(type), clientSecret.serverWriteMACKey(type));
		assertArrayEquals(serverSecret.clientWriteKey(type), clientSecret.clientWriteKey(type));
		assertArrayEquals(serverSecret.serverWriteKey(type), clientSecret.serverWriteKey(type));
		assertArrayEquals(serverSecret.clientWriteIV(type), clientSecret.clientWriteIV(type));
		assertArrayEquals(serverSecret.serverWriteIV(type), clientSecret.serverWriteIV(type));

		final byte[] clientWriteKey = bytes("bdc99e7eeb6a3cc5482e6d4c4292d49a");
		assertArrayEquals(clientSecret.clientWriteKey(type), clientWriteKey);
		assertArrayEquals(serverSecret.clientWriteKey(type), clientWriteKey);

		final DataBuffer buffer1 = DataBuffer.instance();
		buffer1.write(clientSecret.block());
		final DataBuffer buffer2 = DataBuffer.instance();
		buffer2.write(clientSecret.clientWriteMACKey(type));
		buffer2.write(clientSecret.serverWriteMACKey(type));
		buffer2.write(clientSecret.clientWriteKey(type));
		buffer2.write(clientSecret.serverWriteKey(type));
		buffer2.write(clientSecret.clientWriteIV(type));
		buffer2.write(clientSecret.serverWriteIV(type));
		assertEquals(buffer1, buffer2);

		// 设置加解密套件

		final V2CipherSuiter client = new V2CipherSuiter();
		final V2CipherSuiter server = new V2CipherSuiter();

		client.initialize(type);
		server.initialize(type);

		client.encryptReset(clientSecret.clientWriteKey(type), clientSecret.clientWriteIV(type));
		client.encryptMACKey(clientSecret.clientWriteMACKey(type));

		client.decryptReset(clientSecret.serverWriteKey(type), clientSecret.serverWriteIV(type));
		client.decryptMACKey(clientSecret.serverWriteMACKey(type));

		server.encryptReset(serverSecret.serverWriteKey(type), serverSecret.serverWriteIV(type));
		server.encryptMACKey(serverSecret.serverWriteMACKey(type));

		server.decryptReset(serverSecret.clientWriteKey(type), serverSecret.clientWriteIV(type));
		server.decryptMACKey(serverSecret.clientWriteMACKey(type));

		// HASH

		clientSecret.hash(clientHello);
		clientSecret.hash(serverHello);
		clientSecret.hash(certificate);
		clientSecret.hash(serverHelloDone);
		clientSecret.hash(clientKeyExchange);

		serverSecret.hash(clientHello);
		serverSecret.hash(serverHello);
		serverSecret.hash(certificate);
		serverSecret.hash(serverHelloDone);
		serverSecret.hash(clientKeyExchange);

		// 验证FinishedVerfyData
		byte[] verifyData = bytes("cc 4d 39 01 b6 55 af cd 8d b7 e5 c3");
		assertArrayEquals(clientSecret.clientFinished(), verifyData);
		assertArrayEquals(serverSecret.clientFinished(), verifyData);

		// ClientFinished sequence:0

		byte[] iv = bytes("22 96 1f c2 56 c4 12 48 b0 91 b6 e5 7c b8 1f 0a");
		byte[] pd = bytes("0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f");

		buffer1.clear();
		buffer1.write(clientFinished);
		byte[] clientMAC = client.encryptMAC(Record.HANDSHAKE, TLS.V12, buffer1, buffer1.readable());
		assertArrayEquals(clientFinishedMAC, clientMAC);

		// Encrypt
		buffer2.clear();
		buffer2.write(iv);
		client.encryptBlock(iv);
		client.encryptUpdate(buffer1, buffer2, buffer1.readable());
		client.encryptUpdate(clientMAC, buffer2);
		client.encryptUpdate(pd, buffer2);
		client.encryptFinal(buffer2);

		buffer1.write(clientFinishedEncrypted);
		assertEquals(buffer1, buffer2);

		assertEquals(client.encryptSequence(), 1);
		assertEquals(client.decryptSequence(), 0);

		// Decrypt
		buffer1.readFully(iv);
		buffer2.clear();
		server.decryptBlock(iv);
		server.decryptFinal(buffer1, buffer2);

		buffer2.backSkip(buffer2.backByte());
		buffer2.backSkip(server.macLength());
		byte[] serverMAC = server.decryptMAC(Record.HANDSHAKE, TLS.V12, buffer2, buffer2.readable());
		assertArrayEquals(clientFinishedMAC, serverMAC);

		buffer1.write(clientFinished);
		assertEquals(buffer1, buffer2);

		assertEquals(server.encryptSequence(), 0);
		assertEquals(server.decryptSequence(), 1);

		// ServerFinished sequence:1

		clientSecret.hash(clientFinished);
		serverSecret.hash(clientFinished);

		// 验证FinishedVerfyData
		verifyData = bytes("b8 99 57 98 49 a6 31 54 43 e8 ed 0a");
		assertArrayEquals(clientSecret.serverFinished(), verifyData);
		assertArrayEquals(serverSecret.serverFinished(), verifyData);

		iv = bytes("13 d7 58 d3 6b 1b 48 b3 e0 a9 2d 62 e0 48 93 00");
		pd = bytes("0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f 0f");

		buffer1.clear();
		buffer1.write(serverFinished);
		serverMAC = server.encryptMAC(Record.HANDSHAKE, TLS.V12, buffer1, buffer1.readable());
		assertArrayEquals(serverFinishedMAC, serverMAC);

		// Encrypt
		buffer2.clear();
		buffer2.write(iv);
		server.encryptBlock(iv);
		server.encryptUpdate(buffer1, buffer2, buffer1.readable());
		server.encryptUpdate(serverMAC, buffer2);
		server.encryptUpdate(pd, buffer2);
		server.encryptFinal(buffer2);

		buffer1.write(serverFinishedEncrypted);
		assertEquals(buffer1, buffer2);

		assertEquals(server.encryptSequence(), 1);
		assertEquals(server.decryptSequence(), 1);

		// Decrypt
		buffer1.readFully(iv);
		buffer2.clear();
		client.decryptBlock(iv);
		client.decryptFinal(buffer1, buffer2);

		buffer2.backSkip(buffer2.backByte());
		buffer2.backSkip(client.macLength());
		clientMAC = client.decryptMAC(Record.HANDSHAKE, TLS.V12, buffer2, buffer2.readable());
		assertArrayEquals(serverFinishedMAC, clientMAC);

		buffer1.write(serverFinished);
		assertEquals(buffer1, buffer2);

		assertEquals(client.encryptSequence(), 1);
		assertEquals(client.decryptSequence(), 1);

		// REQUEST

		iv = bytes("a1 25 35 e9 f6 82 6b a1 8f 05 c6 84 bc c0 85 ed");
		pd = bytes("0c 0c 0c 0c 0c 0c 0c 0c 0c 0c 0c 0c 0c");

		// Encrypt
		buffer1.clear();
		buffer1.write(request);
		clientMAC = client.encryptMAC(Record.APPLICATION_DATA, TLS.V12, buffer1, buffer1.readable());

		buffer2.clear();
		buffer2.write(iv);
		client.encryptBlock(iv);
		client.encryptUpdate(buffer1, buffer2, buffer1.readable());
		client.encryptUpdate(clientMAC, buffer2);
		client.encryptUpdate(pd, buffer2);
		client.encryptFinal(buffer2);

		buffer1.write(requestEncrypted);
		assertEquals(buffer1, buffer2);

		assertEquals(client.encryptSequence(), 2);
		assertEquals(client.decryptSequence(), 1);

		// Decrypt
		buffer1.readFully(iv);
		buffer2.clear();
		server.decryptBlock(iv);
		server.decryptFinal(buffer1, buffer2);

		buffer2.backSkip(buffer2.backByte());
		buffer2.backSkip(server.macLength());
		serverMAC = server.decryptMAC(Record.APPLICATION_DATA, TLS.V12, buffer2, buffer2.readable());

		buffer1.write(request);
		assertEquals(buffer1, buffer2);

		assertEquals(server.encryptSequence(), 1);
		assertEquals(server.decryptSequence(), 2);
		assertArrayEquals(clientMAC, serverMAC);

		// RESPONSE

		iv = bytes("de 79 1f 4f 91 2d 93 49 a4 a1 5e bd 76 a5 4a 17");
		pd = bytes("07 07 07 07 07 07 07 07");

		// Encrypt
		buffer1.clear();
		buffer1.write(response);
		serverMAC = server.encryptMAC(Record.APPLICATION_DATA, TLS.V12, buffer1, buffer1.readable());

		buffer2.clear();
		buffer2.write(iv);
		server.encryptBlock(iv);
		server.encryptUpdate(buffer1, buffer2, buffer1.readable());
		server.encryptUpdate(serverMAC, buffer2);
		server.encryptUpdate(pd, buffer2);
		server.encryptFinal(buffer2);

		buffer1.write(responseEncrypted);
		assertEquals(buffer1, buffer2);

		assertEquals(server.encryptSequence(), 2);
		assertEquals(server.decryptSequence(), 2);

		// Decrypt
		buffer1.readFully(iv);
		buffer2.clear();
		client.decryptBlock(iv);
		client.decryptFinal(buffer1, buffer2);

		buffer2.backSkip(buffer2.backByte());
		buffer2.backSkip(client.macLength());
		clientMAC = client.decryptMAC(Record.APPLICATION_DATA, TLS.V12, buffer2, buffer2.readable());

		buffer1.write(response);
		assertEquals(buffer1, buffer2);

		assertEquals(client.encryptSequence(), 2);
		assertEquals(client.decryptSequence(), 2);
		assertArrayEquals(clientMAC, serverMAC);

		// ALERT

		buffer1.clear();
		buffer2.clear();

		Alert alert = new Alert(Alert.CLOSE_NOTIFY);
		RecordCoder.encode(alert, buffer1);
		RecordCoder.encodeBlock(client, alert, buffer1, buffer2, TLS.V12);

		RecordCoder.decodeBlock(server, buffer2, buffer1);
		alert = RecordCoder.decodeAlert(buffer1);

		assertEquals(alert.getDescription(), Alert.CLOSE_NOTIFY);
	}

	@Test
	void testPMSExchange() throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		buffer.write(clientKeyExchange);
		final Handshake handshake = HandshakeCoder.decodeV2(buffer);
		final ClientKeyExchange cke = (ClientKeyExchange) handshake;

		final Signaturer signaturer = new Signaturer(SignatureScheme.RSA_PKCS1_SHA256);
		signaturer.setPrivateKey(getPrivateKey());
		signaturer.setPublicKey(getPublicKey());

		final byte[] pms = signaturer.decryptPKCS1(cke.get());
		// System.out.println(Utility.hex(pms));
		assertArrayEquals(preMasterSecret, pms);

		byte[] epms = signaturer.encryptPKCS1(pms);
		epms = signaturer.decryptPKCS1(epms);
		assertArrayEquals(epms, pms);

		epms = signaturer.encryptOAEP(pms);
		epms = signaturer.decryptOAEP(epms);
		assertArrayEquals(epms, pms);
	}

	PrivateKey getPrivateKey() throws Exception {
		final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(serverPrivateKey);
		final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	PublicKey getPublicKey() throws Exception {
		final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(serverPublicKey);
		final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(keySpec);
	}
}