package com.joyzl.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;

class TestUTF8Coder {

	@Test
	void test() throws IOException {
		final String text = "abcdefghiJKLMNOPQRST1234567890中华人民共和国";

		final DataBuffer buffer = UTF8Coder.encode(text);
		final CharSequence temp = UTF8Coder.decodeUTF8(buffer);

		assertEquals(text, temp.toString());

	}

}