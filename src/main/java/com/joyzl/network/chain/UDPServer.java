/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * UDP服务端
 *
 * @author ZhangXi 2019年7月9日
 *
 */
public class UDPServer extends Server {

	private final SocketAddress address;
	// UDP从连接与Server连接共用通道
	private final DatagramChannel datagram_channel;
	/** 从链路 */
	private final ConcurrentHashMap<SocketAddress, UDPSlave> slaves = new ConcurrentHashMap<>();

	public UDPServer(ChainHandler handler, String host, int port) throws IOException {
		super(handler, Point.getPoint(host, port));

		if (host == null || host.length() == 0) {
			address = new InetSocketAddress(port);
		} else {
			address = new InetSocketAddress(host, port);
		}

		datagram_channel = DatagramChannel.open();
		if (datagram_channel.isOpen()) {
			datagram_channel.configureBlocking(false);
			datagram_channel.bind(address);
		} else {
			throw new IOException("UDPServer:打开失败" + key());
		}
	}

	@Override
	public ChainType type() {
		return ChainType.UDP_SERVER;
	}

	@Override
	public boolean active() {
		return datagram_channel.isOpen();
	}

	@Override
	public String getPoint() {
		return Point.getPoint(address);
	}

	@Override
	public SocketAddress getLocalAddress() {
		return address;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////

	@Override
	public void receive() {
		try {
			UDPServerReceiver.register(this, datagram_channel);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	protected void received() {
		UDPSlave slave = null;
		final DataBuffer buffer = DataBuffer.instance();
		try {
			// receive 未提供读取数量返回，需要通过位置计算
			ByteBuffer bb = buffer.write();
			int size = bb.position();
			// 如果要支持接收最大可能的数据包，则需要额外的ByteBuffer用于接收，然后转写到DataBuffer
			// receive接收缓冲区之外的数据静默丢弃，再次执行receive也不会收到额外的数据
			final SocketAddress address = datagram_channel.receive(bb);
			if (address == null) {
				// read.release();
			} else {
				size = bb.position() - size;
				buffer.written(size);

				slave = slaves.get(address);
				if (slave == null) {
					slave = new UDPSlave(this, address);
					slaves.put(address, slave);
					handler().connected(slave);
				}

				final Object message = handler().decode(slave, buffer);
				if (message == null) {
					// 数据不足,无补救措施
					// UDP特性决定后续接收的数据只会是另外的数据帧
				} else {
					// 已解析消息对象
					if (buffer.readable() >= size) {
						throw new IllegalStateException("UDPServer:已解析消息但数据未减少");
					}
					handler().received(slave, message);
				}
			}
		} catch (Exception e) {
			if (slave != null) {
				handler().error(slave, e);
			} else {
				handler().error(this, e);
			}
		} finally {
			if (buffer != null) {
				buffer.release();
			}
		}
	}

	@Override
	public void send(Object message) {

	}

	protected void send(UDPSlave slave, Object message) {
		// 执行消息编码
		DataBuffer buffer = null;
		try {
			buffer = handler().encode(slave, message);
			if (buffer == null) {
				throw new IllegalStateException("UDPSlave:未编码数据 " + message);
			} else if (buffer.readable() <= 0) {
				throw new IllegalStateException("UDPSlave:编码零数据 " + message);
			} else {
				if (buffer.units() > 1) {
					int length = datagram_channel.send(buffer.read(), slave.getRemoteAddress());
					buffer.read(length);
				} else {
					int length = datagram_channel.send(buffer.read(), slave.getRemoteAddress());
					buffer.read(length);
				}
				handler().sent(slave, message);
			}
		} catch (Exception e) {
			if (buffer != null) {
				buffer.release();
			}
			handler().error(slave, e);
		}
	}

	protected void close(UDPSlave slave) {
		slaves.remove(slave.getRemoteAddress(), slave);
	}

	@Override
	public void close() {
		reset();
		try {
			datagram_channel.close();
		} catch (IOException e) {
			handler().error(this, e);
		} finally {
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
		UDPServerReceiver.unRegister(this, datagram_channel);
		slaves.clear();
	}
}