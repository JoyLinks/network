package com.joyzl.network.session;

/**
 * 可刷新时间
 * 
 * @author ZhangXi 2025年2月17日
 */
public class Refreshable<T> implements Timely<T> {

	/** 1小时 */
	public final static int HOUR = 1 * 60 * 60 * 1000;

	private final int life;
	private volatile long time;
	private T value;

	public Refreshable(T value) {
		time = System.currentTimeMillis();
		life = HOUR;
		this.value = value;
	}

	public Refreshable(T value, int life) {
		time = System.currentTimeMillis();
		this.value = value;
		this.life = life;
	}

	@Override
	public boolean valid(long timestamp) {
		return timestamp - time > life;
	}

	@Override
	public T value() {
		time = System.currentTimeMillis();
		return value;
	}

	public long time() {
		return time;
	}
}