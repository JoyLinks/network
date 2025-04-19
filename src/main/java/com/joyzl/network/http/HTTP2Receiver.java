package com.joyzl.network.http;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * HTTP2 流消息接收，接收消息时以流编号区分消息，不考虑消息优先级（优先级由发送方决定）
 * 
 * @author ZhangXi 2025年4月16日
 */
class HTTP2Receiver<M> implements Iterable<M>, Iterator<M> {

	private int capacity, size, max = -1;
	private M[] items;

	@SuppressWarnings("unchecked")
	public HTTP2Receiver() {
		items = (M[]) new Object[100];
	}

	public M get(int i) {
		if (i < 0) {
			throw new IndexOutOfBoundsException(i);
		}
		if (i > max) {
			return null;
		}
		return items[i % items.length];
	}

	public void add(M m, int i) {
		if (i <= max) {
			throw new IllegalArgumentException("不能添加旧的索引");
		}
		max = i;
		i = i % items.length;
		if (items[i] != null) {
			throw new IllegalStateException("EXISTS");
		}
		items[i] = m;
		size++;
	}

	public M remove(int i) {
		if (i < 0 || i > max) {
			throw new IndexOutOfBoundsException(i);
		}
		i = i % items.length;
		final M m = items[i];
		items[i] = null;
		size--;
		return m;
	}

	public void remove(M m) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == m) {
				items[i] = null;
				size--;
				break;
			}
		}
	}

	public void clear() throws IOException {
		size = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				if (items[i] instanceof Closeable) {
					((Closeable) items[i]).close();
				}
				items[i] = null;
			}
		}
	}

	public void capacity(int value) {

	}

	/**
	 * 是否还有消息
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * 容量，可并行消息数量
	 */
	public int capacity() {
		return capacity;
	}

	/**
	 * 消息数量
	 */
	public int size() {
		return size;
	}

	////////////////////////////////////////////////////////////////////////////////

	private int index;

	@Override
	public Iterator<M> iterator() {
		index = 0;
		return this;
	}

	@Override
	public boolean hasNext() {
		while (index < items.length) {
			if (items[index] == null) {
				index++;
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void remove() {
		items[index - 1] = null;
		size--;
	}

	@Override
	public M next() {
		return items[index++];
	}
}