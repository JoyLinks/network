package com.joyzl.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试BCD编码 {@link LittleEndianBCDOutput}
 * 
 * @author ZhangXi
 * @date 2023年9月3日
 */
class TestBCDLittleEndian {

	final ByteArrayOutputStream out = new ByteArrayOutputStream();
	final LittleEndianBCDOutput output = new LittleEndianBCDOutput() {
		@Override
		public void writeByte(int b) {
			out.write(b);
		}
	};
	ByteArrayInputStream in;
	final LittleEndianBCDInput input = new LittleEndianBCDInput() {
		@Override
		public byte readByte() {
			return (byte) in.read();
		}
	};

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
	void test8421() throws IOException {
		output.writeBCD(00);
		output.writeBCD(99);
		output.writeBCDs(987654321);
		output.writeBCDs("00000000");

		output.writeBCD8421(11);
		output.writeBCD8421(88);
		output.writeBCD8421s(123456789);
		output.writeBCD8421s("111111111111");

		in = new ByteArrayInputStream(out.toByteArray());

		assertEquals(input.readBCD(), 00);
		assertEquals(input.readBCD(), 99);
		assertEquals(input.readBCDs(9), 987654321);
		assertEquals(input.readBCDString(8), "00000000");

		assertEquals(input.readBCD8421(), 11);
		assertEquals(input.readBCD8421(), 88);
		assertEquals(input.readBCD8421s(9), 123456789);
		assertEquals(input.readBCD8421String(12), "111111111111");

		assertEquals(in.available(), 0);
	}

	@Test
	void test3() throws IOException {
		output.writeBCD3(11);
		output.writeBCD3(88);
		output.writeBCD3s(123456789);
		output.writeBCD3s("111111111111");

		in = new ByteArrayInputStream(out.toByteArray());

		assertEquals(input.readBCD3(), 11);
		assertEquals(input.readBCD3(), 88);
		assertEquals(input.readBCD3s(9), 123456789);
		assertEquals(input.readBCD3String(12), "111111111111");

		assertEquals(in.available(), 0);
	}

	@Test
	void test2421() throws IOException {
		output.writeBCD2421(11);
		output.writeBCD2421(88);
		output.writeBCD2421s(123456789);
		output.writeBCD2421s("111111111111");

		in = new ByteArrayInputStream(out.toByteArray());

		assertEquals(input.readBCD2421(), 11);
		assertEquals(input.readBCD2421(), 88);
		assertEquals(input.readBCD2421s(9), 123456789);
		assertEquals(input.readBCD2421String(12), "111111111111");

		assertEquals(in.available(), 0);
	}

	@Test
	void test5421() throws IOException {
		output.writeBCD5421(11);
		output.writeBCD5421(88);
		output.writeBCD5421s(123456789);
		output.writeBCD5421s("111111111111");

		in = new ByteArrayInputStream(out.toByteArray());

		assertEquals(input.readBCD5421(), 11);
		assertEquals(input.readBCD5421(), 88);
		assertEquals(input.readBCD5421s(9), 123456789);
		assertEquals(input.readBCD5421String(12), "111111111111");

		assertEquals(in.available(), 0);
	}
}
