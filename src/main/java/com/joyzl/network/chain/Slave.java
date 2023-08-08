/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

/**
 * 链路客户端
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public abstract class Slave<M> extends ChainChannel<M> {

	private final Server<M> server;
	private long read, write;

	public Slave(Server<M> s, String k) {
		super(k);
		server = s;
	}

	public final Server<M> server() {
		return server;
	}

	public final ChainHandler<M> handler() {
		return server.handler();
	}

	protected void refreshLastRead() {
		read = System.currentTimeMillis();
	}

	protected void refreshLastWrite() {
		read = System.currentTimeMillis();
	}

	public long getLastRead() {
		return read;
	}

	public long getLastWrite() {
		return write;
	}
}