package com.joyzl.network.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
	DataBuffer buffer;

	@BeforeEach
	void setUp() throws Exception {
		buffer = DataBuffer.instance();
		assertEquals(buffer.capacity(), DataBufferUnit.BYTES);
		assertEquals(buffer.writeable(), DataBufferUnit.BYTES);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.units(), 1);
	}

	@AfterEach
	void tearDown() throws Exception {
		buffer.release();
		assertEquals(buffer.units(), 1);
	}

	// EMPTY 测试空状态
	// LITTLE 测试少量数据状态，仅一个单元
	// MORE 测试较多数据状态，超过一个单元

	@Test
	void testGetSet() throws Exception {
		// EMPTY

		try {
			buffer.get(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		try {
			buffer.set(0, (byte) 0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTLE

		buffer.writeByte(1);
		assertEquals(buffer.get(0), 1);
		buffer.set(0, Byte.MAX_VALUE);
		assertEquals(buffer.get(0), Byte.MAX_VALUE);

		try {
			buffer.set(1, (byte) 0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		try {
			buffer.get(1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// MORE

		buffer.clear();
		int size = DataBufferUnit.BYTES * 2 + 100;
		for (int index = 0; index < size; index++) {
			buffer.writeByte((byte) index);
		}

		assertEquals(buffer.get(0), 0);
		assertEquals(buffer.get(size - 1), (byte) (size - 1));
		try {
			buffer.get(size);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		buffer.set(0, Byte.MAX_VALUE);
		buffer.set(size - 1, Byte.MAX_VALUE);
		try {
			buffer.set(size, Byte.MAX_VALUE);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
	}

	@Test
	void testWriteReadByte() {
		// EMPTY

		try {
			buffer.readByte();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
			buffer.clear();
		}

		// LITTLE

		buffer.writeByte(Byte.MAX_VALUE);
		assertEquals(buffer.readByte(), Byte.MAX_VALUE);
		try {
			buffer.readByte();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
			buffer.clear();
		}

		// MORE

		buffer.clear();
		int size = 65536;
		for (int index = 0; index < size; index++) {
			buffer.writeByte((byte) index);
		}

		assertEquals(buffer.units(), size / DataBufferUnit.BYTES);
		assertEquals(buffer.capacity(), size);
		assertEquals(buffer.readable(), size);
		assertEquals(buffer.writeable(), size % DataBufferUnit.BYTES);

		for (int index = 0; index < size; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}

		assertEquals(buffer.units(), 1);
		assertEquals(buffer.capacity(), DataBufferUnit.BYTES);
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.writeable(), 0);

		try {
			buffer.readByte();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
	}

	@Test
	void testWriteReadStream() throws IOException {
		// EMPTY

		int size;
		size = buffer.write(InputStream.nullInputStream());
		assertEquals(size, 0);
		assertEquals(buffer.readable(), 0);
		size = buffer.write(InputStream.nullInputStream(), 0);
		assertEquals(size, 0);
		assertEquals(buffer.readable(), 0);

		buffer.read(OutputStream.nullOutputStream());
		assertEquals(buffer.readable(), 0);

		try {
			buffer.read(OutputStream.nullOutputStream(), 1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTLE

		byte[] data = new byte[] { Byte.MAX_VALUE };
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ByteArrayInputStream input = new ByteArrayInputStream(data);

		int length = buffer.write(input);
		assertEquals(length, 1);
		assertEquals(buffer.readable(), 1);
		buffer.read(output);
		assertEquals(output.size(), 1);
		assertEquals(buffer.readable(), 0);
		assertArrayEquals(output.toByteArray(), data);

		input.reset();
		output.reset();

		length = buffer.write(input, 1);
		assertEquals(length, 1);
		assertEquals(buffer.readable(), 1);
		buffer.read(output, 1);
		assertEquals(output.size(), 1);
		assertEquals(buffer.readable(), 0);
		assertArrayEquals(output.toByteArray(), data);

		// MORE

		size = 65536;
		output.reset();
		for (int index = 0; index < size; index++) {
			output.write(index);
		}
		data = output.toByteArray();
		input = new ByteArrayInputStream(data);
		output.reset();

		length = buffer.write(input);
		assertEquals(length, size);
		assertEquals(buffer.readable(), size);
		buffer.read(output);
		assertEquals(buffer.readable(), 0);
		assertArrayEquals(output.toByteArray(), data);

		input.reset();
		size = size / 3;
		length = buffer.write(input, size);
		assertEquals(length, size);
		assertEquals(buffer.readable(), size);
		output.reset();
		buffer.read(output, size);
		assertEquals(buffer.readable(), 0);
		data = Arrays.copyOf(data, size);
		assertArrayEquals(output.toByteArray(), data);
	}

	@Test
	void testReplicate() throws IOException {
		final DataBuffer source = DataBuffer.instance();

		// EMPTY

		buffer.replicate(source);
		assertEquals(buffer.readable(), 0);
		// buffer.replicate(source, 0, 0);
		// assertEquals(buffer.readable(), 0);

		try {
			buffer.replicate(source, 1, 1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTLE

		source.writeByte(Byte.MAX_VALUE);

		buffer.replicate(source);
		assertEquals(buffer.readable(), 1);
		assertEquals(buffer, source);

		buffer.replicate(source, 0, 1);
		assertEquals(buffer.readable(), 2);
		// buffer.replicate(source, 1, 0);
		// assertEquals(buffer.readable(), 2);
		assertEquals(buffer.readByte(), Byte.MAX_VALUE);
		assertEquals(buffer.readByte(), Byte.MAX_VALUE);

		try {
			buffer.replicate(source, 0, 2);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		try {
			buffer.replicate(source, 1, 1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// MORE

		source.clear();
		int size = 65536;
		for (int index = 0; index < size; index++) {
			source.writeByte((byte) index);
		}

		buffer.replicate(source);
		assertEquals(buffer.readable(), size);
		for (int index = 0; index < size; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}
		assertEquals(buffer.readable(), 0);

		buffer.replicate(source, 0, 250);
		assertEquals(buffer.readable(), 250);
		buffer.replicate(source, 250, 250);
		assertEquals(buffer.readable(), 500);
		buffer.replicate(source, 500, size - 500);
		assertEquals(buffer.readable(), size);

		for (int index = 0; index < size; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}
		for (int index = 0; index < size; index++) {
			assertEquals(source.readByte(), (byte) index);
		}

		assertEquals(source.units(), 1);
		assertEquals(source.capacity(), DataBufferUnit.BYTES);
		assertEquals(source.readable(), 0);
		assertEquals(source.writeable(), 0);
		source.release();
	}

	@Test
	void testTransfer() {
		final DataBuffer target = DataBuffer.instance();

		// EMPTY

		buffer.append(target);
		assertEquals(buffer.readable(), 0);
		assertEquals(target.readable(), 0);

		buffer.transfer(target);
		assertEquals(buffer.readable(), 0);
		assertEquals(target.readable(), 0);

		// buffer.append(target, 0);
		// assertEquals(buffer.readable(), 0);
		// assertEquals(target.readable(), 0);

		// buffer.transfer(target, 0);
		// assertEquals(buffer.readable(), 0);
		// assertEquals(target.readable(), 0);

		try {
			buffer.transfer(target, 1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTLE

		buffer.writeByte(Byte.MAX_VALUE);
		buffer.transfer(target);
		assertEquals(buffer.readable(), 0);
		assertEquals(target.readable(), 1);

		buffer.writeByte(Byte.MAX_VALUE);
		// buffer.transfer(target, 0);
		buffer.transfer(target, 1);
		assertEquals(buffer.readable(), 0);
		assertEquals(target.readable(), 2);

		assertEquals(target.readByte(), Byte.MAX_VALUE);
		assertEquals(target.readByte(), Byte.MAX_VALUE);

		// MORE

		// 转移全部
		int size1 = 65536;
		for (int index = 0; index < size1; index++) {
			buffer.writeByte((byte) index);
		}
		buffer.transfer(target);
		assertEquals(target.readable(), size1);
		assertEquals(buffer.readable(), 0);

		for (int index = 0; index < size1; index++) {
			buffer.writeByte((byte) index);
		}
		buffer.transfer(target, size1);
		assertEquals(target.readable(), size1 + size1);
		assertEquals(buffer.readable(), 0);

		// 转移部分
		int size2 = 1250;
		for (int index = 0; index < size2; index++) {
			buffer.writeByte((byte) index);
		}
		buffer.transfer(target);
		assertEquals(target.readable(), size1 + size1 + size2);
		assertEquals(buffer.readable(), 0);

		for (int index = 0; index < size2; index++) {
			buffer.writeByte((byte) index);
		}
		buffer.transfer(target, size2);
		assertEquals(target.readable(), size1 + size1 + size2 + size2);
		assertEquals(buffer.readable(), 0);

		// 验证数据
		for (int index = 0; index < size1; index++) {
			assertEquals(target.readByte(), (byte) index);
		}
		for (int index = 0; index < size1; index++) {
			assertEquals(target.readByte(), (byte) index);
		}
		for (int index = 0; index < size2; index++) {
			assertEquals(target.readByte(), (byte) index);
		}
		for (int index = 0; index < size2; index++) {
			assertEquals(target.readByte(), (byte) index);
		}
		target.release();
	}

	@Test
	void testFileTransfer() throws IOException {
		final File file = File.createTempFile("joyzl", ".temp");
		final FileChannel channel = FileChannel.open(file.toPath(), //
			StandardOpenOption.CREATE, //
			StandardOpenOption.READ, //
			StandardOpenOption.WRITE, //
			StandardOpenOption.DELETE_ON_CLOSE);

		// EMPTY

		buffer.append(channel);
		assertEquals(buffer.readable(), 0);
		assertEquals(channel.size(), 0);

		buffer.transfer(channel);
		assertEquals(buffer.readable(), 0);
		assertEquals(channel.size(), 0);

		// buffer.append(channel, 0);
		// assertEquals(buffer.readable(), 0);
		// assertEquals(channel.size(), 0);
		//
		// buffer.transfer(channel, 0);
		// assertEquals(buffer.readable(), 0);
		// assertEquals(channel.size(), 0);

		try {
			buffer.append(channel, 1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		try {
			buffer.transfer(channel, 1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTLE

		buffer.writeByte(Byte.MAX_VALUE);

		buffer.transfer(channel);
		assertEquals(buffer.readable(), 0);
		assertEquals(channel.size(), 1);

		channel.position(0);
		buffer.append(channel);
		assertEquals(buffer.readable(), 1);
		assertEquals(channel.size(), 1);

		// buffer.transfer(channel, 0);
		buffer.transfer(channel, 1);
		assertEquals(buffer.readable(), 0);
		assertEquals(channel.size(), 2);

		channel.position(0);
		// buffer.append(channel, 0);
		buffer.append(channel, 1);
		buffer.append(channel, 1);
		assertEquals(buffer.readable(), 2);
		assertEquals(channel.size(), 2);

		assertEquals(buffer.readByte(), Byte.MAX_VALUE);
		assertEquals(buffer.readByte(), Byte.MAX_VALUE);

		// MORE

		for (int index = 0; index < 1250; index++) {
			buffer.writeByte((byte) index);
		}
		buffer.transfer(channel);
		assertEquals(buffer.readable(), 0);
		assertEquals(channel.size(), 1250 + 2);

		channel.position(2);
		buffer.append(channel);

		// buffer.transfer(channel, 0);
		buffer.transfer(channel, 1250 - 50);
		assertEquals(buffer.readable(), 50);
		buffer.transfer(channel, 50);
		assertEquals(buffer.readable(), 0);
		assertEquals(channel.size(), 2 + 1250 + 1250);

		channel.position(0);
		buffer.append(channel, 1252);
		assertEquals(buffer.readable(), 1252);
		buffer.append(channel, 1250);
		assertEquals(buffer.readable(), 2 + 1250 + 1250);

		// 验证数据
		final ByteBuffer b = ByteBuffer.allocate(1252 + 1250);
		channel.position(0);
		channel.read(b);
		b.flip();
		assertEquals(b.get(), Byte.MAX_VALUE);
		assertEquals(b.get(), Byte.MAX_VALUE);
		for (int index = 0; index < 1250; index++) {
			assertEquals(b.get(), (byte) index);
		}
		for (int index = 0; index < 1250; index++) {
			assertEquals(b.get(), (byte) index);
		}
		channel.close();
	}

	@Test
	void testCharBuffer() throws IOException {
		final CharBuffer c = CharBuffer.allocate(2048);

		// EMPTY

		buffer.append(CharBuffer.wrap(""), StandardCharsets.UTF_8);
		assertEquals(buffer.readable(), 0);
		buffer.transfer(c, StandardCharsets.UTF_8);

		// LITTLE

		buffer.append(CharBuffer.wrap("0123456789"), StandardCharsets.UTF_8);
		buffer.transfer(c, StandardCharsets.UTF_8);
		assertEquals(buffer.readable(), 0);

		buffer.append(CharBuffer.wrap("中华人民共和国"), StandardCharsets.UTF_8);
		buffer.transfer(c, StandardCharsets.UTF_8);
		assertEquals(buffer.readable(), 0);

		// MORE
		final StringBuilder b = new StringBuilder();
		for (int index = 0; index < 1500; index++) {
			b.append('曦');
		}

		buffer.append(CharBuffer.wrap(b), StandardCharsets.UTF_8);
		buffer.transfer(c, StandardCharsets.UTF_8);
		assertEquals(buffer.readable(), 0);

		// 验证数据
		c.flip();
		for (int index = 0; index < 10; index++) {
			assertEquals(c.get(), Character.forDigit(index, 10));
		}
		assertEquals(c.get(), '中');
		assertEquals(c.get(), '华');
		assertEquals(c.get(), '人');
		assertEquals(c.get(), '民');
		assertEquals(c.get(), '共');
		assertEquals(c.get(), '和');
		assertEquals(c.get(), '国');
		for (int index = 0; index < 1500; index++) {
			assertEquals(c.get(), '曦');
		}
	}

	@Test
	void testBack() {
		// EMPTY

		buffer.backSkip(0);
		try {
			buffer.backSkip(1);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		try {
			buffer.backByte();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTLE

		buffer.writeByte(Byte.MAX_VALUE);
		assertEquals(buffer.backByte(), Byte.MAX_VALUE);
		assertEquals(buffer.readable(), 0);

		buffer.writeByte(Byte.MAX_VALUE);
		buffer.writeByte(Byte.MIN_VALUE);
		buffer.backSkip(1);
		assertEquals(buffer.readable(), 1);
		buffer.backSkip(1);
		assertEquals(buffer.readable(), 0);

		// MORE

		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}
		buffer.backSkip(65536);
		assertEquals(buffer.readable(), 0);

		for (int index = 0; index < 65536; index++) {
			buffer.writeByte((byte) index);
		}

		buffer.backSkip(1500);
		assertEquals(buffer.readable(), 65536 - 1500);
		buffer.backSkip(1500);
		assertEquals(buffer.readable(), 65536 - 1500 - 1500);

		for (int index = 65536 - 1500 - 1500 - 1; index >= 0; index--) {
			assertEquals(buffer.backByte(), (byte) index);
		}
	}

	@Test
	void testMarkReset() throws IOException {
		// EMPTY

		buffer.mark();
		buffer.erase();
		buffer.reset();
		assertEquals(buffer.readable(), 0);

		// LITTLE

		buffer.writeByte(1);
		buffer.mark();
		buffer.readByte();
		buffer.reset();
		assertEquals(buffer.readable(), 1);

		buffer.mark();
		buffer.writeByte(2);
		buffer.reset();
		assertEquals(buffer.readable(), 1);

		buffer.mark();
		buffer.readByte();
		buffer.erase();
		assertEquals(buffer.readable(), 0);

		buffer.writeByte(1);
		buffer.mark();
		buffer.writeByte(2);
		buffer.erase();
		assertEquals(buffer.readable(), 2);

		buffer.readByte();
		buffer.readByte();
		buffer.reset();
		assertEquals(buffer.readable(), 0);

		// MORE

		buffer.clear();
		for (int index = 0; index < 1024 * 3; index++) {
			buffer.writeByte((byte) index);
		}

		buffer.mark();
		for (int index = 0; index < 1024 * 3; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}
		buffer.reset();
		assertEquals(buffer.readable(), 1024 * 3);

		buffer.mark();
		for (int index = 0; index < 1024 * 3; index++) {
			buffer.writeByte(index);
		}
		buffer.reset();
		assertEquals(buffer.readable(), 1024 * 3);

		buffer.mark();
		for (int index = 0; index < 1024; index++) {
			buffer.readByte();
			buffer.backByte();
		}
		buffer.erase();
		assertEquals(buffer.readable(), 1024);

		for (int index = 1024; index < 1024 * 2; index++) {
			assertEquals(buffer.readByte(), (byte) index);
		}
		assertEquals(buffer.readable(), 0);
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testMarkErase() throws IOException {
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
		assertEquals(buffer.units(), 1);
	}

	@Test
	void testSendReceive() {
		ByteBuffer b;
		ByteBuffer[] bs;

		// EMPTY

		b = buffer.write();
		assertEquals(b.remaining(), 1024);
		buffer.written(0);
		assertEquals(buffer.readable(), 0);

		bs = buffer.writes(0);
		assertEquals(bs.length, 1);
		buffer.written(0);
		assertEquals(buffer.readable(), 0);

		try {
			b = buffer.read();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		try {
			bs = buffer.reads();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTLE

		b = buffer.write();
		b.put(Byte.MAX_VALUE);
		buffer.written(1);
		assertEquals(buffer.readable(), 1);

		bs = buffer.writes(1);
		bs[0].put(Byte.MAX_VALUE);
		buffer.written(1);
		assertEquals(buffer.readable(), 2);

		b = buffer.read();
		assertEquals(b.get(), Byte.MAX_VALUE);
		buffer.read(1);
		assertEquals(buffer.readable(), 1);

		bs = buffer.reads();
		assertEquals(bs[0].get(), Byte.MAX_VALUE);
		buffer.read(1);
		assertEquals(buffer.readable(), 0);

		// MORE

		int value = 0;
		int size = 0;

		b = buffer.write();
		for (int index = 0; index < 1000; index++) {
			b.put((byte) value++);
			size++;
		}
		buffer.written(size);
		assertEquals(buffer.readable(), 1000);

		bs = buffer.writes(1500);
		int u = 0;
		for (int index = 0; index < 1250; index++) {
			if (bs[u].hasRemaining()) {
				bs[u].put((byte) value++);
				size++;
			} else {
				u++;
				bs[u].put((byte) value++);
				size++;
			}
		}
		buffer.written(size);
		assertEquals(buffer.readable(), 2250);

		value = 0;
		size = 0;

		b = buffer.read();
		while (b.hasRemaining()) {
			assertEquals(b.get(), (byte) value++);
			size++;
		}
		buffer.read(size);

		bs = buffer.reads();
		size = 0;
		u = 0;
		while (u < bs.length) {
			b = bs[u];
			while (b.hasRemaining()) {
				assertEquals(b.get(), (byte) value++);
				size++;
			}
			u++;
		}
		buffer.read(size);
		assertEquals(buffer.readable(), 0);

	}
}