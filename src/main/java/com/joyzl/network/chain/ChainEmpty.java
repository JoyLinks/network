/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * 空链路
 * <p>
 * 创建空链路时可以指定null值的ChainHandler和key，空链路实例将不会执行任何网络操作；
 * </p>
 */
public class ChainEmpty extends ChainChannel {

	private final ChainHandler handler;

	public ChainEmpty(ChainHandler h) {
		handler = h;
		connect();
	}

	void connect() {
		try {
			handler().connected(this);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	public final ChainHandler handler() {
		return handler;
	}

	@Override
	public ChainType type() {
		return ChainType.NONE;
	}

	@Override
	public boolean active() {
		return true;
	}

	@Override
	public void receive() {
		try {
			handler().received(this, handler().decode(this, null));
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	public void send(Object message) {
		try {
			handler().encode(this, message);
			handler().sent(this, message);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	public void reset() {
		try {
			handler().disconnected(this);
		} catch (Exception e) {
			handler().error(this, e);
		}
		try {
			clearContext();
		} catch (IOException e) {
			handler().error(this, e);
		}
	}

	@Override
	public void close() {
		reset();
	}

	@Override
	public String point() {
		return null;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public SocketAddress getLocalAddress() {
		return null;
	}
}