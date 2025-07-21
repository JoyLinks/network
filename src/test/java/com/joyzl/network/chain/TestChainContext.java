/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class TestChainContext {

	private Chain chain = new Chain() {
		@Override
		public ChainType type() {
			return null;
		}

		@Override
		public void close() {
		}

		@Override
		public String point() {
			return null;
		}

		@Override
		public void reset() {
		}
	};

	@Test
	void test1() {
		// 直接对象获取

		chain.setContext("Test");
		chain.setContext(LocalDateTime.MIN);
		chain.setContext(this);

		assertEquals(chain.getContext(LocalDateTime.class), LocalDateTime.MIN);
		assertEquals(chain.getContext(String.class), "Test");
		assertEquals(chain.getContext(TestChainContext.class), this);

		chain.setContext("Test1");
		assertEquals(chain.getContext(String.class), "Test1");

		chain.setContext(LocalDateTime.MAX);
		assertEquals(chain.getContext(LocalDateTime.class), LocalDateTime.MAX);
	}

	@Test
	void test2() {
		// 继承关系获取

		Long value = System.currentTimeMillis();
		chain.setContext(value);

		assertEquals(chain.getContext(Long.class), value);
		assertEquals(chain.getContext(Number.class), value);
	}

	@Test
	void testTime() {
		final Map<String, String> map = new HashMap<>();
		map.put("Test", "Test");
		chain.setContext("Test");
		chain.setContext(System.currentTimeMillis());

		long time1 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			map.get("Test");
		}
		time1 = System.currentTimeMillis() - time1;

		long time2 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			chain.getContext(String.class);
		}
		time2 = System.currentTimeMillis() - time2;

		long time3 = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			chain.getContext(Number.class);
		}
		time3 = System.currentTimeMillis() - time3;

		System.out.println("Map:" + time1);
		System.out.println("Chain1:" + time2);
		System.out.println("Chain2:" + time3);
		// assertTrue(time1 > time2);
	}
}