/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;

import com.joyzl.network.Executor;
import com.joyzl.network.Point;

/**
 * TCP服务端，监听指定端口接收连接（TCPSlave）
 *
 * @author ZhangXi 2019年7月12日
 *
 */
public class TCPServer<M> extends Server<M> {

	/** MAX pending connections */
	private final static int BACKLOG = 512;

	private final SocketAddress local;
	private final AsynchronousServerSocketChannel server_socket_channel;

	public TCPServer(ChainHandler<M> handler, String host, int port) throws IOException {
		super(handler, Point.getPoint(host, port));

		server_socket_channel = AsynchronousServerSocketChannel.open(Executor.channelGroup());
		if (server_socket_channel.isOpen()) {
			// 禁用最大报文段生存时间，服务重启可立即绑定之前端口
			server_socket_channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
			// 禁用多个服务监听相同端口
			server_socket_channel.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.FALSE);
			if (port > 0) {
				// 指定端口
				if (host == null || host.length() == 0) {
					local = new InetSocketAddress(port);
				} else {
					local = new InetSocketAddress(host, port);
				}
				server_socket_channel.bind(new InetSocketAddress(port), BACKLOG);
			} else {
				// 随机端口
				server_socket_channel.bind(null, BACKLOG);
				local = server_socket_channel.getLocalAddress();
			}
		} else {
			throw new IOException("TCP服务端打开失败，" + key());
		}

		ChainGroup.add(this);
		accept();
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_SERVER;
	}

	@Override
	public boolean active() {
		return server_socket_channel != null && server_socket_channel.isOpen();
	}

	@Override
	public String getPoint() {
		return Point.getPoint(local);
	}

	@Override
	public SocketAddress getLocalAddress() {
		return local;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public void send(Object source) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void receive() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		if (server_socket_channel.isOpen()) {
			ChainGroup.remove(this);
			try {
				server_socket_channel.close();
				super.close();
			} catch (IOException e) {
				handler().error(this, e);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	private void accept() {
		if (Executor.channelGroup().isShutdown() || Executor.channelGroup().isTerminated()) {
			return;
		}
		if (server_socket_channel.isOpen()) {
			server_socket_channel.accept(this, ServerAcceptHandler.INSTANCE);
		}
	}

	@Override
	protected void accepted(AsynchronousSocketChannel socket_channel) {
		try {
			handler().connected(create(socket_channel));
		} catch (Exception e) {
			handler().error(this, e);
		} finally {
			accept();
		}
	}

	@Override
	protected void accepted(Throwable e) {
		if (e instanceof ClosedChannelException) {
			return;
		}
		handler().error(this, e);
		accept();
	}

	protected Slave<M> create(AsynchronousSocketChannel socket_channel) throws Exception {
		return new TCPSlave<>(this, socket_channel);
	}
}