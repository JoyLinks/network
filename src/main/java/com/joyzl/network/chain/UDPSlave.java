/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.SocketAddress;

import com.joyzl.network.Point;

/**
 * UDP从链路，由UDPServer创建，记录对端地址
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
	public void send(Object message) {
		((UDPServer) server()).send(this, message);
	}

	@Override
	public void reset() {
		// UDP从连接与UDP Server共用通道,因此不能关闭通道
		try {
			handler().disconnected(this);
		} catch (Exception e) {
			handler().error(this, e);
		} finally {
			((UDPServer) server()).close(this);
		}
	}

	@Override
	public void close() {
		reset();
		try {
			clearContext();
		} catch (IOException e) {
			handler().error(this, e);
		}
	}
}