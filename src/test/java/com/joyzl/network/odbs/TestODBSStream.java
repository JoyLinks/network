package com.joyzl.network.odbs;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestODBSStream {

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