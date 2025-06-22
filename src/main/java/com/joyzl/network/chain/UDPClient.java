/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.joyzl.network.ChainSelector;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * UDP通道，无连接协议因此无心跳无重连机制
 * <p>
 * 工作机制：
 * <ol>
 * <li>connect()请求连接;</li>
 * <li>connected()连接返回（成功/失败）;</li>
 * <li>receive()接收数据（选择器调用）；</li>
 * <li>received()接收数据返回（成功/失败）；</li>
 * <li>send()请求发送数据；</li>
 * <li>sent()发送数据返回（成功/失败）；</li>
 * <li>close()关闭链路；</li>
 * </ol>
 * 链路关闭后可再次请求连接。对象是多线程安全的。
 * <p>
 *
 * @author simon(ZhangXi TEL : 13883833982) 2019年7月9日
 */
public class UDPClient extends Client {

	private final SocketAddress remote;
	private final DatagramChannel datagram_channel;

	public UDPClient(ChainHandler handler, String host, int port) throws IOException {
		super(handler);

		remote = new InetSocketAddress(host, port);
		datagram_channel = DatagramChannel.open();
		if (datagram_channel.isOpen()) {
			datagram_channel.configureBlocking(false);
		} else {
			throw new IOException("UDP连接打开失败，" + key());
		}
		// 注册NIO.1选择器,当可读时触发receive()方法
		ChainSelector.register(this, datagram_channel, SelectionKey.OP_READ);
		ChainGroup.add(this);

		// datagram_channel.register(ChainSelector.writes(),SelectionKey.OP_WRITE,this);
		// ChainSelector.writes().wakeup();
	}

	@Override
	public ChainType type() {
		return ChainType.UDP_CLIENT;
	}

	@Override
	public boolean active() {
		return datagram_channel.isOpen() && datagram_channel.isConnected();
	}

	@Override
	public String getPoint() {
		return Point.getPoint(remote);
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return remote;
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

	////////////////////////////////////////////////////////////////////////////////

	public void connect() {
		if (datagram_channel.isOpen()) {
			try {
				if (datagram_channel.isConnected()) {
					datagram_channel.disconnect();
				}
				datagram_channel.connect(remote);
				// ChainSelector.reads().wakeup();
				connected();
			} catch (Exception e) {
				connected(e);
			}
		}
	}

	@Override
	protected void connected() {
		try {
			handler().connected(this);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	protected void connected(Throwable e) {
		handler().error(this, e);
	}

	@Override
	public void receive() {
		// UDP数据报特性:如果ByteBuffer不足够接收所有的数据，剩余的被静默抛弃，不会抛出任何异常
		// DataBuffer不能接续，必须清空并确保可以接受最长数据
		final DataBuffer buffer = DataBuffer.instance();
		try {
			int size = datagram_channel.read(buffer.write());
			if (size > 0) {
				// 确认接收到的数据量
				buffer.written(size);
				// 解包
				final Object message = handler().decode(this, buffer);
				if (message == null) {
					// 数据不足，无补救措施
					// UDP特性决定后续接收的数据只会是另外的数据帧
					buffer.clear();
				} else {
					handler().received(this, message);
				}
			} else if (size == 0) {
				// DataBuffer提供的ByteBuffer已满，无补救措施
			} else {
				// 未知情况
			}
		} catch (Exception e) {
			received(e);
		} finally {
			buffer.release();
		}
	}

	@Override
	protected void received(int size) {
	}

	@Override
	protected void received(Throwable e) {
		handler().error(this, e);
	}

	@Override
	public void send(Object message) {
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
					long length = datagram_channel.write(buffer.reads());
					buffer.read(length);
				} else {
					int length = datagram_channel.write(buffer.read());
					buffer.read(length);
				}
				if (buffer.readable() > 0) {
					// 如果超出UDP包最大长度可能出现
					// 经Windows11测试65536数据时发送数量为16384(16Kb)
					throw new IllegalStateException("数据未能全部送出 " + message);
				} else {
					handler().sent(this, message);
				}
			}
		} catch (Exception e) {
			if (buffer != null) {
				buffer.release();
			}
			sent(e);
		}
	}

	@Override
	protected void sent(int size) {
		// 此方法未使用
	}

	@Override
	protected void sent(Throwable e) {
		handler().error(this, e);
	}

	@Override
	public void close() {
		if (datagram_channel.isOpen()) {
			ChainGroup.remove(this);
			if (datagram_channel.isConnected()) {
				try {
					datagram_channel.disconnect();
				} catch (Exception e) {
					handler().error(this, e);
				} finally {
					try {
						handler().disconnected(this);
					} catch (Exception e) {
						handler().error(this, e);
					}
				}
			}
			try {
				ChainSelector.unRegister(this, datagram_channel);
				datagram_channel.close();
			} catch (Exception e) {
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
	public void reset() {
		close();
	}
}
