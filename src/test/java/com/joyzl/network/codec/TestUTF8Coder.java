/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;

class TestUTF8Coder {

	@Test
	void test() throws IOException {
		final String text = """
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

		final DataBuffer buffer = UTF8Coder.encode(text);
		final CharSequence temp = UTF8Coder.decodeUTF8(buffer);

		assertEquals(text, temp.toString());

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
}