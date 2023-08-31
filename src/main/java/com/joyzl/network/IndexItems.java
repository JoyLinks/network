package com.joyzl.network;

import java.util.Iterator;

/**
 * 按索引存取的集合
 * 
 * @author ZhangXi
 * @date 2023年8月31日
 * @param <T>
 */
public class IndexItems<T> implements Iterator<T> {

	private int size, last;
	private final Object[] items;

	public IndexItems(int capacity) {
		items = new Object[capacity];
		size = 0;
		last = 0;
	}

	public boolean isFull() {
		return size == items.length;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public final int put(T item) {
		for (; last < items.length; last++) {
			if (items[last] == null) {
				items[last] = item;
				size++;
				return last;
			}
		}

		for (last = 0; last < items.length; last++) {
			if (items[last] == null) {
				items[last] = item;
				size++;
				return last;
			}
		}
		throw new IllegalStateException("集合空间已满");
	}

	/**
	 * 取出指定索引处的对象
	 */
	@SuppressWarnings("unchecked")
	public final T take(int index) {
		final T item = (T) items[index];
		items[index] = null;
		last = index;
		size--;
		return item;
	}

	@SuppressWarnings("unchecked")
	public final T peek(int index) {
		return (T) items[index];
	}

	////////////////////////////////////////////////////////////////////////////////

	private int index = -1;

	public Iterator<T> iterator() {
		index = -1;
		return this;
	}

	@Override
	public boolean hasNext() {
		while (++index < items.length) {
			if (items[index] != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T next() {
		return (T) items[index];
	}

	@Override
	public void remove() {
		items[index] = null;
	}
}