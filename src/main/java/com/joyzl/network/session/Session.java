/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.daemon.DaemonCheck;

/**
 * 网络会话状态
 * <p>
 * 必须预先定义所需的Session实例，不应让程序动态创建Session
 * 
 * <pre>
 * public final static Session&lt;User&gt; USER = new Session&lt;&gt;();
 * USER.set("session id / token", new User());
 * User user = USER.get("session id / token");
 * </pre>
 * 
 * @author ZhangXi
 * @date 2022年3月11日
 */
public final class Session<T> {

	// 实现方法：预先定义Session，每个Session内部维持一个Map；
	// 用户连接或登录生成会话Token，可在每个Session对应一个值；
	// 过期控制并不十分严格；

	private final static List<Session<?>> SESSIONS = new ArrayList<>();
	private final static Map<String, Token> TOKENS = new ConcurrentHashMap<>();

	/** 过期时间,默认1小时 */
	public final static long TIMEOUT = 1 * 60 * 60 * 1000;
	/** 会话过期检查 */
	public final static DaemonCheck DAEMON_CHECK = new DaemonCheck() {
		@Override
		public void check() {
			Session<?> session;
			Entry<String, Token> entry;
			final Iterator<Entry<String, Token>> iterator = TOKENS.entrySet().iterator();
			while (iterator.hasNext()) {
				entry = iterator.next();
				if (entry.getValue().isValid(TIMEOUT)) {
					continue;
				} else {
					iterator.remove();
					for (int index = 0; index < SESSIONS.size(); index++) {
						session = SESSIONS.get(index);
						session.remove(entry.getValue());
					}
				}
			}
		}

		@Override
		public String toString() {
			return "SESSION DAEMON";
		}
	};

	////////////////////////////////////////////////////////////////////////////////

	private final Map<Token, T> values = new ConcurrentHashMap<>();

	public Session() {
		SESSIONS.add(this);
	}

	public final T get(String token) {
		Token t = TOKENS.get(token);
		if (t == null) {
			return null;
		} else {
			return get(t);
		}
	}

	public final T set(String token, T value) {
		Token t = TOKENS.get(token);
		if (t == null) {
			t = new Token(token);
			TOKENS.put(token, t);
		}
		return set(t, value);
	}

	final T get(Token token) {
		token.refresh();
		return values.get(token);
	}

	final T set(Token token, T value) {
		token.refresh();
		return values.put(token, value);
	}

	final T remove(Token token) {
		return values.remove(token);
	}
}