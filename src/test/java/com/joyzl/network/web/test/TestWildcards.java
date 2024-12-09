package com.joyzl.network.web.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.joyzl.network.web.Wildcards;

/**
 * Wildcards 相关测试
 * 
 * @author ZhangXi 2023年9月14日
 */
class TestWildcards {

	final static Wildcards<Object> WILDCARDS = new Wildcards<>();
	final static int SIZE = 1000;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// 测试样本
		WILDCARDS.bind("*", "*");
		WILDCARDS.bind("/*", "/*");
		WILDCARDS.bind("*test", "*test");
		WILDCARDS.bind("joyzl*test", "joyzl*test");

		// 测试样本:全字符匹配
		String text;
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			WILDCARDS.bind(text, text);
		}

		// 测试样本:前缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index) + Wildcards.ANY;
			WILDCARDS.bind(text, text);
		}

		// 测试样本:后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Wildcards.ANY + Integer.toString(index);
			WILDCARDS.bind(text, text);
		}

		// 测试样本:前缀和后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			text = text + Wildcards.ANY + text;
			WILDCARDS.bind(text, text);
		}
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	// 全字符匹配>前缀匹配>后缀匹配>前缀和后缀匹配

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testWildcards() {
		// 全字符匹配
		String text, value;
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = text;
			assertEquals(WILDCARDS.find(text), value);
		}

		// 前缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = text + Wildcards.ANY;
			assertEquals(WILDCARDS.find(text + "B"), value);
		}

		// 后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = Wildcards.ANY + text;
			assertEquals(WILDCARDS.find("A" + text), value);
		}

		// 前缀和后缀匹配
		for (int index = 0; index < SIZE; index++) {
			text = Integer.toString(index);
			value = text + Wildcards.ANY + text;
			assertEquals(WILDCARDS.find(text + "A" + text), value);
		}
		// 空字符
		assertEquals(WILDCARDS.find(""), "*");
	}

	@RepeatedTest(10000)
	void performance1() {
		assertEquals(WILDCARDS.find("A"), "*");
	}

	@Test
	void performance2() {
		long time = System.currentTimeMillis();
		int size = 10000;
		while (size-- > 0) {
			assertEquals(WILDCARDS.find("A"), "*");
		}
		time = System.currentTimeMillis() - time;
		System.out.println("耗时:" + time + "ms");
	}
}
