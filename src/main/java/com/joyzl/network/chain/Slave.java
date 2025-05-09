/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

/**
 * 链路客户端
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public abstract class Slave extends ChainChannel {

	private final Server server;

	public Slave(Server s, String k) {
		super(k);
		server = s;
	}

	public final Server server() {
		return server;
	}

	public final ChainHandler handler() {
		return server.handler();
	}

	protected abstract void received(int size);

	protected abstract void received(Throwable e);

	protected abstract void sent(int size);

	protected abstract void sent(Throwable e);
}