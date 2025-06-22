/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.verifies;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;

/**
 * TEST verifies
 * 
 * @author ZhangXi 2023年9月15日
 */
class TestVerifies {

	static DataBuffer buffer = DataBuffer.instance();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		for (int index = 0; index < 256; index++) {
			buffer.write(index);
		}
		// System.out.println(buffer);
		// http://www.ip33.com/crc.html
		// 使命：用信息技术助力中国制造腾飞
		// 愿景：成为最具竞争力的国产工业软件公司
		// 价值观：深入，细致，突破
		// 战略定位：
		// 业务布局：
		/*-
		 * 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13 14 15 16 17 18 19 1A 1B 1C 1D 1E 1F 20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F 30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E 3F 40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F 60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 71 72 73 74 75 76 77 78 79 7A 7B 7C 7D 7E 7F 80 81 82 83 84 85 86 87 88 89 8A 8B 8C 8D 8E 8F 90 91 92 93 94 95 96 97 98 99 9A 9B 9C 9D 9E 9F A0 A1 A2 A3 A4 A5 A6 A7 A8 A9 AA AB AC AD AE AF B0 B1 B2 B3 B4 B5 B6 B7 B8 B9 BA BB BC BD BE BF C0 C1 C2 C3 C4 C5 C6 C7 C8 C9 CA CB CC CD CE CF D0 D1 D2 D3 D4 D5 D6 D7 D8 D9 DA DB DC DD DE DF E0 E1 E2 E3 E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF F0 F1 F2 F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF
		 */
	}

	@AfterEach
	void tearDown() throws Exception {
		buffer.clear();
	}

	@Test
	void testBCC() {
		buffer.setVerifier(new BCC());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x00);
	}

	@Test
	void testLRC() {
		buffer.setVerifier(new LRC());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x80);
	}

	@Test
	void testCRC16_CCITT_FALSE() {
		buffer.setVerifier(new CRC16_CCITT_FALSE());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x3FBD);
	}

	void testCRC16_CCITT() {
		buffer.setVerifier(new CRC16_CCITT());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0xD841);
	}

	void testCRC16_DNP() {
		buffer.setVerifier(new CRC16_DNP());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x4472);
	}

	void testCRC16_IBM() {
		buffer.setVerifier(new CRC16_IBM());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0xBAD3);
	}

	void testCRC16_MAXIM() {
		buffer.setVerifier(new CRC16_MAXIM());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x452C);
	}

	@Test
	void testCRC16_MODBUS() {
		buffer.setVerifier(new CRC16_MODBUS());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0xDE6C);
	}

	void testCRC16_USB() {
		buffer.setVerifier(new CRC16_USB());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x2193);
	}

	void testCRC16_X25() {
		buffer.setVerifier(new CRC16_X25());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x303C);
	}

	@Test
	void testCRC16_XMODEM() {
		buffer.setVerifier(new CRC16_XMODEM());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x7E55);
	}

	void testCRC16() {
		buffer.setVerifier(new CRC16());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x0000);
	}

	@Test
	void testCRC32_MPEG_2() {
		buffer.setVerifier(new CRC32_MPEG_2());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x494A116A);
	}

	void testCRC32() {
		buffer.setVerifier(new CRC32());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x29058C73);
	}

	@Test
	void testCRC4_ITU() {
		buffer.setVerifier(new CRC4_ITU());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x5);
	}

	@Test
	void testCRC5_EPC() {
		buffer.setVerifier(new CRC5_EPC());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x02);
	}

	@Test
	void testCRC5_ITU() {
		buffer.setVerifier(new CRC5_ITU());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x05);
	}

	@Test
	void testCRC5_USB() {
		buffer.setVerifier(new CRC5_USB());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x08);
	}

	@Test
	void testCRC6_ITU() {
		buffer.setVerifier(new CRC6_ITU());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x2D);
	}

	@Test
	void testCRC7_MMC() {
		buffer.setVerifier(new CRC7_MMC());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x78);
	}

	@Test
	void testCRC8_ITU() {
		buffer.setVerifier(new CRC8_ITU());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x41);
	}

	@Test
	void testCRC8_MAXIM() {
		buffer.setVerifier(new CRC8_MAXIM());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x18);
	}

	void testCRC8_ROHC() {
		buffer.setVerifier(new CRC8_ROHC());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x8E);
	}

	@Test
	void testCRC8() {
		buffer.setVerifier(new CRC8());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x14);
	}

	void testCS() {
		buffer.setVerifier(new CS());
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x00);
	}
}