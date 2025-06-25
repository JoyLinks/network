/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferReader;
import com.joyzl.network.buffer.DataBufferWriter;

class TestUTF8Coder {

	final String text1 = "abcdefghiJKLMNOPQRST1234567890";
	final String text2 = """
			字母：abcdefghiJKLMNOPQRST
			数字：1234567890
			符号：!@#$%&*()
			控制：\n\t\r
			拉丁：áéíóúñç
			希腊：αβγδεπω
			西里尔：абвгдеё
			阿拉伯：ابتثجح
			中文:中华人民共和国
			日文：こんにちは私の名
			韩文：안녕하세요한국어
			特殊：©®™€¥₩♠♥
			表情：😀❤️🌍🎂🎉🚀🐱🦄
			特殊：𠀀𠀁𠀂𠀃𠀄
			数学：𝛼𝛽𝛾𝛿𝜋
			边界：\u007F \u0080 \u07FF \u0800 \uFFFF \uD83D\uDE00
			特殊: \u200B \uFEFF \uFFFD
			""";

	@Test
	void testUTF8Coder() throws IOException {
		DataBuffer buffer = UTF8Coder.encode(text1);
		CharSequence temp = UTF8Coder.decodeUTF8(buffer);

		assertEquals(text1, temp.toString());

		buffer = UTF8Coder.encode(text2);
		temp = UTF8Coder.decodeUTF8(buffer);

		assertEquals(text2, temp.toString());

	}

	@Test
	void testReaderWriter() throws IOException {
		final DataBufferWriter writer = new DataBufferWriter();
		writer.write(text1);

		final DataBufferReader reader = new DataBufferReader(writer.buffer());
		final CharBuffer buffer = CharBuffer.allocate(1024);
		reader.read(buffer);
		assertEquals(text1, buffer.flip().toString());

		writer.write(text2);
		reader.read(buffer.clear());
		assertEquals(text2, buffer.flip().toString());

		writer.close();
		reader.close();
	}

	@Test
	void testEmoji() throws IOException {
		final int A = 0x1F000;
		final int B = 0x1FFFF;

		final StringBuilder emoji = new StringBuilder();
		for (int c = A; c <= B; c++) {
			emoji.appendCodePoint(c);
		}
		DataBuffer buffer = UTF8Coder.encode(emoji);
		assertEquals(buffer.readable(), (B - A + 1) * 4);
		System.out.println("Emoji:" + buffer.readable() + "byte");

		long time = System.currentTimeMillis();
		final int count = 10000;
		for (int i = 0; i < count; i++) {
			UTF8Coder.encode(emoji);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("Emoji:" + time + "ms");
	}

	@Test
	void testDataBuffer() throws IOException {
		// RFC3629 UTF-8

		DataBuffer buffer = DataBuffer.instance();
		buffer.writeUTF8("\u0041\u2262\u0391\u002E");
		// 41 E2 89 A2 CE 91 2E
		buffer.writeUTF8("\uD55C\uAD6D\uC5B4");
		// ED 95 9C EA B5 AD EC 96 B4
		buffer.writeUTF8("\u65E5\u672C\u8A9E");
		// E6 97 A5 E6 9C AC E8 AA 9E
		buffer.writeUTF8("\uFEFF");
		buffer.writeUTF8(0x233B4);
		// EF BB BF F0 A3 8E B4

		assertEquals('\u0041', buffer.readUTF8());
		assertEquals('\u2262', buffer.readUTF8());
		assertEquals('\u0391', buffer.readUTF8());
		assertEquals('\u002E', buffer.readUTF8());

		assertEquals('\uD55C', buffer.readUTF8());
		assertEquals('\uAD6D', buffer.readUTF8());
		assertEquals('\uC5B4', buffer.readUTF8());

		assertEquals('\u65E5', buffer.readUTF8());
		assertEquals('\u672C', buffer.readUTF8());
		assertEquals('\u8A9E', buffer.readUTF8());

		assertEquals('\uFEFF', buffer.readUTF8());
		assertEquals(0x233B4, buffer.readUTF8());
	}
}