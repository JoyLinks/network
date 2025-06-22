/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBinary {

	@Test
	void testByte() {
		byte value = 0;
		for (int index = 0; index < 8; index++) {
			value = Binary.setBit(value, true, index);
		}
		assertEquals(value, -1);

		for (int index = 0; index < 8; index++) {
			assertEquals(true, Binary.getBit(value, index));
		}

		value = Binary.setBit(value, false, 7);
		assertEquals(value, Byte.MAX_VALUE);

		for (int index = 0; index < 8; index++) {
			value = Binary.setBit(value, false, index);
		}
		assertEquals(value, 0);

		assertEquals(Binary.get4BL(Byte.MAX_VALUE), 0B1111);
		assertEquals(Binary.get4BM(Byte.MAX_VALUE), 0B0111);
		assertEquals(Binary.get4BL(Byte.MIN_VALUE), 0B0000);
		assertEquals(Binary.get4BM(Byte.MIN_VALUE), 0B1000);

		value = Binary.set4BL(value, (byte) 0B1111);
		value = Binary.set4BM(value, (byte) 0B0111);
		assertEquals(Byte.MAX_VALUE, value);
	}

	@Test
	void testShort() {
		short value = 0;
		for (int index = 0; index < 16; index++) {
			value = Binary.setBit(value, true, index);
		}
		assertEquals(value, -1);

		for (int index = 0; index < 16; index++) {
			assertEquals(true, Binary.getBit(value, index));
		}

		value = Binary.setBit(value, false, 15);
		assertEquals(value, Short.MAX_VALUE);

		for (int index = 0; index < 16; index++) {
			value = Binary.setBit(value, false, index);
		}
		assertEquals(value, 0);

		value = Binary.setByte(value, (byte) 0x7F, 1);
		value = Binary.setByte(value, (byte) 0xFF, 0);
		assertEquals(Short.MAX_VALUE, value);

		assertEquals(Binary.getByte(value, 1), (byte) 0x7F);
		assertEquals(Binary.getByte(value, 0), (byte) 0xFF);

		assertEquals(Binary.join((byte) 0x80, (byte) 0x00), Short.MIN_VALUE);

		byte[] item = Binary.split(value);
		assertEquals(item[0], (byte) 0x7F);
		assertEquals(item[1], (byte) 0xFF);

		Binary.put(item, 0, Short.MIN_VALUE);
		value = Binary.getShort(item, 0);
		assertEquals(Short.MIN_VALUE, value);
	}

	@Test
	void testInteger() {
		int value = 0;
		for (int index = 0; index < 32; index++) {
			value = Binary.setBit(value, true, index);
		}
		assertEquals(value, -1);

		for (int index = 0; index < 32; index++) {
			assertEquals(true, Binary.getBit(value, index));
		}

		value = Binary.setBit(value, false, 31);
		assertEquals(value, Integer.MAX_VALUE);

		for (int index = 0; index < 32; index++) {
			value = Binary.setBit(value, false, index);
		}
		assertEquals(value, 0);

		assertEquals(Binary.getByte(Integer.MAX_VALUE, 3), (byte) 0x7F);
		assertEquals(Binary.getByte(Integer.MAX_VALUE, 2), (byte) 0xFF);
		assertEquals(Binary.getByte(Integer.MAX_VALUE, 1), (byte) 0xFF);
		assertEquals(Binary.getByte(Integer.MAX_VALUE, 0), (byte) 0xFF);

		value = Binary.setByte(value, (byte) 0x7F, 3);
		value = Binary.setByte(value, (byte) 0xFF, 2);
		value = Binary.setByte(value, (byte) 0xFF, 1);
		value = Binary.setByte(value, (byte) 0xFF, 0);
		assertEquals(Integer.MAX_VALUE, value);

		assertEquals(Binary.join((byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00), Integer.MIN_VALUE);

		byte[] item = Binary.split(value);
		assertEquals(item[0], (byte) 0x7F);
		assertEquals(item[1], (byte) 0xFF);
		assertEquals(item[2], (byte) 0xFF);
		assertEquals(item[3], (byte) 0xFF);

		Binary.put(item, 0, Integer.MIN_VALUE);
		value = Binary.getInteger(item, 0);
		assertEquals(Integer.MIN_VALUE, value);
	}

	@Test
	void testLong() {
		long value = 0;
		for (int index = 0; index < 64; index++) {
			value = Binary.setBit(value, true, index);
		}
		assertEquals(value, -1);

		for (int index = 0; index < 64; index++) {
			assertEquals(true, Binary.getBit(value, index));
		}

		value = Binary.setBit(value, false, 63);
		assertEquals(value, Long.MAX_VALUE);

		for (int index = 0; index < 64; index++) {
			value = Binary.setBit(value, false, index);
		}
		assertEquals(value, 0);

		assertEquals(Binary.getByte(Long.MAX_VALUE, 7), (byte) 0x7F);
		assertEquals(Binary.getByte(Long.MAX_VALUE, 6), (byte) 0xFF);
		assertEquals(Binary.getByte(Long.MAX_VALUE, 5), (byte) 0xFF);
		assertEquals(Binary.getByte(Long.MAX_VALUE, 4), (byte) 0xFF);
		assertEquals(Binary.getByte(Long.MAX_VALUE, 3), (byte) 0xFF);
		assertEquals(Binary.getByte(Long.MAX_VALUE, 2), (byte) 0xFF);
		assertEquals(Binary.getByte(Long.MAX_VALUE, 1), (byte) 0xFF);
		assertEquals(Binary.getByte(Long.MAX_VALUE, 0), (byte) 0xFF);

		value = Binary.setByte(value, (byte) 0x7F, 7);
		value = Binary.setByte(value, (byte) 0xFF, 6);
		value = Binary.setByte(value, (byte) 0xFF, 5);
		value = Binary.setByte(value, (byte) 0xFF, 4);
		value = Binary.setByte(value, (byte) 0xFF, 3);
		value = Binary.setByte(value, (byte) 0xFF, 2);
		value = Binary.setByte(value, (byte) 0xFF, 1);
		value = Binary.setByte(value, (byte) 0xFF, 0);
		assertEquals(Long.MAX_VALUE, value);

		assertEquals(Binary.join((byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00), Long.MIN_VALUE);

		byte[] item = Binary.split(value);
		assertEquals(item[0], (byte) 0x7F);
		assertEquals(item[1], (byte) 0xFF);
		assertEquals(item[2], (byte) 0xFF);
		assertEquals(item[3], (byte) 0xFF);
		assertEquals(item[4], (byte) 0xFF);
		assertEquals(item[5], (byte) 0xFF);
		assertEquals(item[6], (byte) 0xFF);
		assertEquals(item[7], (byte) 0xFF);

		Binary.put(item, 0, Long.MIN_VALUE);
		value = Binary.getLong(item, 0);
		assertEquals(Long.MIN_VALUE, value);
	}
}