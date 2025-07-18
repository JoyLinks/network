/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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