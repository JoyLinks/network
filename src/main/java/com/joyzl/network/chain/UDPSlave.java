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

import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * UDP通道
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月9日
 *
 */
public class UDPSlave<M> extends Slave<M> {

	private final SocketAddress remote_address;

	public UDPSlave(UDPServer<M> server, SocketAddress address) {
		super(server, Point.getPoint(address));
		remote_address = address;
	}

	@Override
	public ChainType type() {
		return ChainType.UDP_SLAVE;
	}

	@Override
	public boolean active() {
		return server().active();
	}

	@Override
	public String getPoint() {
		return Point.getPoint(remote_address);
	}

	@Override
	public SocketAddress getLocalAddress() {
		return server().getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return remote_address;
	}

	@Override
	public void receive() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void read(DataBuffer reader) {
		reader.release();
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(Object message) {
		if (messages().isEmpty()) {
			messages().addLast((M) message);
			if (message == messages().peekFirst()) {
				write(message);
			}
		} else {
			messages().addLast((M) message);
		}

		// if (message == null) {
		// } else {
		// try {
		// handler().send(this, (M) message);
		// } catch (Exception e) {
		// handler().error(this, e);
		// }
		// }
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void write(Object message) {
		if (message == null) {
			// 消息全部发送完成
		} else {
			DataBuffer buffer = null;
			try {
				buffer = handler().encode(this, (M) message);
				if (buffer == null) {
					if (messages().remove(message)) {
						handler().sent(this, (M) message);
						// 继续发送队列中的消息
						write(messages().peekFirst());
					} else {
						throw new IllegalStateException("怎么会出现不在队列中的消息呢" + message);
					}
				} else if (buffer.readable() <= 0) {
					throw new IllegalStateException("未编码数据 " + message);
				} else {
					((UDPServer<M>) server()).write(buffer, this);
					write(message);
				}
			} catch (Exception e) {
				if (buffer != null) {
					buffer.release();
				}
				handler().error(this, e);
			}
		}
	}

	@Override
	public void close() {
		server().offSlave(this);

		// UDP从连接与UDP Server共用通道,因此不能关闭通道
		try {
			handler().disconnected(this);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}
}