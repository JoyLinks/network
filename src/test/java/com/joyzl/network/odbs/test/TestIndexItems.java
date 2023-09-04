package com.joyzl.network.odbs.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.IndexItems;

/**
 * 测试 {@link IndexItems}
 * 
 * @author ZhangXi
 * @date 2023年9月3日
 */
class TestIndexItems {

	static final IndexItems<Box> ITEMS = new IndexItems<>(Byte.MAX_VALUE);

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		ITEMS.clear();
		assertTrue(ITEMS.isEmpty());
		assertFalse(ITEMS.isFull());
	}

	@AfterEach
	void tearDown() throws Exception {
		assertTrue(ITEMS.isEmpty());
	}

	@Test
	void test1() {
		final List<Box> boxs = new ArrayList<>(Byte.MAX_VALUE);
		// 全部填充
		Box box;
		for (int index = 0; index < Byte.MAX_VALUE; index++) {
			box = new Box();
			box.index = ITEMS.put(box);
			boxs.add(box);
		}

		assertTrue(ITEMS.isFull());
		assertFalse(ITEMS.isEmpty());
		assertEquals(ITEMS.size(), Byte.MAX_VALUE);

		// 全部取出
		for (Box source : boxs) {
			box = ITEMS.take(source.index);
			assertEquals(box, source);
		}
	}

	@Test
	void test2() {
		final List<Box> boxs = new ArrayList<>(Byte.MAX_VALUE);

		// 部分填充
		Box box;
		for (int index = 0; index < 100; index++) {
			box = new Box();
			box.index = ITEMS.put(box);
			boxs.add(box);
		}

		assertFalse(ITEMS.isFull());
		assertFalse(ITEMS.isEmpty());
		assertEquals(ITEMS.size(), 100);

		// 部分取出
		for (Box source : boxs) {
			box = ITEMS.take(source.index);
			assertEquals(box, source);
		}
	}

	@Test
	void test3() {
		// 填充一个取出一个
		Box source, target;
		for (int index = 0; index < 256; index++) {
			source = new Box();
			source.index = ITEMS.put(source);

			target = ITEMS.take(source.index);
			assertEquals(source, target);
		}
	}

	@Test
	void test4() {
		final List<Box> boxs = new ArrayList<>(Byte.MAX_VALUE);

		// 部分填充
		Box box;
		for (int index = 0; index < 100; index++) {
			box = new Box();
			box.index = ITEMS.put(box);
			boxs.add(box);
		}

		assertFalse(ITEMS.isFull());
		assertFalse(ITEMS.isEmpty());
		assertEquals(ITEMS.size(), 100);

		// 部分取出
		for (Box source : boxs) {
			box = ITEMS.take(source.index);
			assertEquals(box, source);
		}

		boxs.clear();

		// 部分填充
		for (int index = 0; index < 100; index++) {
			box = new Box();
			box.index = ITEMS.put(box);
			boxs.add(box);
		}

		assertFalse(ITEMS.isFull());
		assertFalse(ITEMS.isEmpty());
		assertEquals(ITEMS.size(), 100);

		// 部分取出
		for (Box source : boxs) {
			box = ITEMS.take(source.index);
			assertEquals(box, source);
		}
	}

	@Test
	void test5() {
		Box source, target;
		final List<Box> boxs1 = new ArrayList<>(Byte.MAX_VALUE);
		final List<Box> boxs2 = new ArrayList<>(Byte.MAX_VALUE);

		// 部分填充
		for (int index = 0; index < 50; index++) {
			source = new Box();
			source.index = ITEMS.put(source);
			boxs1.add(source);
		}

		assertEquals(ITEMS.size(), boxs1.size());

		// 部分取出
		for (int index = 10; index < boxs1.size(); index++) {
			source = boxs1.get(index);
			target = ITEMS.take(source.index);
			assertEquals(source, target);
		}

		assertEquals(ITEMS.size(), 10);

		// 部分填充
		for (int index = 0; index < 50; index++) {
			source = new Box();
			source.index = ITEMS.put(source);
			boxs2.add(source);
		}

		assertEquals(ITEMS.size(), 60);

		// 部分取出1
		for (int index = 0; index < 10; index++) {
			source = boxs1.get(index);
			target = ITEMS.take(source.index);
			assertEquals(source, target);
		}

		// 部分取出2
		for (int index = 0; index < boxs2.size(); index++) {
			source = boxs2.get(index);
			target = ITEMS.take(source.index);
			assertEquals(source, target);
		}
	}

	class Box {
		int index;
		String value;
	}
}
