/*
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
import java.nio.channels.CompletionHandler;

import com.joyzl.network.Executor;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * TCP服务端，监听指定端口接收连接（TCPSlave）
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月12日
 *
 */
public class TCPServer<M> extends Server<M> {

	private final static int BACKLOG = 1024;
	private final SocketAddress address;
	private final AsynchronousServerSocketChannel server_socket_channel;

	public TCPServer(ChainHandler<M> handler, String host, int port) throws IOException {
		super(handler, Point.getPoint(host, port));

		server_socket_channel = AsynchronousServerSocketChannel.open(Executor.channelGroup());
		if (server_socket_channel.isOpen()) {
			server_socket_channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
			// server_socket_channel.setOption(StandardSocketOptions.SO_REUSEPORT, Boolean.FALSE);
			if (port > 0) {
				if (host == null || host.length() == 0) {
					address = new InetSocketAddress(port);
				} else {
					address = new InetSocketAddress(host, port);
				}
				server_socket_channel.bind(new InetSocketAddress(port), BACKLOG);
			} else {
				server_socket_channel.bind(null, BACKLOG);
				address = server_socket_channel.getLocalAddress();
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
	public void close() {
		ChainGroup.off(this);

		if (server_socket_channel.isOpen()) {
			try {
				server_socket_channel.close();
				super.close();
			} catch (IOException e) {
				handler().error(TCPServer.this, e);
			}
		}
	}

	@Override
	public String getPoint() {
		return Point.getPoint(address);
	}

	@Override
	public SocketAddress getLocalAddress() {
		if (active()) {
			try {
				return server_socket_channel.getLocalAddress();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
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
	protected void write(Object writer) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void receive() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void read(DataBuffer reader) {
		throw new UnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////////

	private void accept() {
		if (Executor.channelGroup().isShutdown()) {
			// 线程组已关闭
			return;
		}
		if (server_socket_channel.isOpen()) {
			server_socket_channel.accept(this, ACCEPT_HANDLER);
		}
	}

	protected Slave<M> accepted(AsynchronousSocketChannel socket_channel) throws IOException {
		// 继承者可重载此方法实现自己的连接创建
		return new TCPSlave<>(this, socket_channel);
	}

	protected final CompletionHandler<AsynchronousSocketChannel, TCPServer<M>> ACCEPT_HANDLER = new CompletionHandler<AsynchronousSocketChannel, TCPServer<M>>() {

		@Override
		public void completed(AsynchronousSocketChannel socket_channel, TCPServer<M> server) {
			try {
				final Slave<M> slave = server.accepted(socket_channel);
				server.handler().connected(slave);
				addSlave(slave);
			} catch (Exception e) {
				server.handler().error(server, e);
			}
			server.accept();
		}

		@Override
		public void failed(Throwable e, TCPServer<M> server) {
			server.handler().error(server, e);
			server.accept();
		}
	};
}