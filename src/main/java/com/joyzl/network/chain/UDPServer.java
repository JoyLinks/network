/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.joyzl.network.ChainSelector;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * UDP通道
 *
 * @author ZhangXi 2019年7月9日
 *
 */
public class UDPServer extends Server {

	private final SocketAddress address;
	// UDP从连接与Server连接共用通道
	private final DatagramChannel datagram_channel;

	public UDPServer(ChainHandler handler, String host, int port) throws IOException {
		super(handler, Point.getPoint(host, port));

		datagram_channel = DatagramChannel.open();
		if (datagram_channel.isOpen()) {
			datagram_channel.configureBlocking(false);
			if (host == null || host.length() == 0) {
				address = new InetSocketAddress(port);
			} else {
				address = new InetSocketAddress(host, port);
			}
			datagram_channel.bind(address);
		} else {
			throw new IOException("UDP服务端打开失败，" + key());
		}
		ChainSelector.register(this, datagram_channel, SelectionKey.OP_READ);
		ChainGroup.add(this);
	}

	@Override
	public ChainType type() {
		return ChainType.UDP_SERVER;
	}

	@Override
	public boolean active() {
		return datagram_channel != null && datagram_channel.isOpen();
	}

	@Override
	public String getPoint() {
		return Point.getPoint(address);
	}

	@Override
	public SocketAddress getLocalAddress() {
		if (active()) {
			try {
				return datagram_channel.getLocalAddress();
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

	////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void accepted(AsynchronousSocketChannel channel) {
	}

	@Override
	protected void accepted(Throwable e) {
	}

	@Override
	public void receive() {
		DataBuffer read = DataBuffer.instance();
		ByteBuffer buffer = read.write();
		Slave slave = null;
		try {
			// receive 未提供读取数量返回，需要通过位置计算
			int size = buffer.position();
			// 如果要支持接收最大可能的数据包，则需要额外的ByteBuffer用于接收，然后转写到DataBuffer
			// receive接收缓冲区之外的数据静默丢弃，再次执行receive也不会收到额外的数据
			final SocketAddress address = datagram_channel.receive(buffer);
			if (address == null) {
				// read.release();
			} else {
				size = buffer.position() - size;
				read.written(size);
				slave = getSlave(Point.getPoint(address));
				if (slave == null) {
					addSlave(slave = new UDPSlave(this, address));
					handler().connected(slave);
				}
				final Object message = handler().decode(this, read);
				if (message == null) {
					// 数据不足,无补救措施
					// UDP特性决定后续接收的数据只会是另外的数据帧
				} else {
					handler().received(slave, message);
				}
			}
		} catch (Exception e) {
			handler().error(slave == null ? this : slave, e);
		} finally {
			if (read != null) {
				read.release();
			}
		}
	}

	@Override
	public void send(Object source) {
		// 必须通过UDPSlave发送
		// UDPServer无法明确目标地址
		throw new UnsupportedOperationException();
	}

	protected void send(UDPSlave slave, Object message) throws IOException {
		// 执行消息编码
		DataBuffer buffer = null;
		try {
			buffer = handler().encode(this, message);
			if (buffer == null) {
				throw new IllegalStateException("消息未编码数据 " + message);
			} else if (buffer.readable() <= 0) {
				throw new IllegalStateException("消息编码零数据 " + message);
			} else {
				if (buffer.units() > 1) {
					int length = datagram_channel.send(buffer.read(), slave.getRemoteAddress());
					buffer.read(length);
				} else {
					int length = datagram_channel.send(buffer.read(), slave.getRemoteAddress());
					buffer.read(length);
				}
				handler().sent(this, message);
			}
		} catch (Exception e) {
			if (buffer != null) {
				buffer.release();
			}
			slave.sent(e);
		}
	}

	@Override
	public void close() {
		try {
			ChainSelector.unRegister(this, datagram_channel);
			datagram_channel.close();
		} catch (IOException e) {
			handler().error(this, e);
		} finally {
			ChainGroup.remove(this);
			try {
				handler().disconnected(this);
			} catch (Exception e) {
				handler().error(this, e);
			}
		}
		try {
			clearContext();
		} catch (IOException e) {
			handler().error(this, e);
		}
	}

	@Override
	public void reset() {
		close();
	}
}