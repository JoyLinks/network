/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBufferUnit;

class TestDataBufferUnit {

	DataBufferUnit unit;

	@BeforeEach
	void setUp() throws Exception {
		unit = DataBufferUnit.get();
	}

	@AfterEach
	void tearDown() throws Exception {
		unit.release();
	}

	@Test
	void testTime() {
		int size = 100000;
		DataBufferUnit unit;
		long time = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			unit = DataBufferUnit.get();
			unit.release();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("DataBufferUnit GET:" + size + "次，耗时" + time + "毫秒");

		ByteBuffer bb;
		time = System.currentTimeMillis();
		for (int i = 0; i < size; i++) {
			bb = ByteBuffer.allocateDirect(DataBufferUnit.BYTES);
			bb.clear();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("ByteBuffer NEW:" + size + "次，耗时" + time + "毫秒");
	}

	@Test
	void testEmpty() {
		// 空状态
		assertEquals(unit.capacity(), DataBufferUnit.BYTES);
		assertEquals(unit.isBlank(), true);
		assertEquals(unit.isEmpty(), true);
		assertEquals(unit.isFull(), false);
		assertEquals(unit.readable(), 0);
		assertEquals(unit.readIndex(), 0);
		assertEquals(unit.writeable(), DataBufferUnit.BYTES);
		assertEquals(unit.writeIndex(), 0);

		assertEquals(unit.readSkip(0), 0);
		assertEquals(unit.readSkip(1), 0);
		assertEquals(unit.backSkip(0), 0);
		assertEquals(unit.backSkip(1), 0);

		ByteBuffer b;

		b = unit.receive();
		assertEquals(b.remaining(), DataBufferUnit.BYTES);
		assertEquals(unit.received(), 0);

		b = unit.send();
		assertEquals(b.remaining(), 0);
		assertEquals(unit.sent(), 0);

		try {
			unit.readByte();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		unit.mark();
		assertEquals(unit.marked(), false);
		unit.reset();
		assertEquals(unit.marked(), false);

		// buffer()
		// apart()
		// braek()
		// link(DataBufferUnit)
		// next(DataBufferUnit)
		// next()

		unit.clear();
	}

	@Test
	void testPart() {
		// 部分数据
		int size = DataBufferUnit.BYTES / 2;
		for (int index = 0; index < size; index++) {
			unit.writeByte((byte) index);
		}

		assertEquals(unit.isBlank(), false);
		assertEquals(unit.isEmpty(), false);
		assertEquals(unit.isFull(), false);
		assertEquals(unit.readable(), size);
		assertEquals(unit.readIndex(), 0);
		assertEquals(unit.writeable(), size);
		assertEquals(unit.writeIndex(), size);

		unit.mark();
		assertEquals(unit.readSkip(0), 0);
		assertEquals(unit.readSkip(10), 10);
		assertEquals(unit.readable(), size - 10);
		assertEquals(unit.backSkip(0), 0);
		assertEquals(unit.backSkip(10), 10);
		assertEquals(unit.readable(), size - 20);
		unit.reset();

		unit.mark();
		assertEquals(unit.readSkip(size), size);
		assertEquals(unit.readable(), 0);
		unit.reset();

		unit.mark();
		assertEquals(unit.backSkip(size), size);
		assertEquals(unit.readable(), 0);
		unit.reset();

		for (int index = 0; index < size; index++) {
			assertEquals(unit.get(index), (byte) index);
		}
		for (int index = 0; index < size; index++) {
			assertEquals(unit.readByte(), (byte) index);
		}

		// 部分数据读完
		assertEquals(unit.isBlank(), false);
		assertEquals(unit.isEmpty(), true);
		assertEquals(unit.isFull(), false);
		assertEquals(unit.readable(), 0);
		assertEquals(unit.readIndex(), size);
		assertEquals(unit.writeable(), size);
		assertEquals(unit.writeIndex(), size);

		//
		ByteBuffer b;
		unit.set(0, Byte.MIN_VALUE);
		unit.set(1, Byte.MAX_VALUE);
		assertEquals(unit.readable(), 0);
		unit.readIndex(0);
		unit.writeIndex(2);
		assertEquals(unit.readable(), 2);
		unit.mark();
		assertEquals(unit.readByte(), Byte.MIN_VALUE);
		assertEquals(unit.readByte(), Byte.MAX_VALUE);
		unit.reset();
		assertEquals(unit.readable(), 2);
		assertEquals(unit.readIndex(), 0);
		assertEquals(unit.writeIndex(), 2);
		assertEquals(unit.backByte(), Byte.MAX_VALUE);
		assertEquals(unit.backByte(), Byte.MIN_VALUE);
		unit.reset();
		b = unit.receive();
		b.put((byte) 0);
		assertEquals(unit.received(), 1);
		assertEquals(unit.readable(), 3);
		b = unit.send();
		assertEquals(b.get(), Byte.MIN_VALUE);
		assertEquals(b.get(), Byte.MAX_VALUE);
		assertEquals(unit.sent(), 2);
		assertEquals(unit.readable(), 1);
	}

	@Test
	void testFull() {
		ByteBuffer b;
		// 满载数据
		unit.clear();
		b = unit.receive();
		int size = DataBufferUnit.BYTES;
		for (int index = 0; index < size; index++) {
			b.put((byte) index);
		}
		assertEquals(unit.received(), size);

		assertEquals(unit.isBlank(), false);
		assertEquals(unit.isEmpty(), false);
		assertEquals(unit.isFull(), true);
		assertEquals(unit.readable(), size);
		assertEquals(unit.readIndex(), 0);
		assertEquals(unit.writeable(), 0);
		assertEquals(unit.writeIndex(), size);

		unit.mark();
		assertEquals(unit.readSkip(0), 0);
		assertEquals(unit.readSkip(10), 10);
		assertEquals(unit.readable(), size - 10);
		assertEquals(unit.backSkip(0), 0);
		assertEquals(unit.backSkip(10), 10);
		assertEquals(unit.readable(), size - 20);
		unit.reset();

		unit.mark();
		assertEquals(unit.readSkip(size), size);
		assertEquals(unit.readable(), 0);
		unit.reset();

		unit.mark();
		assertEquals(unit.backSkip(size), size);
		assertEquals(unit.readable(), 0);
		unit.reset();

		b = unit.send();
		for (int index = 0; index < size; index++) {
			assertEquals(b.get(), (byte) index);
		}
		assertEquals(unit.sent(), size);
		try {
			unit.writeByte((byte) 0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// send-sent,receive-received
		b = unit.receive(1);
		assertEquals(b.remaining(), 0);
		unit.clear();

		b = unit.receive(0);
		assertEquals(b.remaining(), 0);
		assertEquals(unit.received(), 0);
		assertEquals(unit.readable(), 0);

		b = unit.send(0);
		assertEquals(b.remaining(), 0);
		assertEquals(unit.sent(), 0);
		assertEquals(unit.readable(), 0);

		b = unit.receive(2);
		assertEquals(b.remaining(), 2);
		b.put(Byte.MAX_VALUE);
		b.put(Byte.MAX_VALUE);
		try {
			b.put(Byte.MAX_VALUE);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		assertEquals(unit.received(), 2);
		assertEquals(unit.readable(), 2);

		b = unit.receive(1024);
		assertEquals(b.remaining(), 1022);
		assertEquals(unit.received(), 0);

		b = unit.send(2);
		assertEquals(b.remaining(), 2);
		b.get();
		b.get();
		try {
			b.get();
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		assertEquals(unit.sent(), 2);
		assertEquals(unit.readable(), 0);

		b = unit.send(1024);
		assertEquals(b.remaining(), 0);
		assertEquals(unit.sent(), 0);
	}
}