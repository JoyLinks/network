package com.joyzl.network.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话
 * 
 * <pre>
 * public final static Session&lt;User&gt; USER = new SessionRefreshable&lt;&gt;();
 * USER.set("session id / token", new User());
 * User user = USER.get("session id / token");
 * </pre>
 * 
 * @author ZhangXi 2025年2月16日
 */
public abstract class Session<T> {

	/** 所有实例化的会话容器，此列表用于守护进程 */
	private final static List<Session<?>> SESSIONS = new ArrayList<>();

	/** 会话过期检查 */
	public final static Runnable SESSION_DAEMON = new Runnable() {
		@Override
		public void run() {
			Session<?> session;
			final long timestamp = System.currentTimeMillis();
			try {
				for (int index = 0; index < SESSIONS.size(); index++) {
					session = SESSIONS.get(index);
					if (session.VALUES.isEmpty()) {
						continue;
					}
					session.check(timestamp);
				}
			} catch (Exception e) {

			}
		}

		@Override
		public String toString() {
			return "SESSION DAEMON";
		}
	};

	////////////////////////////////////////////////////////////////////////////////

	private final ConcurrentHashMap<Object, Timely<T>> VALUES = new ConcurrentHashMap<>();

	public Session() {
		synchronized (SESSIONS) {
			SESSIONS.add(this);
		}
	}

	/**
	 * 包装当前值为具有时效性检查接口的对象
	 * 
	 * @see Timely
	 * @return 如果值已实现时效性接口可直接返回
	 */
	protected abstract Timely<T> wrap(T t);

	/**
	 * 指定时戳检查时效性，过期对象将被移除
	 */
	protected void check(long timestamp) {
		final Iterator<Entry<Object, Timely<T>>> iterator = VALUES.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue().valid(timestamp)) {
				continue;
			} else {
				iterator.remove();
			}
		}
	}

	/**
	 * 指定键设置值，返回之前设置的值，如果已过期则返回空(null)
	 * 
	 * @param key 键可为任意对象，应确保有合理的哈希值(Object.hashCode())
	 * @param value 要设置的值
	 * @return 返回之前设置的值，如果已过期则返回空(null)
	 */
	public T set(Object key, T value) {
		final Timely<T> previous = VALUES.put(key, null);
		if (previous != null) {
			if (previous.valid(System.currentTimeMillis())) {
				return previous.value();
			}
		}
		return null;
	}

	/**
	 * 指定键获取值，如果已过期则返回空(null)
	 * 
	 * @param key 键可为任意对象，应确保有合理的哈希值(Object.hashCode())
	 * @return 键关联的值，如果已过期返回空(null)
	 */
	public T get(Object key) {
		final Timely<T> current = VALUES.get(key);
		if (current != null) {
			if (current.valid(System.currentTimeMillis())) {
				return current.value();
			}
		}
		return null;
	}

	/**
	 * 获取键值对数量
	 */
	public int size() {
		return VALUES.size();
	}
}