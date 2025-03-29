/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
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

	// 链路标识
	private final String key;
	// 链路类型，由业务类型指定
	private volatile int type;
	// 链路令牌
	private volatile String token;

	/**
	 * 创建新链路
	 *
	 * @param h 链路的数据处理对象
	 * @param k 链路连接接点标识
	 */
	public Chain(String k) {
		key = k;
	}

	/**
	 * 链路标识KEY
	 * <p>
	 * 在同一类型链路中，KEY唯一标识一个链路接点，例如：TCP链路中的192.168.0.1:1031
	 *
	 * @return String
	 */
	public final String key() {
		return key;
	}

	/**
	 * 链路类型
	 * 
	 * @return {@link ChainType}
	 */
	public abstract ChainType type();

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
	 * 获取链路接点
	 */
	public abstract String getPoint();

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
		return this.getClass().getSimpleName() + " " + key();
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
			throw new NullPointerException();
		}
		if (context == null) {
			context = new Context(value);
		} else {
			context.put(value);
		}
	}

	/**
	 * 清除链路关联的对象实例，如果对象实现了{@link Closeable}接口将被关闭；<br>
	 * 链路关闭时此方法被自动调用，通常无须手动清除，特殊情形例外。
	 */
	public void clearContext() throws IOException {
		while (context != null) {
			if (context.o instanceof Closeable) {
				((Closeable) context.o).close();
			}
			context = context.c;
		}
	}

	/**
	 * 链路上下文对象，可绑定对象实例到链路，每种对象只能绑定单个实例，已链表方式缓存
	 * 
	 * @author ZhangXi 2025年3月6日
	 */
	private class Context {
		private Context c;
		private Object o;

		public Context(Object value) {
			o = value;
		}

		public void put(Object value) {
			if (value.getClass() == o.getClass()) {
				o = value;
			} else if (c == null) {
				c = new Context(value);
			} else {
				c.put(value);
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T get(Class<T> cs) {
			if (o.getClass() == cs) {
				return (T) o;
			} else if (cs.isInstance(o)) {
				return (T) o;
			} else if (c != null) {
				return c.get(cs);
			} else {
				return null;
			}
		}
	}
}