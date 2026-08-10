/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

class TestIndexQueue {

	@Test
	void testAddPoll() {
		final IndexQueue<Object> deque = new IndexQueue<>();

		// EMPTY

		assertEquals(deque.size(), 0);
		assertEquals(deque.peek(), null);
		assertEquals(deque.poll(), null);

		// LITTEL

		deque.add(1);
		assertEquals(deque.size(), 1);
		assertEquals(deque.peek(), 1);
		assertEquals(deque.poll(), 1);
		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);

		// MORE

		for (int index = 0; index < deque.capacity(); index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity());
		assertEquals(deque.peek(), 0);
		try {
			deque.add(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		for (int index = 0; index < deque.capacity(); index++) {
			assertEquals(deque.peek(), index);
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.poll(), null);
		assertEquals(deque.peek(), null);

		for (int index = 0; index < deque.capacity() / 2; index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity() / 2);
		assertEquals(deque.peek(), 0);

		for (int index = 0; index < deque.capacity() / 2; index++) {
			assertEquals(deque.peek(), index);
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.poll(), null);
		assertEquals(deque.peek(), null);

		for (int index = 0; index < deque.capacity(); index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity());
		assertEquals(deque.peek(), 0);
		try {
			deque.add(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		for (int index = 0; index < deque.capacity(); index++) {
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.poll(), null);
		assertEquals(deque.peek(), null);
	}

	@Test
	void testAddPeekTake() {
		final IndexQueue<Object> deque = new IndexQueue<>(256, 512);

		// EMPTY

		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);
		assertEquals(deque.peek(), null);

		assertNull(deque.take(0));
		assertNull(deque.take(256));

		// LITTEL

		int tag = deque.add(1);
		assertEquals(deque.size(), 1);
		assertEquals(deque.queue(), 1);

		assertEquals(deque.peek(), 1);
		assertEquals(deque.size(), 1);
		assertEquals(deque.queue(), 1);

		assertEquals(deque.take(tag), 1);
		assertEquals(deque.size(), 0);
		assertEquals(deque.take(tag), null);
		assertEquals(deque.queue(), 0);

		// MORE

		int tags[] = new int[256];
		for (int index = 0; index < 256; index++) {
			tags[index] = deque.add(index);
		}
		assertEquals(deque.size(), 256);
		assertEquals(deque.queue(), 256);
		assertEquals(deque.peek(), 0);

		for (int index = 0; index < 256; index++) {
			assertEquals(deque.take(tags[index]), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);
	}

	@Test
	void testIterator() {
		final IndexQueue<Object> deque = new IndexQueue<>(64, 256);

		// EMPTY

		Iterator<Object> iterator = deque.iterator();
		assertEquals(iterator.hasNext(), false);

		// LITTEL

		deque.add(1);

		// 遍历
		iterator = deque.iterator();
		assertEquals(iterator.hasNext(), true);
		assertEquals(iterator.next(), 1);

		// 遍历移除
		iterator = deque.iterator();
		assertEquals(iterator.hasNext(), true);
		assertEquals(iterator.next(), 1);
		iterator.remove();
		assertEquals(deque.size(), 0);

		// MORE

		for (int index = 0; index < deque.capacity(); index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity());

		int size = 0;
		iterator = deque.iterator();
		while (iterator.hasNext()) {
			assertNotNull(iterator.next());
			iterator.remove();
			size++;
		}
		assertEquals(deque.size(), 0);
		assertEquals(size, deque.capacity());
	}

	@Test
	void testMaxTag() {
		final IndexQueue<Object> deque = new IndexQueue<>(64, 65536);

		// 0~65534

		int tag = 0;
		for (int index = 0; index < deque.max() - 1; index++) {
			// SEND
			tag = deque.add(index);
			// ENCODE
			assertNotNull(deque.read());
			// DECODE
			assertNotNull(deque.take(tag));
		}
		assertEquals(tag, 65534);

		// 65535

		// SEND
		tag = deque.add(65535);
		// ENCODE
		assertNotNull(deque.read());
		// DECODE
		assertNotNull(deque.take(tag));

		assertEquals(tag, 65535);

		// 0

		// SEND
		tag = deque.add(0);
		// ENCODE
		assertNotNull(deque.read());
		// DECODE
		assertNotNull(deque.take(tag));

		assertEquals(tag, 0);
	}
}