package com.joyzl.network.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TestHTTP2Stream {

	@Test
	void test() {
		System.out.println(Integer.MAX_VALUE + 2);
	}

	@Test
	void testServerReceiver() {
		final HTTP2ServerReceiver receiver = new HTTP2ServerReceiver();

		// 缓存
		final Request request1 = new Request();
		request1.setStream(1);
		receiver.add(request1, 1);
		final Request request3 = new Request();
		request3.setStream(3);
		receiver.add(request3, 3);
		final Request request5 = new Request();
		request5.setStream(5);
		receiver.add(request5, 5);
		// 获取
		assertEquals(receiver.size(), 3);
		assertEquals(receiver.isEmpty(), false);
		assertEquals(receiver.get(1), request1);
		assertEquals(receiver.get(3), request3);
		assertEquals(receiver.get(5), request5);
		// 移除
		assertEquals(receiver.remove(5), request5);
		assertEquals(receiver.remove(3), request3);
		assertEquals(receiver.remove(1), request1);
		assertEquals(receiver.isEmpty(), true);
		assertEquals(receiver.size(), 0);

		// 填满
		int id;
		Request request;
		for (int index = 4; index < 104; index++) {
			id = HTTP2ServerReceiver.indexOdd(index);
			request = new Request();
			request.setStream(id);
			receiver.add(request, id);
		}
		for (int index = 4; index < 104; index++) {
			id = HTTP2ServerReceiver.indexOdd(index);
			request = receiver.get(id);
			assertEquals(request.getStream(), id);
		}
		// 填超
		request = new Request();
		request.setStream(HTTP2ServerReceiver.indexOdd(105));
		try {
			receiver.add(request, request.getStream());
		} catch (Exception e) {
			assertNotNull(e);
		}

		// 遍历移除
		id = 0;
		receiver.iterator();
		while (receiver.hasNext()) {
			request = receiver.next();
			receiver.remove();
			if (request != null) {
				id++;
			}
		}
		assertEquals(id, 100);
		assertEquals(receiver.size(), 0);
		assertEquals(receiver.isEmpty(), true);
		// 遍历空
		receiver.iterator();
		assertEquals(receiver.hasNext(), false);

	}
}