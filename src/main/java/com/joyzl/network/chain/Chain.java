/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.Closeable;
import java.io.IOException;

/**
 * 链路
 * 
 * @author ZhangXi 2019年7月9日
 *
 */
public abstract class Chain {

	// 链路类型，由业务类型指定
	private volatile int type;
	// 链路令牌
	private volatile String token;

	/**
	 * 链路类型
	 * 
	 * @return {@link ChainType}
	 */
	public abstract ChainType type();

	/**
	 * 获取链路接点
	 */
	public abstract String point();

	/**
	 * 重置链路
	 * <p>
	 * 服务端的重置与关闭行为通常相同，客户端如果具有重连机制则重置后可继续尝试连接。
	 * 因此消息处理的内部逻辑通常应调用reset()，而外部逻辑应调用close()表示无须尝试继续连接。
	 */
	public abstract void reset();

	/**
	 * 关闭链路
	 */
	public abstract void close();

	/**
	 * 获取链路类型
	 */
	public final int getType() {
		return type;
	}

	/**
	 * 设置链路类型
	 *
	 * @param value > 0
	 */
	public final void setType(int value) {
		type = value;
	}

	/**
	 * 获取链路关联的令牌
	 */
	public String getToken() {
		return token;
	}

	/**
	 * 设置链路关联的令牌
	 */
	public void setToken(String value) {
		token = value;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + point();
	}

	////////////////////////////////////////////////////////////////////////////////
	// 支持链路关联多个对象实例

	private Context context;

	/** 获取是否至少有一个关联对象实例 */
	public boolean hasContext() {
		return context != null;
	}

	/**
	 * 获取链路关联的对象实例，如果找不到指定类型的实例将返回空(null)
	 */
	public <T> T getContext(Class<T> cs) {
		if (context == null) {
			return null;
		} else {
			return context.get(cs);
		}
	}

	/**
	 * 设置链路关联的对象实例，类型相同的对象实例将被覆盖，不能设置空(null)
	 */
	public void setContext(Object value) {
		if (value == null) {
			// 链路关联实例采用类型标识因此不能设置null
			throw new NullPointerException();
		}
		if (context == null) {
			context = new Context(value);
		} else {
			context.put(value);
		}
	}

	/**
	 * 移除设置的关联对象实例
	 */
	@SuppressWarnings("unchecked")
	public <T> T removeContext(Class<T> cs) {
		if (context == null) {
			return null;
		}
		if (context.item.getClass() == cs) {
			final Object item = context.item;
			context = context.next;
			return (T) item;
		} else {
			Context c = context;
			while (c.next != null) {
				if (c.next.item.getClass() == cs) {
					final Object item = c.next.item;
					c.next = c.next.next;
					return (T) item;
				}
				c = c.next;
			}
			return null;
		}
	}

	/**
	 * 清除链路关联的对象实例，如果对象实现了{@link Closeable}接口将被关闭；<br>
	 * 链路关闭时此方法被自动调用，通常无须手动清除。
	 */
	public void clearContext() throws IOException {
		while (context != null) {
			if (context.item instanceof Closeable) {
				((Closeable) context.item).close();
			}
			context = context.next;
		}
	}

	/**
	 * 链路上下文对象，可绑定对象实例到链路，每种对象只能绑定单个实例，已链表方式缓存
	 * 
	 * @author ZhangXi 2025年3月6日
	 */
	private class Context {
		private Context next;
		private Object item;

		public Context(Object value) {
			item = value;
		}

		public void put(Object value) {
			if (value.getClass() == item.getClass()) {
				item = value;
			} else if (next == null) {
				next = new Context(value);
			} else {
				next.put(value);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T get(Class<T> cs) {
			if (item.getClass() == cs) {
				return (T) item;
			} else if (cs.isInstance(item)) {
				return (T) item;
			} else if (next != null) {
				return next.get(cs);
			} else {
				return null;
			}
		}
	}
}