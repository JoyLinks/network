package com.joyzl.network.session;

/**
 * 倒计时过期
 * 
 * @author ZhangXi 2025年2月17日
 */
public class Countdown<T> implements Timely<T> {

	private volatile long time;
	private T value;

	public Countdown(T value, int time) {
		this.value = value;
		this.time = time;
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