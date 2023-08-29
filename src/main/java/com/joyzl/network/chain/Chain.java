/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 中翌智联（重庆）科技有限公司
 */
package com.joyzl.network.chain;

import java.net.SocketAddress;

/**
 * 链路
 *
 * <p>
 * Chain表示链路，对应具体的服务端和客户端，服务端还会根据连接创建子链路；
 * 服务端和客户端均通过ChainHandler处理数据，一个ChainHandler实例可以被多个Chain链路实例使用；
 * ChainHandler实现必须是多线程安全的，可以同时处理多个链路的网络数据包；
 * ChainHandler应根据业务情况，考虑将耗时的业务投递到业务线程进行处理；
 * ChainHandler必须根据业务逻辑调用Chain.receive()继续接收客户端后续数据；
 * </p>
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月9日
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
	 * 链路是否处于活动状态
	 *
	 * @return true 链路有效 / false 链路无效
	 */
	public abstract boolean active();

	/**
	 * 链路接收数据
	 */
	public abstract void receive();

	/**
	 * 通过链路发送对象
	 */
	public abstract void send(Object message);

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
	 * 获取链路关联的远程地址
	 * 
	 * @return null / SocketAddress
	 */
	public abstract SocketAddress getRemoteAddress();

	/**
	 * 获取链路关联的本地地址
	 */
	public abstract SocketAddress getLocalAddress();

	/**
	 * 获取链路关联的令牌
	 * 
	 * @return String /null
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String value) {
		token = value;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + key();
	}
}