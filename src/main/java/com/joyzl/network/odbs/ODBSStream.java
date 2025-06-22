/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.odbs;

import java.io.Closeable;
import java.io.IOException;

/**
 * 消息发送流，链表模式实现；<br>
 * 添加消息存入链表，保持顺序，可指定关联标识，可多次获取发送，直至移除；<br>
 * 消息关联的标识与业务无关，仅用于流式消息发送和接收。
 * <p>
 * 如果消息包含资源（文件或输入流）且实现了Closeable接口，清空流时将自动关闭。
 * </p>
 * <p>
 * {@link ODBSStream}对象用于流式发送，{@link ODBSIndex}用于流式接收。
 * </p>
 * 
 * @author ZhangXi 2025年4月11日
 */
public class ODBSStream<M> {

	// head始终为存储值头部,foot为已存储值节点的下一个节点
	// 移除值时空闲的节点将移除并连接到foot之后
	// 不能形成环形链表，将无法判断空和满状态
	// HEAD--FOOT-----END
	// head和foot之间是已缓存的值，且不允许空
	// foot和END之间式空闲的节点，值全部为空
	// prev和next用于流式发送，分别记录上一个节点和当前节点
	// 上一个节点用于移除当前流消息时将断开的后续节点连接在一起

	private final int capacity;
	private Item<M> head, foot;
	private Item<M> prev, next;
	private boolean done;
	private int size;

	/** 默认容量初始化消息流 */
	public ODBSStream() {
		this(128);
	}

	/** 指定容量初始化消息流 */
	public ODBSStream(int capacity) {
		this.capacity = capacity;
		head = foot = new Item<>();
		capacity--;
		while (capacity-- > 0) {
			foot.next = new Item<>();
			foot = foot.next;
		}
		foot.next = new Item<>();
		foot = head;
	}

	public int size() {
		return size;
	}

	/** 是否无消息 */
	public boolean isEmpty() {
		return head == foot;
	}

	/** 添加消息 */
	public void add(M m, int id) {
		if (foot.next == null) {
			throw new IllegalStateException("FULL");
		} else {
			foot.id = id;
			foot.value = m;
			foot = foot.next;
			size++;
		}
	}

	/** 移除指定消息 */
	public void remove(M m) {
		if (head.value == m) {
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
		prev = next = null;
		size = 0;
	}

	/** 清空并释放节点，释放的节点移动到所有空闲节点头部 */
	private Item<M> free(Item<M> item) {
		item.value = null;
		Item<M> next = item.next;
		item.next = foot.next;
		foot.next = item;
		return next;
	}

	/** 获取消息，只要还有消息此方法始终返回消息，直至全部移除，可通过remove()移除 */
	public M stream() {
		if (head == foot) {
			return null;
		}
		if (next == null) {
			next = head;
		} else {
			prev = next;
			next = next.next;
			if (next == foot) {
				next = head;
				prev = null;
			}
		}
		done = false;
		return next.value;
	}

	/**
	 * 获取当前消息标识，既stream()获取的消息
	 * <p>
	 * 消息流对象为每个存入的消息提供了独立的消息标识，这有助于消息实例用于多发场景；<br>
	 * 从客户端收到的消息关联客户端分配的奇数标识，回复时采用相同的消息标识；<br>
	 * 当此消息实例还要被转发给其他客户端或群发到多个客户端时，需要新的消息标识，<br>
	 * 此时新的偶数消息标识由特定链路分配，同时还要保留原来的消息标识。
	 * </p>
	 */
	public int id() {
		return next.id;
	}

	/**
	 * 标记当前消息已完成，既stream()获取的消息
	 * <p>
	 * 此方法辅助流式发送时标记状态，如果消息不具备编码状态标记可采用此方法，而无须改变消息类结构；<br>
	 * 当消息实例在多个链路群发时，即便有状态字段也会产生混淆，也应采用此方法。<br>
	 * 此状态是临时性的，当获取下一个消息时状态将被重置。
	 * </p>
	 */
	public void done() {
		done = true;
	}

	/** 获取当前消息是否被标记完成 */
	public boolean isDone() {
		return done;
	}

	/** 移除当前获取的消息，既stream()获取的消息 */
	public void remove() {
		if (next == null) {
			return;
		}
		if (next == head) {
			head = free(next);
			next = null;
			size--;
		} else {
			prev.next = free(next);
			next = prev;
			if (next == foot) {
				next = null;
			}
			size--;
		}
	}

	public int capacity() {
		return capacity;
	}

	static class Item<M> {
		Item<M> next;
		M value;
		int id;
	}
}