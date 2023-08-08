/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 *
 */
package com.joyzl.network.chain;

import java.net.SocketAddress;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 空链路
 * <p>
 * 创建空链路时可以指定null值的ChainHandler和key，空链路实例将不会执行任何网络操作； 也可通过ChainEmpty.INSTANCE获取空链路实例
 * </p>
 */
public class ChainEmpty<M> extends ChainChannel<M> {

	private final ChainHandler<M> handler;

	public ChainEmpty(ChainHandler<M> h, String k) {
		super(k);
		handler = h;
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
	}

	@Override
	public void send(Object message) {
		write(message);
	}

	@Override
	protected void write(Object writer) {
	}

	@Override
	protected void read(DataBuffer reader) {
		if (reader.readable() > 0) {
			M message;
			try {
				while (true) {
					message = handler().decode(this, reader);
					if (message == null) {
						break;
					} else {
						if (reader.reference()) {
							if (reader.hasResidue()) {
								// final DataBuffer buffer =
								// handler().receive(this);
								final DataBuffer buffer = DataBuffer.getB2048();
								reader.residue(buffer);
								reader.release();
								reader = buffer;

								handler().received(this, message);
							} else {
								reader.release();
								reader = null;

								handler().received(this, message);
								return;
							}
						} else {
							if (reader.hasResidue() || reader.readable() > 0) {
								handler().received(this, message);
								// 有剩余数据,继续尝试解包,继续接收数据
							} else {
								reader.release();
								reader = null;

								handler().received(this, message);
								return;
							}
						}
					}
				}
			} catch (Exception e) {
				if (reader != null) {
					reader.release();
					reader = null;
				}
				handler().error(this, e);
			}
		} else {
			reader.release();
			close();
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