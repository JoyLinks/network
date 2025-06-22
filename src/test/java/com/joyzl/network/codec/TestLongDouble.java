/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.codec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestLongDouble {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		// final NumberFormat format = NumberFormat.getInstance();
		// format.setGroupingUsed(false);
		// format.setMaximumFractionDigits(10);
		// format.setMinimumFractionDigits(1);
		//
		// final DataBuffer buffer = DataBuffer.getB2048();
		// buffer.writeDouble(123.456789D);
		// System.out.println("double:" + double64(buffer.readLong()));
		//
		// buffer.writeDouble(-123.456789D);
		// System.out.println("double:" + double64(buffer.readLong()));
		//
		// // bytes 对应的 LongDouble = 3.3 小端序
		// buffer.write(new byte[] { 0x05, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00,
		// 0x00, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x0a, 0x40 });
		// buffer.readDoubleLE();
		// System.out.println(format.format(buffer.readDoubleLE()));
		//
		// // bytes 对应的 LongDouble = 3.3 大端序
		// buffer.write(new byte[] { 0x40, 0x0a, 0x66, 0x66, 0x66, 0x66, 0x66,
		// 0x66, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x05 });
		// System.out.println(format.format(buffer.readDouble()));
		// buffer.readDouble();
		//
		// // bytes 对应的 LongDouble = 0.12794 大端序
		// buffer.write(new byte[] { 0x3f, (byte) 0xc0, 0x60, 0x56, (byte) 0x81,
		// (byte) 0xec, (byte) 0xd6, (byte) 0xd7, 0x00, 0x00, 0x00, 0x01, 0x00,
		// 0x04, 0x00, 0x08 });
		// System.out.println(format.format(buffer.readDouble()));
		// buffer.readDouble();
		//
		// // bytes 对应的 LongDouble = 0.12794 小端序
		// buffer.write(new byte[] { 0x08, 0x00, 0x04, 0x00, 0x01, 0x00, 0x00,
		// 0x00, (byte) 0xd7, (byte) 0xd6, (byte) 0xec, (byte) 0x81, 0x56, 0x60,
		// (byte) 0xc0, 0x3f });
		// buffer.readDouble();
		// System.out.println(format.format(buffer.readDoubleLE()));
	}

	@Test
	void test1() {
		BigDecimal value;

		// bytes 对应的 LongDouble = 3.3 大端序
		byte[] bytes = new byte[] { 0x40, 0x0a, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x05 };
		value = double128(bytes);
		value = value.setScale(10, RoundingMode.CEILING);
		System.out.println(value);

		// 123456789.123456789
		// 0x4019D6F34547E6B74DCE58D7CC490820
		bytes = new byte[] { 0x40, 0x19, (byte) 0xD6, (byte) 0xF3, 0x45, 0x47, (byte) 0xE6, (byte) 0xB7, 0x4D, (byte) 0xCE, 0x58, (byte) 0xD7, (byte) 0xCC, 0x49, 0x08, 0x20 };
		value = double128(bytes);
		value = value.setScale(10, RoundingMode.CEILING);
		System.out.println(value);
	}

	/**
	 * LongDouble IEEE754 Binary128
	 * 
	 * <pre>
	 * IEEE 754 四精度浮点数结构 128Bit
	 * [S|E 15Bit        |M 112Bit                                                                                                         ]
	 * [0 000000000000000 000000000000000000000000000000000000000000000000|0000000000000000000000000000000000000000000000000000000000000000]
	 * [0 111111111111111 000000000000000000000000000000000000000000000000]0x7FFF
	 * [0 000000000000000 111111111111111111111111111111111111111111111111]0xFFFFFFFFFFFF
	 * 
	 * [0 0000000|0000 0000|00000000|00000000|00000000|00000000|00000000|00000000]
	 * 
	 * bias = 16383
	 * 16495 = bais + 112
	 * </pre>
	 * 
	 * @param bits1
	 * @param bits2
	 * @return
	 * @author simon
	 */
	BigDecimal double128(byte[] bytes) {
		// sign符号1位:1负0正
		int s = (bytes[0] & 0x80) == 0 ? 1 : -1;
		// exponent指数15位
		int e = ((bytes[0] & 0x7F) << 8) | (bytes[1] & 0xFF);
		if (e == 0) {

		} else {
			// m = 1 + INT * POW(2,-122)
			// s * m * pow(2, e - 16383);
			BigInteger fraction = new BigInteger(s, bytes, 2, 6 + 8);
			BigDecimal decimal = new BigDecimal(fraction);
			decimal = decimal.multiply(new BigDecimal(Math.pow(2, -112)));
			decimal = decimal.add(BigDecimal.ONE);
			decimal = decimal.multiply(new BigDecimal(s * Math.pow(2, e - 16383)));
			decimal.setScale(10, RoundingMode.HALF_UP);
			return decimal;
		}

		return BigDecimal.ZERO;
	}

	/**
	 * <pre>
	 * IEEE 754 双精度浮点数结构 64Bit
	 * [S 1B|E 11Bit    |M 52Bit                                             ]
	 * 零（非常规）：当阶码与尾数都是全0时，表示0，符号为0表示正0；符号为1表示负0
	 * [0    00000000000 0000000000000000000000000000000000000000000000000000]
	 * 无穷（非常规）：当阶码区为11个1，并且尾数52个0时，表示无穷；符号为0表示正无穷；符号为1表示负无穷Infinity
	 * [0    11111111111 0000000000000000000000000000000000000000000000000000]
	 * NaN（非常规）：当阶码区为11个1，并且尾数为不是0时，表示NaN(非数字)
	 * [0    11111111111 1000000000000000000000000000000000000000000000000000]
	 * bias=1023
	 * 1075 = bias + 52
	 * </pre>
	 * 
	 * @author simon
	 */
	double double64(long bits) {
		int s = ((bits >> 63) == 0) ? 1 : -1;
		int e = (int) ((bits >> 52) & 0x7FFL);
		long m = (e == 0) ? (bits & 0xFFFFFFFFFFFFFL) << 1 : (bits & 0xFFFFFFFFFFFFFL) | 0x10000000000000L;
		return s * m * Math.pow(2, e - 1075);
		// if (e == 0 && m == 0) {
		// return 0;
		// } else if (e == 2047) {
		// if (m == 0) {
		// return s > 0 ? Double.POSITIVE_INFINITY : Double.POSITIVE_INFINITY;
		// } else {
		// return Double.NaN;
		// }
		// } else {
		// return s * m * Math.pow(2, e - 1075);
		// }
	}
}
