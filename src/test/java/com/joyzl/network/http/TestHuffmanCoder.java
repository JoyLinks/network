package com.joyzl.network.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;

public class TestHuffmanCoder {

	@Test
	void testExample() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		final StringBuilder builder = new StringBuilder();

		assertEquals(HuffmanCoder.byteSize("www.example.com"), 12);
		HuffmanCoder.encode(buffer, "www.example.com");
		// f1 e3 c2 e5 f2 3a 6b a0 ab 90 f4 ff
		// f1 e3 c2 e5 f2 3a 6b a0 ab 90 f4 ff

		HuffmanCoder.decode(buffer, builder, 12);
		assertEquals(builder.toString(), "www.example.com");
	}

	@Test
	void testASCII() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();

		final StringBuilder builder1 = new StringBuilder();
		for (int c = 0; c <= 256; c++) {
			builder1.append((char) c);
		}

		HuffmanCoder.encode(buffer, builder1);

		final StringBuilder builder2 = new StringBuilder();
		HuffmanCoder.decode(buffer, builder2, buffer.readable());

		assertEquals(builder1.toString(), builder2.toString());
	}

	@Test
	void testTime() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		final StringBuilder builder = new StringBuilder();

		final int count = 1000000;
		final String text = "www.example.com";
		System.out.println("霍夫曼编编码字符:" + count * text.length());

		int length = 12;
		long time = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			length = HuffmanCoder.byteSize(text);
			HuffmanCoder.encode(buffer, text);
		}

		time = System.currentTimeMillis() - time;
		System.out.println("霍夫曼编编码耗时:" + time + "ms");
		System.out.println("霍夫曼编编码长度:" + buffer.readable());

		time = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			builder.setLength(0);
			HuffmanCoder.decode(buffer, builder, length);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("霍夫曼解码耗时:" + time + "ms");
	}
}