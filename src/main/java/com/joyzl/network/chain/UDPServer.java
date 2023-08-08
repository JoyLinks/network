/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.joyzl.network.ChainSelector;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * UDP通道
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月9日
 *
 */
public class UDPServer<M> extends Server<M> {

	private final SocketAddress address;
	// UDP从连接与Server连接共用通道
	private final DatagramChannel datagram_channel;

	public UDPServer(ChainHandler<M> handler, String host, int port) throws IOException {
		super(handler, Point.getPoint(host, port));

		datagram_channel = DatagramChannel.open();
		if (datagram_channel.isOpen()) {
			datagram_channel.configureBlocking(false);
			datagram_channel.register(ChainSelector.reads(), SelectionKey.OP_READ, this);
			if (host == null || host.length() == 0) {
				address = new InetSocketAddress(port);
			} else {
				address = new InetSocketAddress(host, port);
			}
			datagram_channel.bind(address);

			ChainGroup.add(this);
			ChainSelector.reads().wakeup();
		} else {
			throw new IOException("UDP服务端打开失败，" + key());
		}
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

	@Override
	public void receive() {
		read(DataBuffer.getB2048());
	}

	@Override
	protected void read(DataBuffer reader) {
		ByteBuffer buffer = reader.getWriteableBuffer();
		int position = buffer.position();
		Slave<M> slave = null;
		try {
			// receive 未提供读取数量返回，需要通过位置计算
			final SocketAddress address = datagram_channel.receive(buffer);
			if (address == null) {
			} else {
				reader.writtenBuffers(buffer.position() - position);
				slave = getSlave(Point.getPoint(address));
				if (slave == null) {
					slave = new UDPSlave<>(this, address);
					addSlave(slave);
					handler().connected(slave);
				}
				final M message = handler().decode(this, reader);
				if (message == null) {
					// 数据不足,无补救措施,UDP特性决定后续接收的数据只会是另外的数据帧
				} else {
					handler().received(slave, message);
				}
			}
		} catch (Exception e) {
			handler().error(slave == null ? this : slave, e);
		} finally {
			if (reader != null) {
				reader.release();
			}
		}
	}

	@Override
	public void send(Object source) {
		// 必须通过UDPSlave发送
		// UDPServer无法明确目标地址
		throw new UnsupportedOperationException();
	}

	@Override
	protected void write(Object writer) {
		// 必须通过UDPSlave发送
		// UDPServer无法明确目标地址
		throw new UnsupportedOperationException();
	}

	protected void write(DataBuffer buffer, UDPSlave<M> slave) throws IOException {
		// TODO :UDP发送方式需要调整为一次性将所有ByteBuffer发送
		int length;
		while (buffer.hasReadableBuffer()) {
			length = datagram_channel.send(buffer.getReadableBuffer(), slave.getRemoteAddress());
			if (length > 0) {
				buffer.readBuffers(length);
			} else if (length == 0) {
				continue;
			} else {
				break;
			}
		}
		buffer.release();
	}

	@Override
	public void close() {
		try {
			datagram_channel.close();
		} catch (IOException e) {
			handler().error(this, e);
		} finally {
			ChainGroup.off(this);
			try {
				handler().disconnected(this);
			} catch (Exception e) {
				handler().error(this, e);
			}
		}
	}
}