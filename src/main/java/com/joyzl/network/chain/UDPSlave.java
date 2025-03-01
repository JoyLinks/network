/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.SocketAddress;

import com.joyzl.network.Point;

/**
 * UDP通道
 *
 * @author ZhangXi 2019年7月9日
 *
 */
public class UDPSlave extends Slave {

	private final SocketAddress remote_address;

	public UDPSlave(UDPServer server, SocketAddress address) {
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
	}

	@Override
	protected void received(int size) {
	}

	@Override
	protected void received(Throwable e) {
	}

	@Override
	public void send(Object message) {
		try {
			((UDPServer) server()).send(this, message);
		} catch (IOException e) {
			sent(e);
		}
	}

	@Override
	protected void sent(int size) {
	}

	@Override
	protected void sent(Throwable e) {
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