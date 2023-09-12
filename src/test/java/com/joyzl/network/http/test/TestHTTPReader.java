/**
 * www.joyzl.net<br>
 * 中翌智联（重庆）科技有限公司<br>
 * Copyright © JOY-Links Company. All rights reserved. 
 */
package com.joyzl.network.http.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;

/**
 * HTTP 相关测试
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年10月14日
 */
class TestHTTPReader {

	final DataBuffer buffer = DataBuffer.instance();
	final HTTPWriter writer = new HTTPWriter(buffer);
	final HTTPReader reader = new HTTPReader(buffer);

	@BeforeEach
	void setUp() throws Exception {
		buffer.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testSkipWhitespace() throws Exception {
		// 测试样本
		writer.write(" \t \n A");
		writer.write(" \t \r B");

		reader.skipWhitespace();
		reader.readTo(' ');
		assertEquals(reader.previous(), 'A');

		reader.skipWhitespace();
		reader.readTo(' ');
		assertEquals(reader.last(), 'B');
	}

	@Test
	void testReadTo1() throws Exception {
		// 测试样本
		writer.write("AAAA BBBB CCC");

		reader.readTo(HTTPCoder.SPACE);
		assertEquals(reader.last(), HTTPCoder.SPACE);
		assertEquals(reader.previous(), 'A');
		assertEquals(reader.string(), "AAAA");

		reader.readTo(HTTPCoder.SPACE);
		assertEquals(reader.last(), HTTPCoder.SPACE);
		assertEquals(reader.previous(), 'B');
		assertEquals(reader.string(), "BBBB");

		reader.readTo(HTTPCoder.SPACE);
		assertEquals(reader.last(), 'C');
		assertEquals(reader.previous(), 'C');
		assertEquals(reader.string(), "CCC");
	}

	@Test
	void testReadTo2() throws Exception {
		// 测试样本
		writer.write("AAAA BBBB");
		writer.write(HTTPCoder.CRLF);
		writer.write("CCC");
		writer.write(HTTPCoder.CRLF);

		reader.readTo(HTTPCoder.SPACE, HTTPCoder.CRLF);
		assertEquals(reader.last(), HTTPCoder.SPACE);
		assertEquals(reader.previous(), 'A');
		assertEquals(reader.string(), "AAAA");

		reader.readTo(HTTPCoder.SPACE, HTTPCoder.CRLF);
		assertEquals(reader.last(), HTTPCoder.LF);
		assertEquals(reader.previous(), 'B');
		assertEquals(reader.string(), "BBBB");
	}

	@Test
	void testReadTo3() throws Exception {
		// 测试样本
		writer.write("AAAA");
		writer.write(HTTPCoder.SPACE);
		writer.write("BBBB");
		writer.write("CCC");
		writer.write("XXXYYYZZZ");
		writer.write("XYZ");
		writer.write("DD");

		reader.readTo(HTTPCoder.SPACE, "BBBB");
		assertEquals(reader.last(), HTTPCoder.SPACE);
		assertEquals(reader.previous(), 'A');
		assertEquals(reader.string(), "AAAA");

		reader.readTo(HTTPCoder.SPACE, "CCC");
		assertEquals(reader.last(), 'C');
		assertEquals(reader.previous(), 'B');
		assertEquals(reader.string(), "BBBB");

		reader.readTo(HTTPCoder.SPACE, "XYZ");
		assertEquals(reader.last(), 'Z');
		assertEquals(reader.previous(), 'Z');
		assertEquals(reader.string(), "XXXYYYZZZ");

		reader.readTo(HTTPCoder.SPACE, "DD");
		assertEquals(reader.last(), 'D');
		assertEquals(reader.string(), "");
	}

	@Test
	void testReadAt1() throws Exception {
		// 测试样本
		writer.write("AAAABBBBCCC");
		writer.write("DDD");
		// 测试样本
		writer.write("BC-DEF|BCD|BCDE|CDEF");
		writer.write("BCDEF");
		// 测试样本
		writer.write("DD");

		reader.readAt('D', "DD");
		assertEquals(reader.string(), "AAAABBBBCCC");

		reader.readAt('B', "CDEF");
		assertEquals(reader.string(), "BC-DEF|BCD|BCDE|CDEF");

		reader.readAt('D', "D");
		assertEquals(reader.string(), "");
	}

	@Test
	void testReadAt2() throws Exception {
		// 测试样本
		writer.write("AAAA");
		writer.write("BBBB");
		writer.write("CCC");
		writer.write("XXXYYYZZZ");
		// 测试样本
		writer.write("BC-DEF|BCD|BCDE|CDEF");
		writer.write("BC");
		writer.write("DEF");
		// 测试样本
		writer.write("XYZ");
		writer.write("DD");

		reader.readAt("CCC", "XXXYYYZZZ");
		assertEquals(reader.string(), "AAAABBBB");

		reader.readAt("BC", "DEF");
		assertEquals(reader.string(), "BC-DEF|BCD|BCDE|CDEF");

		reader.readAt("XYZ", "DD");
		assertEquals(reader.string(), "");
	}

	@Test
	void testReadBy1() throws Exception {
		// 测试样本
		writer.write("AAAABBBBCCCCDDDDEEEE");
		writer.write("XXXYYYZZZ");
		// 测试样本
		writer.write("BC-DEF|BCD|BCDE|CDEF");
		writer.write("BCDEF");
		// 测试样本
		writer.write("XYZ");
		writer.write("DD");

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		reader.readBy(output, "XXXYYYZZZ");
		assertEquals(output.toString(), "AAAABBBBCCCCDDDDEEEE");

		output.reset();
		reader.readBy(output, "BCDEF");
		assertEquals(output.toString(), "BC-DEF|BCD|BCDE|CDEF");

		output.reset();
		reader.readBy(output, "DD");
		assertEquals(output.toString(), "XYZ");
	}

	@Test
	void testReadBy2() throws Exception {
		// 测试样本
		writer.write("AAAABBBBCCCCDDDDEEEE");
		writer.write("XXXYYYZZZ");
		writer.write("--");
		// 测试样本
		writer.write("BC-DEF|BCD|BCDE|CDEF");
		writer.write("BCDEF");
		writer.write("--");
		// 测试样本
		writer.write("XYZ");
		writer.write("DD");
		writer.write("--");

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		reader.readBy(output, "XXXYYYZZZ", "--");
		assertEquals(output.toString(), "AAAABBBBCCCCDDDDEEEE");

		output.reset();
		reader.readBy(output, "BCDEF", "--");
		assertEquals(output.toString(), "BC-DEF|BCD|BCDE|CDEF");

		output.reset();
		reader.readBy(output, "DD", "--");
		assertEquals(output.toString(), "XYZ");
	}
}
