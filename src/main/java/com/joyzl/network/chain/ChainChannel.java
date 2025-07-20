/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.net.SocketAddress;

/**
 * 链路输入输出支持
 * <p>
 * Chain表示链路，对应具体的服务端和客户端，服务端还会根据连接创建子链路；
 * 服务端和客户端均通过ChainHandler处理数据，一个ChainHandler实例可以被多个Chain链路实例使用；
 * ChainHandler实现必须是多线程安全的，可以同时处理多个链路的网络数据包；
 * ChainHandler应根据业务情况，考虑将耗时的业务投递到业务线程进行处理；
 * ChainHandler必须根据业务逻辑调用Chain.receive()继续接收客户端后续数据；
 * </p>
 * 
 * @author ZhangXi 2019年7月20日
 *
 */
public abstract class ChainChannel extends Chain {

	public ChainChannel(String k) {
		super(k);
	}

	/**
	 * 链路是否处于活动状态
	 *
	 * @return true 链路有效 / false 链路无效
	 */
	public abstract boolean active();

	/**
	 * 链路开始接收数据
	 */
	public abstract void receive();

	/**
	 * 通过链路发送对象
	 */
	public abstract void send(Object message);

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
}