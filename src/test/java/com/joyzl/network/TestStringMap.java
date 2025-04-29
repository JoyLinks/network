package com.joyzl.network;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

class TestStringMap {

	@Test
	void testTime() {
		final StringMap<Object> smap = new StringMap<>();
		final Map<String, Object> hmap = new HashMap<>();
		final Map<String, Object> tmap = new TreeMap<>();
		for (int index = 0; index < 100; index++) {
			hmap.put("K" + index, index);
			tmap.put("K" + index, index);
			smap.put("K" + index, index);
		}

		int count = Integer.MAX_VALUE;
		String key = "K99";
		long time = System.currentTimeMillis();
		while (count-- > 0) {
			hmap.get(key);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("HashMap 总耗时" + time);

		count = Integer.MAX_VALUE;
		time = System.currentTimeMillis();
		while (count-- > 0) {
			tmap.get(key);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("TreeMap 总耗时" + time);

		count = Integer.MAX_VALUE;
		time = System.currentTimeMillis();
		while (count-- > 0) {
			smap.get(key);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("StringMap 总耗时" + time);
	}

}