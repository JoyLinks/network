/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

/**
 * 从链路
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public abstract class Slave extends ChainChannel {

	private final Server server;

	public Slave(Server s) {
		server = s;
	}

	public final Server server() {
		return server;
	}

	public final ChainHandler handler() {
		return server.handler();
	}
}