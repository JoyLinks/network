/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestLinkQueue {

	private LinkQueue<Object> deque = new LinkQueue<>(2);

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		deque.clear();
	}

	@Test
	void testAddPoll() {
		// EMPTY

		assertTrue(deque.isEmpty());
		assertEquals(deque.size(), 0);
		assertEquals(deque.peek(), null);
		assertEquals(deque.poll(), null);

		// LITTEL

		deque.add(1);
		assertFalse(deque.isEmpty());
		assertEquals(deque.size(), 1);
		assertEquals(deque.peek(), 1);
		assertEquals(deque.poll(), 1);
		assertEquals(deque.size(), 0);

		// MORE

		// ADD FULL
		for (int index = 0; index < deque.capacity(); index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity());
		assertEquals(deque.peek(), 0);
		assertFalse(deque.isEmpty());
		try {
			deque.add(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// POLL ALL
		for (int index = 0; index < deque.capacity(); index++) {
			assertEquals(deque.peek(), index);
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.poll(), null);
		assertEquals(deque.peek(), null);
		assertTrue(deque.isEmpty());

		// ADD HALF
		for (int index = 0; index < deque.capacity() / 2; index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity() / 2);
		assertEquals(deque.peek(), 0);
		assertFalse(deque.isEmpty());

		// POLL ALL
		for (int index = 0; index < deque.capacity() / 2; index++) {
			assertEquals(deque.peek(), index);
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.poll(), null);
		assertEquals(deque.peek(), null);
		assertTrue(deque.isEmpty());

		// ADD FULL
		for (int index = 0; index < deque.capacity(); index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity());
		assertEquals(deque.peek(), 0);
		assertFalse(deque.isEmpty());
		try {
			deque.add(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// POLL ALL
		for (int index = 0; index < deque.capacity(); index++) {
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.poll(), null);
		assertEquals(deque.peek(), null);
		assertTrue(deque.isEmpty());
	}

	@Test
	void testAddRemove() {
		// EMPTY

		assertTrue(deque.isEmpty());
		assertEquals(deque.size(), 0);
		deque.remove(0);

		// LITTEL

		deque.add(1);
		assertFalse(deque.isEmpty());
		assertEquals(deque.size(), 1);
		deque.remove(1);
		assertEquals(deque.size(), 0);
		assertTrue(deque.isEmpty());

		// MORE

		// ADD FULL
		for (int index = 0; index < deque.capacity(); index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity());
		assertFalse(deque.isEmpty());

		// REMOVE ALL
		for (int index = 0; index < deque.capacity(); index++) {
			deque.remove(index);
		}
		assertEquals(deque.size(), 0);
		assertTrue(deque.isEmpty());

		// ADD HALF
		for (int index = 0; index < deque.capacity() / 2; index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), deque.capacity() / 2);
		assertFalse(deque.isEmpty());

		// POLL ALL
		for (int index = 0; index < deque.capacity() / 2; index++) {
			deque.remove(index);
		}
		assertEquals(deque.size(), 0);
		assertTrue(deque.isEmpty());
	}

	@Test
	void testReadNext() {
		// EMPTY

		assertNull(deque.read());
		assertFalse(deque.next());

		// LITTEL

		deque.add(1);
		assertEquals(deque.read(), 1);
		assertFalse(deque.next());
		assertNull(deque.read());
		assertFalse(deque.next());

		assertEquals(deque.poll(), 1);
		assertNull(deque.read());
		assertFalse(deque.next());

		// MORE

		deque.add(1);
		deque.add(2);
		assertEquals(deque.read(), 1);
		assertTrue(deque.next());
		assertEquals(deque.read(), 2);
		assertFalse(deque.next());

		assertEquals(deque.poll(), 1);
		assertEquals(deque.poll(), 2);
		assertNull(deque.read());
		assertFalse(deque.next());

		deque.add(1);
		deque.add(2);
		deque.poll();
		assertEquals(deque.read(), 2);
		assertFalse(deque.next());
		assertEquals(deque.poll(), 2);
		assertNull(deque.read());
		assertFalse(deque.next());

		deque.add(1);
		assertEquals(deque.read(), 1);
		assertEquals(deque.poll(), 1);
		assertFalse(deque.next());
		deque.add(2);
		assertEquals(deque.read(), 2);
		assertEquals(deque.poll(), 2);
		assertFalse(deque.next());
		deque.add(3);
		assertEquals(deque.read(), 3);
		assertEquals(deque.poll(), 3);
		assertFalse(deque.next());
	}

	@Test
	void testIterator() {
		// EMPTY

		Iterator<Object> iterator = deque.iterator();
		assertEquals(iterator.hasNext(), false);

		// LITTEL

		deque.add(1);

		iterator = deque.iterator();
		assertEquals(iterator.hasNext(), true);
		assertEquals(iterator.next(), 1);

		iterator = deque.iterator();
		assertEquals(iterator.hasNext(), true);
		assertEquals(iterator.next(), 1);
		assertFalse(deque.isEmpty());

		iterator.remove();
		assertEquals(deque.size(), 0);
		assertTrue(deque.isEmpty());

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
}