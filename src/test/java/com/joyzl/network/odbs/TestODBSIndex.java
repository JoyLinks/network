/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.odbs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import com.joyzl.network.odbs.ODBSIndex.MessageOddIndex;

public class TestODBSIndex {

	@Test
	void testIndex() {
		final ODBSIndex<Message> client = new ODBSIndex<>();

		Message message1 = new Message();
		message1.id = client.add(message1);
		assertEquals(message1.id, 0);
		assertEquals(client.size(), 1);
		assertEquals(client.isEmpty(), false);
		assertEquals(client.get(message1.id), message1);

		Message message2 = new Message();
		message2.id = client.add(message2);
		assertEquals(message2.id, 1);
		assertEquals(client.size(), 2);
		assertEquals(client.isEmpty(), false);
		assertEquals(client.get(message2.id), message2);

		Message message3 = new Message();
		message3.id = client.add(message3);
		assertEquals(message3.id, 2);
		assertEquals(client.size(), 3);
		assertEquals(client.isEmpty(), false);
		assertEquals(client.get(message3.id), message3);

		assertEquals(client.remove(0), message1);
		assertEquals(client.remove(1), message2);
		assertEquals(client.remove(2), message3);
	}

	@Test
	void testOddIndex() {
		final MessageOddIndex<Message> client = new MessageOddIndex<>();

		Message message1 = new Message();
		message1.id = client.add(message1);
		assertEquals(message1.id, 1);
		assertEquals(client.size(), 1);
		assertEquals(client.isEmpty(), false);
		assertEquals(client.get(message1.id), message1);

		Message message2 = new Message();
		message2.id = client.add(message2);
		assertEquals(message2.id, 3);
		assertEquals(client.size(), 2);
		assertEquals(client.isEmpty(), false);
		assertEquals(client.get(message2.id), message2);

		Message message3 = new Message();
		message3.id = client.add(message3);
		assertEquals(message3.id, 5);
		assertEquals(client.size(), 3);
		assertEquals(client.isEmpty(), false);
		assertEquals(client.get(message3.id), message3);

		assertEquals(client.remove(1), message1);
		assertEquals(client.remove(3), message2);
		assertEquals(client.remove(5), message3);
	}

	@Test
	void testIndexFlip() throws IOException {
		final ODBSIndex<Message> indexs = new ODBSIndex<>();
		final Message message = new Message();

		long time = System.currentTimeMillis();
		for (int index = 0; index < ODBSIndex.MAX; index++) {
			message.id = indexs.add(message);
			indexs.remove(message.id);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("MessageIndex Add&Remove:" + time / Integer.MAX_VALUE + "ms");

		message.id = indexs.add(message);
		assertEquals(message.id, ODBSIndex.MAX);
		message.id = indexs.add(message);
		assertEquals(message.id, 0);
	}

	@Test
	void testOddIndexFlip() throws IOException {
		final MessageOddIndex<Message> indexs = new MessageOddIndex<>();
		final Message message = new Message();

		long time = System.currentTimeMillis();
		for (int index = 0; index < ODBSIndex.MAX; index++) {
			message.id = indexs.add(message);
			indexs.remove(message.id);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("MessageIndex Add&Remove:" + time / Integer.MAX_VALUE + "ms");

		message.id = indexs.add(message);
		assertEquals(message.id, Integer.MAX_VALUE);
		message.id = indexs.add(message);
		assertEquals(message.id, 1);
	}

	@Test
	void testIterator() {
		final ODBSIndex<Message> stream = new ODBSIndex<>();

		// 添加消息
		Message message = null;
		for (int index = 0; index < stream.capacity(); index++) {
			message = new Message();
			message.id = stream.add(message);
		}

		// 遍历消息，且未移除任何消息
		int size = 0;
		for (Message m : stream) {
			if (m != null) {
				size++;
			}
		}
		assertEquals(stream.size(), size);

		// 移除尾部消息后遍历
		stream.remove(message);
		size = 0;
		for (Message m : stream) {
			if (m != null) {
				size++;
			}
		}
		assertEquals(stream.size(), size);

		// 移除中部消息后遍历
		message = stream.remove(10);
		size = 0;
		for (Message m : stream) {
			if (m != null) {
				size++;
			}
		}
		assertEquals(stream.size(), size);

		// 移除首部消息后遍历
		message = stream.remove(1);
		size = 0;
		for (Message m : stream) {
			if (m != null) {
				size++;
			}
		}
		assertEquals(stream.size(), size);

		// 遍历并移除
		final Iterator<Message> iterator = stream.iterator();
		while (iterator.hasNext()) {
			message = iterator.next();
			iterator.remove();
		}
		assertEquals(stream.size(), 0);
	}

	class Message {
		int id;
	}
}