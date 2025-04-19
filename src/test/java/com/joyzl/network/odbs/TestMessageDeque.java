package com.joyzl.network.odbs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMessageDeque {

	private MessageQueue<Object> deque = new MessageQueue<>();

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

		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);
		assertEquals(deque.poll(), null);

		// LITTEL

		deque.add(1);
		assertEquals(deque.size(), 1);
		assertEquals(deque.queue(), 1);
		assertEquals(deque.poll(), 1);
		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);

		// MORE

		for (int index = 0; index < 255; index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), 255);
		assertEquals(deque.queue(), 255);
		try {
			deque.add(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		for (int index = 0; index < 255; index++) {
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);
		assertEquals(deque.poll(), null);

		for (int index = 0; index < 100; index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), 100);
		assertEquals(deque.queue(), 100);

		for (int index = 0; index < 100; index++) {
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);

		for (int index = 0; index < 255; index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), 255);
		assertEquals(deque.queue(), 255);
		try {
			deque.add(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		for (int index = 0; index < 255; index++) {
			assertEquals(deque.poll(), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.poll(), null);
		assertEquals(deque.queue(), 0);
	}

	@Test
	void testAddPeekTake() {
		// EMPTY

		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);
		assertEquals(deque.peek(), null);
		try {
			deque.take(0);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}
		try {
			deque.take(256);
			fail("应抛出异常");
		} catch (Exception e) {
			assertNotNull(e);
		}

		// LITTEL

		int tag = deque.add(1);
		assertEquals(deque.size(), 1);
		assertEquals(deque.queue(), 1);

		assertEquals(deque.peek(), 1);
		assertEquals(deque.size(), 1);
		assertEquals(deque.queue(), 0);

		assertEquals(deque.take(tag), 1);
		assertEquals(deque.size(), 0);
		assertEquals(deque.take(tag), null);
		assertEquals(deque.queue(), 0);

		// MORE

		int tags[] = new int[255];
		for (int index = 0; index < 255; index++) {
			tags[index] = deque.add(index);
		}
		assertEquals(deque.size(), 255);
		assertEquals(deque.queue(), 255);

		for (int index = 0; index < 255; index++) {
			assertEquals(deque.peek(), index);
		}
		assertEquals(deque.size(), 255);
		assertEquals(deque.queue(), 0);

		for (int index = 0; index < 255; index++) {
			assertEquals(deque.take(tags[index]), index);
		}
		assertEquals(deque.size(), 0);
		assertEquals(deque.queue(), 0);
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
		iterator.remove();
		assertEquals(deque.size(), 0);

		// MORE

		for (int index = 0; index < 255; index++) {
			deque.add(index);
		}
		assertEquals(deque.size(), 255);

		int size = 0;
		iterator = deque.iterator();
		while (iterator.hasNext()) {
			assertNotNull(iterator.next());
			iterator.remove();
			size++;
		}
		assertEquals(deque.size(), 0);
		assertEquals(size, 255);
	}
}