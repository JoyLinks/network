/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.session;

/**
 * 倒计时时间戳，超过时间未使用则过期移除
 * 
 * @author ZhangXi 2025年2月17日
 * @param <T>
 */
public class SessionCountdown<T> extends Session<T> {

	private final int life;

	/**
	 * 默认1小时过期
	 */
	public SessionCountdown() {
		life = Refreshable.HOUR;
	}

	/**
	 * 指定过期时间
	 * 
	 * @param life
	 */
	public SessionCountdown(int life) {
		this.life = life;
	}

	@Override
	protected Timely<T> wrap(T t) {
		return new Countdown<>(t, life);
	}
}