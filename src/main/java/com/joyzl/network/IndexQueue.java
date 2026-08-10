/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * 数组实现具有固定空间的消息队列；提供基础队列功能先进先出；存入消息将自动分配索引，可通过索引取出消息；<br>
 * 只能通过索引取出已读取过的消息，
 * <p>
 * 如果消息包含资源（文件或输入流）且实现了Closeable接口，清空队列时将自动关闭。
 * </p>
 * <p>
 * 如果需要确保线程安全应通过额外的机制实现，此类的所有方法均不是线程安全的。
 * </p>
 * 
 * @author ZhangXi 2025年2月27日
 * @param <M>
 */
public class IndexQueue<M> implements Iterable<M> {

	private final int max;
	private final M[] elements;
	/**
	 * 为了消除头尾重叠歧义，索引范围为0 ~ max，实际索引通过模除获得 index % elements.length
	 * 逻辑存储形式：[head.read...foot]，head read foot 范围 0~max
	 */
	private int read = 0, head = 0, foot = 0;

	public IndexQueue() {
		this(64, Integer.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	public IndexQueue(int capacity, int max) {
		if (max < capacity) {
			throw new IllegalArgumentException("标识范围无效");
		}
		elements = (M[]) new Object[capacity];
		this.max = max;
	}

	/**
	 * 最大可用标识0~max，当标识到达最大值时将自动翻转为0
	 */
	public int max() {
		return max;
	}

	/**
	 * 队列容量
	 */
	public int capacity() {
		return elements.length;
	}

	/**
	 * 队列是否空，所有消息已发送且取出
	 */
	public boolean isEmpty() {
		return head == foot;
	}

	/**
	 * 队列是否空，所有消息已发送，不含未取出的消息
	 */
	public boolean isBlank() {
		return read == foot;
	}

	/**
	 * 消息数量，包括未取出的消息
	 */
	public int size() {
		if (foot > head) {
			return foot - head;
		}
		if (foot < head) {
			return max - head + foot;
		}
		return 0;
	}

	/**
	 * 排队消息数量，所有未读取消息，不含未取出的消息
	 */
	public int queue() {
		if (foot > read) {
			return foot - read;
		}
		if (foot < read) {
			return max - read + foot;
		}
		return 0;
	}

	/**
	 * 添加消息到队列尾部，并返回整数标记，可通过标记取出消息
	 */
	public int add(M m) {
		int index = foot % elements.length;
		if (elements[index] == null) {
			elements[index] = m;
			index = foot++;
			if (foot == max) {
				foot = 0;
			}
			return index;
		}
		throw new IllegalStateException("Message Deque FULL");
	}

	/**
	 * 获取队列中首部的消息，通过此方法获取的消息将驻留队列中
	 */
	public M peek() {
		if (head == foot) {
			return null;
		}
		return elements[head % elements.length];
	}

	/**
	 * 取出队列中首部的消息，通过此方法取出的消息不能再通过标记取出
	 */
	public M poll() {
		if (head == foot) {
			return null;
		}

		final int index = head % elements.length;
		final M m = elements[index];
		elements[index] = null;

		if (read == head) {
			read++;
			if (read == max) {
				read = 0;
			}
		}

		head++;
		if (head == max) {
			head = 0;
		}
		return m;
	}

	/** 读取当前对象 */
	public M read() {
		if (read == foot) {
			return null;
		}
		return elements[read % elements.length];
	}

	/** 读取位置推进到下一个 */
	public boolean next() {
		if (read < foot) {
			read++;
			return read < foot;
		}
		return false;
	}

	/**
	 * 根据标记取出消息，不能取出未读取的消息
	 * 
	 * @param index
	 * @return 如果消息已取出则返回空，如果索引无效返回空
	 */
	public M take(int index) {
		// 20260809 标记超限返回空不抛出异常
		// throw new IndexOutOfBoundsException("Invalid index " + index);

		// 取出中间位置时计数会不准确

		if (head < read) {
			// [.H...R...F.]
			if (index < head || index > read) {
				return null;
			}
			if (index == head) {
				head++;
				if (head == max) {
					head = 0;
				}
			}
		} else if (head > read) {
			// [.R...F...H.]
			if (index < head || index > read) {
				return null;
			}
			if (index == head) {
				head++;
				if (head == max) {
					head = 0;
				}
			}
		} else {
			// head==read
			if (read == foot) {
				// EMPTY
				return null;
			}
			if (index != read) {
				return null;
			} else {
				head++;
				if (head == max) {
					head = 0;
				}
				read = head;
			}
		}

		// if (index + 1 == foot) {
		// index = index % elements.length;
		// final M m = elements[index];
		// elements[index] = null;
		// foot--;
		// if (read >= foot) {
		// read = foot;
		// }
		// return m;
		// }

		index = index % elements.length;
		final M m = elements[index];
		elements[index] = null;
		return m;
	}

	/** 清除所有消息，如果消息携带资源并实现Closeable接口将自动关闭 */
	public void clear() throws IOException {
		read = head = foot = 0;
		for (int index = 0; index < elements.length; index++) {
			if (elements[index] != null) {
				if (elements[index] instanceof Closeable) {
					((Closeable) elements[index]).close();
				}
				elements[index] = null;
			}
		}
	}

	@Override
	public String toString() {
		return size() + ":" + head + '~' + read + '~' + foot;
	}

	////////////////////////////////////////////////////////////////////////////////
	// 提供队列遍历支持，且不会额外创建对象

	private int index;

	final Iterator<M> ITERATOR = new Iterator<M>() {
		@Override
		public boolean hasNext() {
			if (++index < foot) {
				return true;
			}
			return false;
		}

		@Override
		public M next() {
			return elements[index % elements.length];
		}

		@Override
		public void remove() {
			// 取出中间位置时计数会不准确
			// 未修订读取位置

			elements[index % elements.length] = null;
			if (index == head) {
				head++;
			} else if (index == foot - 1) {
				foot--;
			} else {

			}
		}
	};

	@Override
	public Iterator<M> iterator() {
		index = head - 1;
		return ITERATOR;
	}
}