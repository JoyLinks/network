/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.net.SocketAddress;

/**
 * 空链路
 * <p>
 * 创建空链路时可以指定null值的ChainHandler和key，空链路实例将不会执行任何网络操作；
 * </p>
 */
public class ChainEmpty<M> extends ChainChannel<M> {

	private final ChainHandler<M> handler;

	public ChainEmpty(ChainHandler<M> h, String k) {
		super(k);
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

	public final ChainHandler<M> handler() {
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
			final M message = handler().decode(this, null);
			handler().received(this, message);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(Object message) {
		try {
			final M m = (M) message;
			handler().encode(this, m);
			handler().sent(this, m);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	public void close() {
		try {
			handler().disconnected(this);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	public String getPoint() {
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