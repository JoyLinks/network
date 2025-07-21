/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.Executor;
import com.joyzl.network.Point;

/**
 * TCP服务端，监听指定端口接收连接（TCPSlave）
 *
 * @author ZhangXi 2019年7月12日
 *
 */
public class TCPServer extends Server {

	/** DEFAULT MAX pending connections */
	private final static int BACKLOG = 512;

	private final SocketAddress local;
	private final AsynchronousServerSocketChannel server_socket_channel;
	/** 从链路 */
	private final ConcurrentHashMap<SocketAddress, Slave> slaves = new ConcurrentHashMap<>();

	public TCPServer(ChainHandler handler, String host, int port) throws IOException {
		this(handler, host, port, BACKLOG);
	}

	public TCPServer(ChainHandler handler, String host, int port, int backlog) throws IOException {
		super(handler);

		server_socket_channel = AsynchronousServerSocketChannel.open(Executor.channelGroup());
		if (server_socket_channel.isOpen()) {
			// 禁用最大报文段生存时间，服务重启可立即绑定之前端口
			server_socket_channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
			// 禁用多个服务监听相同端口
			// server_socket_channel.setOption(StandardSocketOptions.SO_REUSEPORT,Boolean.FALSE);
			if (port > 0) {
				// 指定端口
				if (host == null || host.length() == 0) {
					local = new InetSocketAddress(port);
				} else {
					local = new InetSocketAddress(host, port);
				}
				server_socket_channel.bind(new InetSocketAddress(port), backlog);
			} else {
				// 随机端口
				server_socket_channel.bind(null, backlog);
				local = server_socket_channel.getLocalAddress();
			}
		} else {
			throw new IOException("TCPServer:打开失败 " + Point.getPoint(host, port));
		}
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
	public String point() {
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
	public void receive() {
		accept();
	}

	@Override
	public void reset() {
		// 关闭并移除所有从链路
		final Iterator<Entry<SocketAddress, Slave>> iterator = slaves.entrySet().iterator();
		while (iterator.hasNext()) {
			iterator.next().getValue().close();
			iterator.remove();
		}
	}

	@Override
	public void close() {
		if (server_socket_channel.isOpen()) {
			try {
				server_socket_channel.close();
			} catch (IOException e) {
				handler().error(this, e);
			}
			try {
				clearContext();
			} catch (IOException e) {
				handler().error(this, e);
			}
		}
	}

	@Override
	public Collection<Slave> slaves() {
		return Collections.unmodifiableCollection(slaves.values());
	}

	////////////////////////////////////////////////////////////////////////////////

	protected void accept() {
		if (Executor.channelGroup().isShutdown() || Executor.channelGroup().isTerminated()) {
			return;
		}
		if (server_socket_channel.isOpen()) {
			server_socket_channel.accept(this, TCPServerAccepter.INSTANCE);
		}
	}

	protected void accepted(AsynchronousSocketChannel socket_channel) {
		try {
			Slave slave = create(socket_channel);
			Slave sprev = slaves.put(slave.getRemoteAddress(), slave);
			handler().connected(slave);
			if (sprev != null) {
				sprev.close();
			}
		} catch (Exception e) {
			handler().error(this, e);
		} finally {
			accept();
		}
	}

	protected void accepted(Throwable e) {
		if (e instanceof ClosedChannelException) {
			return;
		}
		handler().error(this, e);
		accept();
	}

	protected Slave create(AsynchronousSocketChannel socket_channel) throws Exception {
		return new TCPSlave(this, socket_channel);
	}

	protected void close(TCPSlave slave) {
		slaves.remove(slave.getRemoteAddress(), slave);
	}

}