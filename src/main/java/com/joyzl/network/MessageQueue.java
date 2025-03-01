package com.joyzl.network;

import java.util.Iterator;

/**
 * 固定数量的消息队列；提供基础队列功能顺序添加，顺序获取，按标记取出；<br>
 * 消息顺序获取并发送，消息驻留队列中，直至回复后通过标记取出；<br>
 * 存取标志从1开始，最大为255，重复使用，可通过单字节表示，0预留未加入队列的消息；<br>
 * 消息队列的容量固定为255，内部持有一个数组缓存消息；<br>
 * 如果需要确保线程安全应通过额外的机制实现，此类的所有方法均不是线程安全的。
 * <p>
 * 此消息队列提供两种模式：add-poll、add-peek-take；<br>
 * add-poll模式，与传统队列类似，添加消息到尾部，从首部取出；<br>
 * add-peek-take模式，添加消息到尾部，从首部取出发送（将标记一并发送给对端，对端应原样返回），
 * 发出的消息将驻留队列中，收到回复时按标记取出消息，如果标记的消息在一个周期后未取出将导致队列满异常。
 * </p>
 * 
 * @author ZhangXi 2025年2月27日
 * @param <M>
 */
public class MessageQueue<M> implements Iterator<M> {

	// 256*65536 = 16M
	private final Object[] elements = new Object[255];
	private int head, foot, size;

	// 为了消除头尾重叠歧义
	// 索引范围为0 ~ elements.length * 2 - 1
	// 实际索引通过模除获得 foot % elements.length
	// 极端情况：foot在经历255个消息周期后，如果第一个消息还未取出，将导致意外的队列满异常

	public MessageQueue() {
	}

	/**
	 * 消息数量，包括未取出的消息
	 */
	public int size() {
		return size;
	}

	/**
	 * 队列是否空，所有消息已发送且取出
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * 队列是否空，所有消息已发送，不含未取出的消息
	 */
	public boolean isBlank() {
		return head == foot;
	}

	/**
	 * 排队消息数量，所有未发送消息，不含未取出的消息
	 */
	public int queue() {
		return foot > head ? foot - head : head - foot;
	}

	/**
	 * 添加消息到队列尾部，并返回1~255的标记，可通过标记取出消息
	 */
	public int add(M m) {
		final int index = foot % elements.length;
		if (elements[index] == null) {
			elements[index] = m;
			size++;
			foot++;
			if (foot == elements.length * 2) {
				foot = 0;
			}
			return index + 1;
		}
		throw new IllegalStateException("Message Deque FULL");
	}

	/**
	 * 取出队列中首部的消息，通过此方法取出的消息不能再通过标记取出
	 */
	public M poll() {
		if (head == foot) {
			return null;
		}
		final int index = head % elements.length;
		@SuppressWarnings("unchecked")
		final M m = (M) elements[index];
		elements[index] = null;
		size--;
		head++;
		if (head == elements.length * 2) {
			head = 0;
		}
		return m;
	}

	/**
	 * 获取队列中首部的消息，通过此方法获取的消息将驻留队列中，等待通过标记取出
	 */
	public M peek() {
		if (head == foot) {
			return null;
		}
		final int index = head % elements.length;
		@SuppressWarnings("unchecked")
		final M m = (M) elements[index];
		head++;
		return m;
	}

	/**
	 * 根据标记取出消息
	 * 
	 * @param index
	 * @return 如果消息已取出则返回空
	 */
	public M take(int index) {
		index--;
		@SuppressWarnings("unchecked")
		final M m = (M) elements[index];
		if (m != null) {
			elements[index] = null;
			size--;
			return m;
		}
		return null;
	}

	public void clear() {
		head = foot = size = 0;
		for (int index = 0; index < elements.length; index++) {
			elements[index] = null;
		}
	}

	@Override
	public String toString() {
		return size + ":" + head + '~' + foot;
	}

	////////////////////////////////////////////////////////////////////////////////
	// 提供队列遍历支持，且不会额外创建对象，不保证顺序

	private int index;

	public Iterator<M> iterator() {
		index = -1;
		return this;
	}

	@Override
	public boolean hasNext() {
		while (++index < elements.length) {
			if (elements[index] != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public M next() {
		return (M) elements[index];
	}

	@Override
	public void remove() {
		elements[index] = null;
		size--;
	}
}