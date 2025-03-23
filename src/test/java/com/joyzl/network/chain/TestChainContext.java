package com.joyzl.network.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TestChainContext {

	private Chain chain = new Chain(null) {
		@Override
		public ChainType type() {
			return null;
		}

		@Override
		public void close() {
		}

		@Override
		public String getPoint() {
			return null;
		}

		@Override
		public void reset() {
		}
	};

	@Test
	void test() {
		chain.setContext("Test");
		final Object o1 = new Object();
		chain.setContext(o1);
		chain.setContext(this);

		assertEquals(chain.getContext(Object.class), o1);
		assertEquals(chain.getContext(String.class), "Test");
		assertEquals(chain.getContext(TestChainContext.class), this);

		chain.setContext("Test1");
		assertEquals(chain.getContext(String.class), "Test1");

		final Object o2 = new Object();
		chain.setContext(o2);
		assertEquals(chain.getContext(Object.class), o2);
	}

	@Test
	void testTime() {
		final Map<String, String> map = new HashMap<>();
		map.put("Test", "Test");

		final Object o = new Object();
		chain.setContext(o);

		long time1 = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			map.get("Test");
		}
		time1 = System.currentTimeMillis() - time1;

		long time2 = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			chain.getContext(Object.class);
		}
		time2 = System.currentTimeMillis() - time2;

		System.out.println("Map:" + time1);
		System.out.println("Chain:" + time2);
		assertTrue(time1 > time2);
	}
}