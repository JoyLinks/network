package com.joyzl.network;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * 链表模式实现具有固定存储空间的消息队列；添加消息存入链表，保持先进先出顺序，可从头部取出；<br>
 * 提供额外读取支持，读取位置位于头部，可多次读取，直至移动到下一个位置，读取和移动不会移除消息；
 * 读取和队列存取是两个工作模式，可从头部读取直至尾部；也可以从头部取出直至尾部全部取出。
 * <p>
 * 此集合与标准Queue队列类似，但功能更加简化，专为消息排队发送并等待响应的情形而设计。
 * 消息请求通过读模式顺序发出，并已相同的顺序响应，通过队列模式获取头部消息匹配响应。
 * </p>
 * <p>
 * 如果消息包含资源（文件或输入流）且实现了Closeable接口，清空队列时将自动关闭。
 * </p>
 * <p>
 * 如果需要确保线程安全应通过额外的机制实现，此类的所有方法均不是线程安全的。
 * </p>
 * 
 * @author ZhangXi 2025年4月11日
 */
public class LinkQueue<M> implements Iterable<M> {

	private final int capacity;

	/**
	 * head始终为存储值头部,foot为已存储值节点的下一个节点，且始终无值；head和foot之间是已缓存的值，且不允许空；
	 * foot和END之间式空闲的节点，值全部为空；移除值时空闲的节点将移除并连接到foot之后；
	 * 链表不能形成环形，将无法判断空和满状态，形式如：HEAD--FOOT-----END
	 */
	private Item<M> head, foot;
	private Item<M> read;
	private int size;

	/** 默认容量初始化消息流 */
	public LinkQueue() {
		this(64);
	}

	/** 指定容量初始化消息流 */
	public LinkQueue(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException("队列容量无效");
		}
		this.capacity = capacity;
		read = head = foot = new Item<>();
		capacity--;
		while (capacity-- > 0) {
			foot.next = new Item<>();
			foot = foot.next;
		}
		foot.next = new Item<>();
		foot = head;
	}

	/** 消息数量 */
	public int size() {
		return size;
	}

	/** 队列容量 */
	public int capacity() {
		return capacity;
	}

	/** 是否空 */
	public boolean isEmpty() {
		return head == foot;
	}

	/** 添加消息 */
	public void add(M m) {
		if (foot.next == null) {
			throw new IllegalStateException("FULL");
		} else {
			foot.value = m;
			foot = foot.next;
			size++;
		}
	}

	/** 获取头部消息，消息不会移除 */
	public M peek() {
		return head.value;
	}

	/** 清空并释放节点，释放的节点移动到所有空闲节点头部 */
	private Item<M> free(Item<M> item) {
		item.value = null;
		Item<M> next = item.next;
		item.next = foot.next;
		foot.next = item;
		if (read == item) {
			read = next;
		}
		return next;
	}

	/** 取出头部消息，消息将被移除 */
	public M poll() {
		if (head == foot) {
			return null;
		}
		M m = head.value;
		head = free(head);
		size--;
		return m;
	}

	/** 移除指定消息 */
	public void remove(M m) {
		if (head == foot) {
			return;
		} else if (head.value == m) {
			head = free(head);
			size--;
		} else {
			Item<M> item = head;
			while (item.next != foot) {
				if (item.next.value == m) {
					item.next = free(item.next);
					size--;
					break;
				}
				item = item.next;
			}
		}
	}

	/** 清除所有消息，如果消息携带资源并实现Closeable接口将自动关闭 */
	public void clear() throws IOException {
		while (head != foot) {
			if (head.value instanceof Closeable) {
				((Closeable) head.value).close();
			}
			head = free(head);
		}
		read = head;
		size = 0;
	}

	static class Item<M> {
		Item<M> next;
		M value;
	}

	////////////////////////////////////////////////////////////////////////////////
	// 提供队列读取支持，可顺序读取，且不移除对象

	public M read() {
		return read.value;
	}

	public boolean next() {
		if (read == foot) {
			return false;
		} else {
			read = read.next;
			if (read == foot) {
				return false;
			}
			return true;
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	// 提供队列遍历支持，且不会额外创建对象，按添加顺序

	private Item<M> previous, current;

	@Override
	public Iterator<M> iterator() {
		previous = current = null;
		return ITERATOR;
	}

	final Iterator<M> ITERATOR = new Iterator<M>() {
		@Override
		public boolean hasNext() {
			if (current == null) {
				current = head;
			} else {
				previous = current;
				current = current.next;
			}
			if (current == foot) {
				return false;
			}
			return true;
		}

		@Override
		public M next() {
			return current.value;
		}

		@Override
		public void remove() {
			if (current == head) {
				head = free(current);
				current = null;
				size--;
			} else {
				previous.next = free(previous);
				current = previous;
				size--;
			}
		}
	};
}