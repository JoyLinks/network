/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.joyzl.network.IndexMap.Entry;

class TestIndexMap {

	@Test
	void test() {
		final IndexMap<Node> imp = new IndexMap<>();

		for (int index = 1; index <= 100; index++) {
			imp.put(index, new Node(index));
		}
		assertEquals(imp.size(), 100);

		for (int index = 1; index <= 100; index++) {
			assertEquals(imp.get(index).id, index);
		}

		for (int index = 1; index <= 100; index++) {
			assertTrue(imp.contains(index));
		}

		for (int index = 1; index <= 100; index++) {
			imp.put(index, new Node(index));
		}
		assertEquals(imp.size(), 100);

		int count = 0;
		for (Entry<Node> e : imp) {
			if (e.value() != null) {
				count++;
			}
		}
		assertEquals(count, imp.size());

		count = 0;
		final Iterator<Entry<Node>> iterator = imp.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			count++;
		}
		assertEquals(count, 100);
		assertEquals(imp.size(), 0);
	}

	@Test
	void testTime() {
		final Map<Integer, Node> map = new HashMap<>();
		final IndexMap<Node> imp = new IndexMap<>(512);
		Node node = null;
		for (int index = 1; index <= 1000; index++) {
			node = new Node(index);
			map.put(index, node);
			imp.put(index, node);
		}

		double u;
		int count = 0, tag = 999;

		long time = System.currentTimeMillis();
		while (count++ < Integer.MAX_VALUE) {
			node = map.get(tag);
		}
		time = System.currentTimeMillis() - time;
		u = time / 1000000.0;
		System.out.println("HashMap 总耗时" + time + "ms 单次" + u + "ms");
		assertEquals(node.id, tag);

		count = 0;
		time = System.currentTimeMillis();
		while (count++ < Integer.MAX_VALUE) {
			node = imp.get(tag);
		}
		time = System.currentTimeMillis() - time;
		u = time / 1000000.0;
		System.out.println("IndexMap 总耗时" + time + "ms 单次" + u + "ms");
		assertEquals(node.id, tag);
	}

	@Test
	void testOverflow() {
		int odd = Integer.MAX_VALUE;
		int eve = Integer.MAX_VALUE - 1;

		odd += 2;
		eve += 2;

		System.out.println(odd);
		System.out.println(eve);
	}

	class Node {
		int id;

		Node(int value) {
			id = value;
		}
	}
}