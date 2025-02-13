package com.joyzl.network.codec.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;

/**
 * 测试 {@link DataBuffer}
 * 
 * @author ZhangXi
 * @date 2023年9月3日
 */
class TestDataBuffer {

	// 65536 Byte = 64Kb

	@Test
	void testTime() {
		DataBufferUnit unit;
		long time = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			unit = DataBufferUnit.get();
			unit.release();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("GET:" + time);

		ByteBuffer bb;
		time = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			bb = ByteBuffer.allocateDirect(DataBufferUnit.BYTES);
			bb.clear();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("NEW:" + time);

	}

	@Test
	void testDataBufferUnit() {
		DataBufferUnit unit = DataBufferUnit.get();

		ByteBuffer b = unit.receive();
		assertEquals(unit.received(), 0);

		b = unit.receive();
		b.put((byte) 0);
		assertEquals(unit.received(), 1);
	}

	@Test
	void testMethods() throws Exception {
		// 验证方法极限

		final DataBuffer buffer = DataBuffer.instance();

		try {
			buffer.set(0, (byte) 0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
			buffer.clear();
		}
		try {
			buffer.writeByte(1);
			buffer.set(1, (byte) 0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
			buffer.clear();
		}

		try {
			buffer.get(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
			buffer.clear();
		}
		try {
			buffer.writeByte(1);
			buffer.get(1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
			buffer.clear();
		}
		try {
			buffer.readByte();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
			buffer.clear();
		}

		int length;
		length = buffer.write(InputStream.nullInputStream());
		assertEquals(length, 0);
		length = buffer.write(InputStream.nullInputStream(), 0);
		assertEquals(length, 0);
	}

	@Test
	void testEmpty() {
		final DataBuffer buffer = DataBuffer.instance();
		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.BYTES);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), DataBufferUnit.BYTES);

		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testWriteReadByte() {
		final DataBuffer buffer = DataBuffer.instance();
		// WRITE 65536
		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}

		assertEquals(buffer.units(), 65536 / DataBufferUnit.BYTES);
		assertEquals(buffer.capacity(), 65536);
		assertEquals(buffer.readable(), 65536);
		assertEquals(buffer.writeable(), 65536 % DataBufferUnit.BYTES);

		// READ 65536
		for (int index = 0; index < 65536; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.BYTES);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testWriteReadIndex() {
		final DataBuffer buffer = DataBuffer.instance();
		// WRITE 65536
		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}
		// SET 65536
		for (int index = 0; index < 65536; index++) {
			buffer.set(index, (byte) index);
		}
		// GET 65536
		for (int index = 0; index < 65536; index++) {
			assertEquals(buffer.get(index), (byte) index);
		}

		assertEquals(buffer.units(), 65536 / DataBufferUnit.BYTES);
		assertEquals(buffer.capacity(), 65536);
		assertEquals(buffer.readable(), 65536);
		assertEquals(buffer.writeable(), 65536 % DataBufferUnit.BYTES);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testWriteReadStream() throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		for (int index = 0; index < 65536; index++) {
			output.write(index);
		}
		final ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

		final DataBuffer buffer = DataBuffer.instance();
		buffer.write(input);
		output.reset();
		buffer.read(output);

		final byte[] bytes = output.toByteArray();
		for (int index = 0; index < 65536; index++) {
			assertEquals(bytes[index], (byte) index);
		}

		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.BYTES);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testMarkReset() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}

		buffer.mark();
		for (int index = 0; index < 65536; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		buffer.reset();
		for (int index = 0; index < 65536; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.BYTES);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testMarkErase() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}

