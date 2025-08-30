/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.odbs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestODBSStream {

	@Test
	void testEmpty() throws IOException {
		final ODBSStream<Message> stream = new ODBSStream<>(3);
		stream.stream();
		stream.remove();
		stream.remove(new Message(1));
		stream.clear();

	}

	@Test
	void testAddRemove() throws IOException {
		final ODBSStream<Message> stream = new ODBSStream<>(3);

		final Message message1 = new Message(1);
		final Message message2 = new Message(2);
		final Message message3 = new Message(3);

		stream.add(message1, 1);
		assertEquals(stream.isEmpty(), false);

		stream.remove(message1);
		assertEquals(stream.isEmpty(), true);

		stream.add(message1, 1);
		assertEquals(stream.isEmpty(), false);
		stream.add(message2, 2);
		assertEquals(stream.isEmpty(), false);
		stream.remove(message1);
		assertEquals(stream.isEmpty(), false);
		stream.remove(message2);
		assertEquals(stream.isEmpty(), true);

		stream.add(message1, 1);
		assertEquals(stream.isEmpty(), false);
		stream.add(message2, 2);
		assertEquals(stream.isEmpty(), false);
		stream.add(message3, 3);
		assertEquals(stream.isEmpty(), false);
		stream.remove(message1);
		assertEquals(stream.isEmpty(), false);
		stream.remove(message2);
		assertEquals(stream.isEmpty(), false);
		stream.remove(message3);
		assertEquals(stream.isEmpty(), true);

		stream.add(message1, 1);
		stream.add(message2, 2);
		stream.add(message3, 3);
		assertEquals(stream.isEmpty(), false);
		stream.clear();
		assertEquals(stream.isEmpty(), true);

		stream.add(message1, 1);
		stream.add(message2, 2);
		stream.add(message3, 3);
		try {
			stream.add(message1, 1);
		} catch (Exception e) {
			assertNotNull(e);
		}
		stream.clear();
		stream.clear();
		assertEquals(stream.isEmpty(), true);
	}

	@Test
	void testStream() throws IOException {
		final ODBSStream<Message> stream = new ODBSStream<>(3);

		final Message message1 = new Message(1);
		final Message message2 = new Message(2);
		final Message message3 = new Message(3);

		// 添加单个发送后移除
		stream.add(message1, 1);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		stream.remove();
		assertEquals(stream.isEmpty(), true);
		assertNull(stream.stream());

		// 添加多个发送后从尾部移除
		stream.add(message1, 1);
		stream.add(message2, 2);
		stream.add(message3, 3);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		assertEquals(stream.stream(), message2);
		assertEquals(stream.id(), 2);
		assertEquals(stream.stream(), message3);
		assertEquals(stream.id(), 3);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		assertEquals(stream.stream(), message2);
		assertEquals(stream.id(), 2);
		assertEquals(stream.stream(), message3);
		assertEquals(stream.id(), 3);
		stream.remove();
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		assertEquals(stream.stream(), message2);
		assertEquals(stream.id(), 2);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		assertEquals(stream.stream(), message2);
		assertEquals(stream.id(), 2);
		stream.remove();
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		stream.remove();
		assertNull(stream.stream());

		// 添加多个发送后从头部移除
		stream.add(message1, 1);
		stream.add(message2, 2);
		stream.add(message3, 3);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		stream.remove();
		assertEquals(stream.stream(), message2);
		assertEquals(stream.id(), 2);
		assertEquals(stream.stream(), message3);
		assertEquals(stream.id(), 3);
		assertEquals(stream.stream(), message2);
		assertEquals(stream.id(), 2);
		stream.remove();
		assertEquals(stream.stream(), message3);
		assertEquals(stream.id(), 3);
		assertEquals(stream.stream(), message3);
		assertEquals(stream.id(), 3);
		stream.remove();
		assertNull(stream.stream());

		// 添加多个发送后从中部移除
		stream.add(message1, 1);
		stream.add(message2, 2);
		stream.add(message3, 3);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		assertEquals(stream.stream(), message2);
		assertEquals(stream.id(), 2);
		stream.remove();
		assertEquals(stream.stream(), message3);
		assertEquals(stream.id(), 3);
		assertEquals(stream.stream(), message1);
		assertEquals(stream.id(), 1);
		stream.remove();
		assertEquals(stream.stream(), message3);
		assertEquals(stream.id(), 3);
		stream.remove();
		assertNull(stream.stream());

		// 添加后清空
		stream.add(message1, 1);
		stream.add(message2, 2);
		stream.add(message3, 3);
		stream.clear();
		stream.clear();
		assertNull(stream.stream());
	}

	class Message {
		int id;

		Message(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "ID:" + id;
		}
	}
}