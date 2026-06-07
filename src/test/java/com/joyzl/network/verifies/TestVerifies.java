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
		final Verifier verifier = new BCC();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x00);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x00);
	}

	@Test
	void testLRC() {
		final Verifier verifier = new LRC();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x80);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x80);
	}

	@Test
	void testCRC16_CCITT_FALSE() {
		final Verifier verifier = new CRC16_CCITT_FALSE();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x3FBD);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x3FBD);
	}

	@Test
	void testCRC16_CCITT() {
		final Verifier verifier = new CRC16_CCITT();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0xD841);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0xD841);
	}

	@Test
	void testCRC16_DNP() {
		final Verifier verifier = new CRC16_DNP();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x4472);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x4472);
	}

	@Test
	void testCRC16_IBM() {
		final Verifier verifier = new CRC16_IBM();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0xBAD3);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0xBAD3);
	}

	@Test
	void testCRC16_MAXIM() {
		final Verifier verifier = new CRC16_MAXIM();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x452C);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x452C);
	}

	@Test
	void testCRC16_MODBUS() {
		final Verifier verifier = new CRC16_MODBUS();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0xDE6C);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0xDE6C);
	}

	@Test
	void testCRC16_USB() {
		final Verifier verifier = new CRC16_USB();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x2193);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x2193);
	}

	@Test
	void testCRC16_X25() {
		final Verifier verifier = new CRC16_X25();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x303C);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x303C);
	}

	@Test
	void testCRC16_XMODEM() {
		final Verifier verifier = new CRC16_XMODEM();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x7E55);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x7E55);
	}

	void testCRC16_LSB() {
		final Verifier verifier = new CRC16_LSB();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x0000);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x0000);
	}

	@Test
	void testCRC32_MPEG_2() {
		final Verifier verifier = new CRC32_MPEG_2();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x494A116A);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x494A116A);
	}

	@Test
	void testCRC32() {
		final Verifier verifier = new CRC32();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x29058C73);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x29058C73);
	}

	@Test
	void testCRC4_ITU() {
		final Verifier verifier = new CRC4_ITU();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x5);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x5);
	}

	@Test
	void testCRC5_EPC() {
		final Verifier verifier = new CRC5_EPC();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x02);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x02);
	}

	@Test
	void testCRC5_ITU() {
		final Verifier verifier = new CRC5_ITU();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x05);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x05);
	}

	@Test
	void testCRC5_USB() {
		final Verifier verifier = new CRC5_USB();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x08);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x08);
	}

	@Test
	void testCRC6_ITU() {
		final Verifier verifier = new CRC6_ITU();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x2D);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x2D);
	}

	@Test
	void testCRC7_MMC() {
		final Verifier verifier = new CRC7_MMC();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x78);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x78);
	}

	@Test
	void testCRC8_ITU() {
		final Verifier verifier = new CRC8_ITU();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x41);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x41);
	}

	@Test
	void testCRC8_MAXIM() {
		final Verifier verifier = new CRC8_MAXIM();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x18);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x18);
	}

	@Test
	void testCRC8_ROHC() {
		final Verifier verifier = new CRC8_ROHC();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x8E);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x8E);
	}

	@Test
	void testCRC8() {
		final Verifier verifier = new CRC8();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x14);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x14);
	}

	@Test
	void testCS() {
		final Verifier verifier = new CS();
		buffer.verify(verifier);
		final int value = verifier.value();
		assertEquals(value, 0x80);

		verifier.reset();
		buffer.setVerifier(verifier);
		while (buffer.readable() > 0) {
			buffer.readByte();
		}
		assertEquals(buffer.getVerifier().value(), 0x80);
	}
}