		buffer.mark();
		for (int index = 0; index < 65536; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		buffer.erase();
		assertEquals(buffer.units(), 1);
		assertEquals(buffer.readable(), 0);

		buffer.reset();
		assertEquals(buffer.units(), 1);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testReadBuffer() throws IOException {
		final DataBuffer source = DataBuffer.instance();
		for (int index = 0; index < 65536; index++) {
			source.writeByte((byte) index);
		}

		final DataBuffer target = DataBuffer.instance();
		source.read(target);

		for (int index = 0; index < 65536; index++) {
			assertEquals(target.readByte(), (byte) index);
		}

		assertEquals(source.units(), 1);
		assertEquals(source.capacity(), DataBufferUnit.BYTES);
		assertEquals(source.readable(), 0);
		assertEquals(source.writeable(), 0);
		source.release();

		assertEquals(target.units(), 1);
		assertEquals(target.capacity(), DataBufferUnit.BYTES);
		assertEquals(target.readable(), 0);
		assertEquals(target.writeable(), 0);
		target.release();

		assertEquals(DataBuffer.freeCount(), 2);
	}

	@Test
	void testWriteBuffer() throws IOException {
		final DataBuffer source = DataBuffer.instance();
		for (int index = 0; index < 65536; index++) {
			source.writeByte((byte) index);
		}

		final DataBuffer target = DataBuffer.instance();
		target.write(source);

		for (int index = 0; index < 65536; index++) {
			assertEquals(target.readByte(), (byte) index);
		}

		assertEquals(source.units(), 1);
		assertEquals(source.capacity(), DataBufferUnit.BYTES);
		assertEquals(source.readable(), 0);
		assertEquals(source.writeable(), 0);
		source.release();

		assertEquals(target.units(), 1);
		assertEquals(target.capacity(), DataBufferUnit.BYTES);
		assertEquals(target.readable(), 0);
		assertEquals(target.writeable(), 0);
		target.release();

		assertEquals(source.units(), 1);
		assertEquals(target.units(), 1);
		assertEquals(DataBuffer.freeCount(), 2);
	}

	@Test
	void testReplicate() throws IOException {
		final DataBuffer source = DataBuffer.instance();
		for (int index = 0; index < 65536; index++) {
			source.writeByte((byte) index);
		}

		final DataBuffer target = DataBuffer.instance();

		target.replicate(source);
		for (int index = 0; index < 65536; index++) {
			assertEquals(target.readByte(), (byte) index);
		}

		target.replicate(source, 2048, 4096);
		for (int index = 0; index < 2048; index++) {
			assertEquals(target.readByte(), (byte) index);
		}

		for (int index = 0; index < 65536; index++) {
			assertEquals(source.readByte(), (byte) index);
		}

		assertEquals(source.units(), 1);
		assertEquals(source.capacity(), DataBufferUnit.BYTES);
		assertEquals(source.readable(), 0);
		assertEquals(source.writeable(), 0);
		source.release();

		assertEquals(target.units(), 1);
		assertEquals(target.capacity(), DataBufferUnit.BYTES);
		assertEquals(target.readable(), 0);
		assertEquals(target.writeable(), 0);
		target.release();
	}

	@Test
	void testTransfer() {
		int size = 65536;
		final DataBuffer target = DataBuffer.instance();
		final DataBuffer source = DataBuffer.instance();
		for (int index = 0; index < size; index++) {
			source.writeByte((byte) index);
		}

		// 写入半单元量
		source.transfer(target, 512);
		source.transfer(target, 512);
		assertEquals(target.readable(), 1024);
		assertEquals(source.readable(), size -= 1024);
		for (int index = 0; index < 1024; index++) {
			assertEquals(target.readByte(), (byte) index);
		}
		// 写入全单元量
		source.transfer(target, 1024);
		source.transfer(target, 1024);
		assertEquals(target.readable(), 2048);
		assertEquals(source.readable(), size -= 2048);
		for (int index = 0; index < 2048; index++) {
			assertEquals(target.readByte(), (byte) (index + 1024));
		}
		// 写入超单元量
		source.transfer(target, 1536);
		source.transfer(target, 1536);
		assertEquals(target.readable(), 3072);
		assertEquals(source.readable(), size -= 3072);
		for (int index = 0; index < 3072; index++) {
			assertEquals(target.readByte(), (byte) (index + 3072));
		}
		// 写入半单元量
		source.transfer(target, 512);
		source.transfer(target, 512);
		assertEquals(target.readable(), 1024);
		assertEquals(source.readable(), size -= 1024);
		for (int index = 0; index < 1024; index++) {
			assertEquals(target.readByte(), (byte) (index + 4096));
		}

		source.transfer(target, 12);
		source.transfer(target, 500);
		source.transfer(target, 1000);
		source.transfer(target, 24);
		source.transfer(target, 512);
		source.transfer(target, 1024);
		assertEquals(target.readable(), 3072);
		assertEquals(source.readable(), size -= 3072);
		for (int index = 0; index < 3072; index++) {
			assertEquals(target.readByte(), (byte) (index + 5120));
		}
	}
}