package com.joyzl.network.codec.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
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
	void testEmpty() {
		final DataBuffer buffer = DataBuffer.instance();
		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), DataBufferUnit.UNIT_SIZE);

		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@RepeatedTest(100)
	void testWriteReadByte() {
		final DataBuffer buffer = DataBuffer.instance();
		// WRITE 65536
		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}

		assertEquals(buffer.units(), 65536 / DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.capacity(), 65536);
		assertEquals(buffer.readable(), 65536);
		assertEquals(buffer.writeable(), 65536 % DataBufferUnit.UNIT_SIZE);

		// READ 65536
		for (int index = 0; index < 65536; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@RepeatedTest(100)
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

		assertEquals(buffer.units(), 65536 / DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.capacity(), 65536);
		assertEquals(buffer.readable(), 65536);
		assertEquals(buffer.writeable(), 65536 % DataBufferUnit.UNIT_SIZE);
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
		assertEquals(buffer.capacity(), DataBufferUnit.UNIT_SIZE);
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
		assertEquals(buffer.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testBoundsDiscard() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}
		assertEquals(buffer.units(), 65536 / DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.discard(), 0);
		assertEquals(buffer.readable(), 65536);

		// 读取部分限制内数据
		buffer.bounds(2048);
		for (int index = 0; index < 1024; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}
		assertEquals(buffer.discard(), 1024);
		assertEquals(buffer.readable(), 65536 - 2048);
		assertEquals(buffer.units(), 62);

		// 读取全部限制内数据
		buffer.bounds(2048);
		for (int index = 0; index < 2048; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}
		assertEquals(buffer.discard(), 0);
		assertEquals(buffer.readable(), 65536 - 2048 - 2048);
		assertEquals(buffer.units(), 61);

		// 继续读取后续数据
		for (int index = 0; index < 65536 - 2048 - 2048; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testBoundsResidue() throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}

		assertEquals(buffer.units(), 65536 / DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.residue(), 0);

		buffer.bounds(2048);
		assertEquals(buffer.residue(), 65536 - 2048);

		// 转移限制外数据
		final DataBuffer target = DataBuffer.instance();
		buffer.residue(target);
		assertEquals(buffer.residue(), 0);

		// 验证转移的数据
		for (int index = 0; index < 65536 - 2048; index++) {
			assertEquals(target.readByte(), (byte) index);
		}

		assertEquals(target.units(), 1);
		assertEquals(target.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(target.readable(), 0);
		assertEquals(target.writeable(), 0);
		target.release();

		// 验证保留的数据
		for (int index = 0; index < 2048; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);
		buffer.release();
	}

	@RepeatedTest(100)
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
		assertEquals(source.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(source.readable(), 0);
		assertEquals(source.writeable(), 0);
		source.release();

		assertEquals(target.units(), 1);
		assertEquals(target.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(target.readable(), 0);
		assertEquals(target.writeable(), 0);
		target.release();

		assertEquals(DataBuffer.freeCount(), 2);
	}

	@RepeatedTest(100)
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
		assertEquals(source.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(source.readable(), 0);
		assertEquals(source.writeable(), 0);
		source.release();

		assertEquals(target.units(), 1);
		assertEquals(target.capacity(), DataBufferUnit.UNIT_SIZE);
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
		assertEquals(source.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(source.readable(), 0);
		assertEquals(source.writeable(), 0);
		source.release();

		assertEquals(target.units(), 1);
		assertEquals(target.capacity(), DataBufferUnit.UNIT_SIZE);
		assertEquals(target.readable(), 0);
		assertEquals(target.writeable(), 0);
		target.release();
	}
}