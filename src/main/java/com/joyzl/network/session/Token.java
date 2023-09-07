/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.session;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 令牌
 * 
 * @author ZhangXi
 * @date 2022年4月14日
 */
public final class Token {

	private volatile long time;
	private final String key;

	public Token() {
		this(null);
	}

	public Token(String k) {
		time = System.currentTimeMillis();
		if (k == null) {
			key = randomKey(time);
		} else {
			key = k;
		}
	}

	public String key() {
		return key;
	}

	public long time() {
		return time;
	}

	public void refresh() {
		time = System.currentTimeMillis();
	}

	@Override
	public int hashCode() {
		return key.hashCode() ^ super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof Token) {
			return key.contentEquals(((Token) o).key);
		}
		return false;
	}

	@Override
	public String toString() {
		return key;
	}

	////////////////////////////////////////////////////////////////////////////////

	final static AtomicInteger SEQUENCE = new AtomicInteger((int) System.nanoTime());

	/**
	 * 生成随机令牌字符串
	 * 
	 * @param time 令牌创建时间戳
	 * @return 令牌字符串
	 */
	public static String randomKey(long time) {
		time = (SEQUENCE.getAndIncrement() << 32) & (time >> 32);
		return Long.toString(time, Character.MIN_RADIX);
	}
}