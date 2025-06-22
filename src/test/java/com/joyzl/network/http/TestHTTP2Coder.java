/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.joyzl.codec.BigEndianDataInput;
import com.joyzl.codec.BigEndianDataOutput;
import com.joyzl.codec.LittleEndianDataInput;
import com.joyzl.codec.LittleEndianDataOutput;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.codec.BigEndianBCDInput;
import com.joyzl.network.codec.BigEndianBCDOutput;
import com.joyzl.network.codec.LittleEndianBCDInput;
import com.joyzl.network.codec.LittleEndianBCDOutput;

class TestHTTP2Coder {

	@Test
	void test() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();

		final BigEndianDataOutput bedo = buffer;
		final LittleEndianDataOutput ledo = buffer;
		final BigEndianBCDOutput bebo = buffer;
		final LittleEndianBCDOutput lebo = buffer;

		buffer.writeInt(Integer.MAX_VALUE);
		bedo.writeInt(Integer.MAX_VALUE);
		ledo.writeInt(Integer.MAX_VALUE);
		bebo.writeBCD(Byte.MAX_VALUE);
		lebo.writeBCD(Byte.MAX_VALUE);

		final BigEndianDataInput bedi = buffer;
		final LittleEndianDataInput ledi = buffer;
		final BigEndianBCDInput bebi = buffer;
		final LittleEndianBCDInput lebi = buffer;

		assertEquals(buffer.readInt(), Integer.MAX_VALUE);
		assertEquals(bedi.readInt(), Integer.MAX_VALUE);
		assertEquals(ledi.readInt(), Integer.MAX_VALUE);
		assertEquals(bebi.readBCD(), Byte.MAX_VALUE);
		assertEquals(lebi.readBCD(), Byte.MAX_VALUE);
	}

	@Test
	void testTag() {
		assertTrue(HTTP2Coder.isHuffman(HTTP2Coder.HUFFMAN));
		assertTrue(HTTP2Coder.isIndexed(HTTP2Coder.INDEXED));
		assertTrue(HTTP2Coder.isIncremental(HTTP2Coder.INCREMENTAL));
		assertTrue(HTTP2Coder.isNoIndexing(HTTP2Coder.NO_INDEXING));
		assertTrue(HTTP2Coder.isTableSize(HTTP2Coder.TABLE_SIZE));

		final byte value = 1;

		assertTrue(HTTP2Coder.isHuffman((byte) (HTTP2Coder.HUFFMAN | value)));
		assertTrue(HTTP2Coder.isIndexed((byte) (HTTP2Coder.INDEXED | value)));
		assertTrue(HTTP2Coder.isIncremental((byte) (HTTP2Coder.INCREMENTAL | value)));
		assertTrue(HTTP2Coder.isNoIndexing((byte) (HTTP2Coder.NO_INDEXING | value)));
		assertTrue(HTTP2Coder.isTableSize((byte) (HTTP2Coder.TABLE_SIZE | value)));
	}

	@Test
	void testVarint() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		byte tag = 0;

		HTTP2Coder.writeVarint(buffer, tag, 10, 5);
		HTTP2Coder.writeVarint(buffer, tag, 1337, 5);
		HTTP2Coder.writeVarint(buffer, tag, 42, 8);

		// System.out.println(buffer);
		// 0~5 [0a 1f 9a 0a 2a]

		tag = buffer.readByte();
		assertEquals(HTTP2Coder.readVarint(buffer, tag, 5), 10);
		tag = buffer.readByte();
		assertEquals(HTTP2Coder.readVarint(buffer, tag, 5), 1337);
		tag = buffer.readByte();
		assertEquals(HTTP2Coder.readVarint(buffer, tag, 8), 42);

		tag = HTTP2Coder.INDEXED;
		HTTP2Coder.writeVarint(buffer, tag, 10, 5);
		HTTP2Coder.writeVarint(buffer, tag, 1337, 5);
		HTTP2Coder.writeVarint(buffer, tag, 42, 8);

		tag = buffer.readByte();
		assertEquals(HTTP2Coder.readVarint(buffer, tag, 5), 10);
		tag = buffer.readByte();
		assertEquals(HTTP2Coder.readVarint(buffer, tag, 5), 1337);
		tag = buffer.readByte();
		assertEquals(HTTP2Coder.readVarint(buffer, tag, 8), 42);
	}
}