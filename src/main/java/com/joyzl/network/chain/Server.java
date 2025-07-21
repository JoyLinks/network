/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * 链路服务端
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public abstract class Server extends ChainChannel {

	/** 消息处理对象 */
	private final ChainHandler handler;

	public Server(ChainHandler h) {
		handler = h;
	}

	public final ChainHandler handler() {
		return handler;
	}

	/** 获取服务端所有已连接链路 */
	public abstract Collection<Slave> slaves();

	/**
	 * 通过字符串形式的主机名或地址查找链路
	 * 
	 * @param host 地址或主机名
	 * @return Slave / null
	 */
	public Slave findSlave(String host) {
		// InetSocketAddress.getHostName:windows10.microdone.cn
		// InetSocketAddress.getHostString:windows10.microdone.cn
		// InetSocketAddress.getPort:52777
		// InetAddress.getCanonicalHostName:windows10.microdone.cn
		// InetAddress.getHostAddress:192.168.2.12
		// InetAddress.getHostName:windows10.microdone.cn

		InetSocketAddress isa;
		for (Slave slave : slaves()) {
			isa = (InetSocketAddress) slave.getRemoteAddress();
			if (host.equals(isa.getAddress().getHostAddress())) {
				return slave;
			}
			if (host.equals(isa.getHostName())) {
				return slave;
			}
		}
		return null;
	}

	/**
	 * 通过地址查找链路
	 * 
	 * @param address 远端地址
	 * @return Slave / null
	 */
	public Slave findSlave(InetAddress address) {
		InetSocketAddress isa;
		for (Slave slave : slaves()) {
			isa = (InetSocketAddress) slave.getRemoteAddress();
			if (address.equals(isa.getAddress())) {
				return slave;
			}
		}
		return null;
	}

	/**
	 * 群发给服务端的所有子链路
	 */
	@Override
	public void send(Object message) {
		if (active()) {
			for (Slave slave : slaves()) {
				if (slave.active()) {
					slave.send(message);
				}
			}
		}
	}

	/**
	 * 群发给服务端的指定子链路，由过滤器筛选
	 * 
	 * @param message 群发的消息
	 * @param filter 筛选过滤器
	 */
	public void send(Object message, Predicate<? super Slave> filter) {
		if (active()) {
			for (Slave slave : slaves()) {
				if (slave.active()) {
					if (filter.test(slave)) {
						slave.send(message);
					}
				}
			}
		}
	}

	/**
	 * 群发给服务端的指定子链路，由过滤器筛选，并指定排除（通常为群发发起链路）
	 * 
	 * @param message 群发的消息
	 * @param filter 筛选过滤器
	 * @param exclude 排除的链路
	 */
	public void send(Object message, Predicate<? super Slave> filter, Chain exclude) {
		if (active()) {
			for (Slave slave : slaves()) {
				if (exclude != slave) {
					if (slave.active()) {
						if (filter.test(slave)) {
							slave.send(message);
						}
					}
				}
			}
		}
	}
}