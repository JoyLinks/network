/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import com.joyzl.network.http.HTTP1;

class TestStringMap {

	@Test
	void testPutGet() {
		final StringMap<String> smap = new StringMap<>();
		assertEquals(smap.size(), 0);
		assertTrue(smap.isEmpty());

		smap.put("A", "A");
		assertEquals(smap.size(), 1);
		assertFalse(smap.isEmpty());
		assertEquals(smap.get("A"), "A");
		assertEquals(smap.get("a"), "A");
		assertTrue(smap.containsKey("A"));
		assertTrue(smap.containsKey("a"));
		assertTrue(smap.containsValue("A"));
		assertFalse(smap.containsValue("a"));

		smap.put("b", "B");
		assertEquals(smap.size(), 2);
		assertFalse(smap.isEmpty());
		assertEquals(smap.get("B"), "B");
		assertEquals(smap.get("b"), "B");
		assertTrue(smap.containsKey("B"));
		assertTrue(smap.containsKey("b"));
		assertTrue(smap.containsValue("B"));
		assertFalse(smap.containsValue("b"));

		assertEquals(smap.remove("A"), "A");
		assertEquals(smap.remove("b"), "B");
		assertEquals(smap.size(), 0);
		assertTrue(smap.isEmpty());
	}

	@Test
	void testPutAllGet() {
		final Map<String, String> hmap = new HashMap<>();
		for (String name : HTTP1.HEADERS) {
			hmap.put(name, name);
		}

		final StringMap<String> smap = new StringMap<>();
		smap.putAll(hmap);
		assertEquals(smap.size(), HTTP1.HEADERS.size());

		for (String name : HTTP1.HEADERS) {
			assertEquals(smap.get(name), name);
			assertEquals(smap.get(name.toLowerCase()), name);
			assertEquals(smap.get(name.toUpperCase()), name);
		}

		for (String name : HTTP1.HEADERS) {
			assertEquals(smap.remove(name), name);
		}
		assertEquals(smap.size(), 0);
	}

	@Test
	void testIterator() {
		final StringMap<String> smap = new StringMap<>();
		for (String name : HTTP1.HEADERS) {
			smap.put(name, name);
		}

		final Map<String, String> hmap = new HashMap<>();
		for (Entry<String, String> entry : smap) {
			hmap.put(entry.getKey().toString(), entry.getValue());
		}
		assertEquals(smap.size(), hmap.size());

		hmap.clear();
		for (String key : smap.keySet()) {
			hmap.put(key.toString(), key.toString());
		}
		assertEquals(smap.size(), hmap.size());

		hmap.clear();
		for (String value : smap.values()) {
			hmap.put(value, value);
		}
		assertEquals(smap.size(), hmap.size());

		final Iterator<Entry<String, String>> iterator = smap.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
		}
		assertEquals(smap.size(), 0);
	}

	@Test
	void testTime() {
		final StringMap<String> smap = new StringMap<>();
		final Map<String, String> hmap = new HashMap<>();
		for (String name : HTTP1.HEADERS) {
			hmap.put(name, name);
			smap.put(name, name);
		}

		String key = "Connection";
		int count = Integer.MAX_VALUE;

		long time = System.currentTimeMillis();
		while (count-- > 0) {
			hmap.get(key.toString());
		}
		time = System.currentTimeMillis() - time;
		System.out.println("HashMap   总耗时" + time);

		count = Integer.MAX_VALUE;
		time = System.currentTimeMillis();
		while (count-- > 0) {
			smap.get(key);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("StringMap 总耗时" + time);
	}
}