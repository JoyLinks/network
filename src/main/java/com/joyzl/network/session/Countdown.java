/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.session;

/**
 * 倒计时过期缓存值包装类
 * 
 * @author ZhangXi 2025年2月17日
 */
class Countdown<T> implements Timely<T> {

	private final long time;
	private final T value;

	public Countdown(T value, int life) {
		this.value = value;
		this.time = System.currentTimeMillis() + life;
	}

	@Override
	public boolean valid(long timestamp) {
		return timestamp < time;
	}

	@Override
	public T value() {
		return value;
	}

	public long time() {
		return time;
	}
}