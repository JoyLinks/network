/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

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
	// 链路关联上下文
	private Object context;

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

	@SuppressWarnings("unchecked")
	public <T> T getContext() {
		return (T) context;
	}

	public void setContext(Object value) {
		context = value;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + key();
	}
}