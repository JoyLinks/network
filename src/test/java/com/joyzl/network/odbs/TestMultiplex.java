package com.joyzl.network.odbs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.joyzl.network.IndexMap;

class TestMultiplex {

	@Test
	void test() {
		final Map<Integer, Node> map = new HashMap<>();
		final IndexMap<Node> imp = new IndexMap<>(512);
		Node node = null;
		for (int index = 1; index <= 1000; index++) {
			node = new Node();
			node.id = index;
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
		System.out.println("总耗时" + time + "ms 单次" + u + "ms");
		assertEquals(node.id, tag);

		count = 0;
		time = System.currentTimeMillis();
		while (count++ < Integer.MAX_VALUE) {
			node = imp.get(tag);
		}
		time = System.currentTimeMillis() - time;
		u = time / 1000000.0;
		System.out.println("总耗时" + time + "ms 单次" + u + "ms");
		assertEquals(node.id, tag);
	}

	class Node {
		Object value;
		int id;
	}
}