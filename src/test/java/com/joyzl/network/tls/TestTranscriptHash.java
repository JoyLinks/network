package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;

public class TestTranscriptHash {

	@Test
	void test() throws Exception {
		final V2TranscriptHash t = new V2TranscriptHash();
		t.initialize("SHA-256");

		assertEquals(32, t.hashLength());
		assertArrayEquals(Utility.hex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"), t.hashEmpty());
		assertArrayEquals(Utility.hex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"), t.hash());

		final DataBuffer buffer1 = DataBuffer.instance();
		final DataBuffer buffer2 = DataBuffer.instance();
		for (int i = 0; i < 1024 + 512; i++) {
			buffer1.write(i);
			buffer2.write(i);
		}

		t.hashReset();
		t.hash(buffer1);
		final byte[] result1 = t.hash();

		t.hashReset();
		buffer1.writeByte(1);
		t.hash(buffer1, 1024 + 512);
		final byte[] result2 = t.hash();

		assertArrayEquals(result1, result2);
		buffer1.backByte();
		assertEquals(buffer1, buffer2);

		assertEquals(32, t.hashLength());
	}
}