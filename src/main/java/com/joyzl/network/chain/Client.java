/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

/**
 * 链路客户端
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public abstract class Client extends ChainChannel {

	/** 消息处理对象 */
	private final ChainHandler handler;

	public Client(ChainHandler h) {
		super(Long.toString(System.currentTimeMillis() + System.nanoTime(), Character.MAX_RADIX));
		handler = h;
	}

	public final ChainHandler handler() {
		return handler;
	}

	protected abstract void connected();

	protected abstract void connected(Throwable e);

	protected abstract void received(int size);

	protected abstract void received(Throwable e);

	protected abstract void sent(int size);

	protected abstract void sent(Throwable e);
}