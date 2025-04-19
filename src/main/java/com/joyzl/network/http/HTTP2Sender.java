package com.joyzl.network.http;

import java.util.Comparator;
import java.util.Iterator;

/**
 * HTTP2 流消息发送，考虑消息优先级，完全二叉树实现
 * 
 * @author ZhangXi 2025年4月16日
 */
class HTTP2Sender<M> implements Iterable<M>, Iterator<M> {

	// 流标识符零(0x0)用于连接控制消息
	// 客户端发起使用奇数 1357 Request
	// 服务端响应使用奇数 1357 Response
	// 服务端发起使用偶数 2468 Push
	// 流编号只增不减，用尽后GOAWAY

	// 其实就是多个请求或响应同时收发
	// 客户端首先用奇数流发起请求
	// 客户端请求发送完成后用相同奇数流接收响应
	// 服务端用奇数流接收请求
	// 服务端处理完成后用相同奇数流发送响应
	// 服务端推送用偶数流发起
	// 客户端指定服务器可以启动的最大并发流数
	// 服务器指定客户端可以启动的最大并发流数

	// 奇数转换为索引 (odd-1)/2=index
	// 偶数转换为索引 (even-2)/2=index

	private final Comparator<? super M> compareter;
	private M[] items;
	private int size;

	public HTTP2Sender(boolean server) {
		this(COMPARETER, server);
	}

	@SuppressWarnings("unchecked")
	public HTTP2Sender(Comparator<? super M> compareter, boolean server) {
		this.compareter = compareter;
		items = (M[]) new Object[100];
		if (server) {
			// 服务端使用偶数编号，从2开始;
			id = 2;
		} else {
			// 客户端使用奇数编号，从1开始；
			id = 1;
		}
	}

	public void add(M m) {
		if (size >= items.length) {
			throw new IllegalStateException("FULL");
		}

		int parent, index = size++;
		while (index > 0) {
			parent = (index - 1) / 2;
			if (compareter.compare(m, items[parent]) >= 0) {
				break;
			}
			items[index] = items[parent];
			index = parent;
		}
		items[index] = m;
	}

	public void remove(M m) {

	}

	/**
	 * 更新允许的并发流数量
	 */
	public void update(int size) {
		if (size != items.length) {
			@SuppressWarnings("unchecked")
			final M[] newItems = (M[]) new Object[size];
			System.arraycopy(items, 0, newItems, 0, items.length);
			items = newItems;
		}
	}

	final static Comparator<? super Object> COMPARETER = new Comparator<>() {
		@Override
		@SuppressWarnings("unchecked")
		public int compare(Object a, Object b) {
			return ((Comparable<Object>) a).compareTo(b);
		}
	};

	////////////////////////////////////////////////////////////////////////////////

	private int index = 0;

	@Override
	public Iterator<M> iterator() {
		return this;
	}

	/** 获取下一个消息 */
	@Override
	public M next() {
		return items[index++];
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////////

	private int id = 0;

	/** 获取流编号，到达最大值后变成负数 */
	public int nextId() {
		return id += 2;
	}

	/** 最大帧有效负载 */
	private int maxFrameSize;

	/**
	 * 获取当前并发流数量
	 */
	public int getMaxConcurrent() {
		return items.length;
	}

	public int getMaxFrameSize() {
		return maxFrameSize;
	}

	public void setMaxFrameSize(int value) {
		maxFrameSize = value;
	}
}