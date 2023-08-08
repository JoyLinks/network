/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.ChainSelector;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * UDP通道
 * <p>
 * 有数据时自动调用receive()接收数据
 *
 * @author simon(ZhangXi TEL : 13883833982) 2019年7月9日
 */
public class UDPClient<M> extends Client<M> {

	private final SocketAddress address;
	private final DatagramChannel datagram_channel;

	public UDPClient(ChainHandler<M> handler, String host, int port) throws IOException {
		super(handler);

		address = new InetSocketAddress(host, port);
		datagram_channel = DatagramChannel.open();
		if (datagram_channel.isOpen()) {
			datagram_channel.configureBlocking(false);
			// 注册NIO.1选择器,当可读时触发receive()方法
			datagram_channel.register(ChainSelector.reads(), SelectionKey.OP_READ, this);
			// datagram_channel.register(ChainSelector.writes(),SelectionKey.OP_WRITE,this);
		} else {
			throw new IOException("UDP连接打开失败，" + key());
		}

		ChainGroup.add(this);
		ChainSelector.reads().wakeup();
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
		if (active()) {
			try {
				return datagram_channel.getRemoteAddress();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	public void connect() {
		try {
			datagram_channel.connect(address);
			handler().connected(this);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	public void receive() {
		read(DataBuffer.getB2048());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void read(DataBuffer reader) {
		try {
			final int length = datagram_channel.read(reader.getWriteableBuffer());
			// UDP数据报特性:如果ByteBuffer不足够接收所有的数据,剩余的被静默抛弃
			// DataBuffer不能接续,必须清空并确保可以接受最长数据

			if (length > 0) {
				// 确认接收到的数据量
				reader.writtenBuffers(length);
				// 解包
				final Object source = handler().decode(this, reader);
				if (source == null) {
					// 数据不足,无补救措施
					// UDP特性决定后续接收的数据只会是另外的数据帧
					reader.release();
				} else {
					reader.release();
					// 防止handler().received()异常时重复释放缓存对象
					reader = null;

					handler().received(this, (M) source);
				}
			} else if (length == 0) {
				// 1.接收完成
				// 2.DataBuffer提供的ByteBuffer已满
				// 好像无补救措施
				reader.release();
			} else {
				reader.release();
			}
		} catch (Exception e) {
			if (reader != null) {
				// handler().decode()异常时由此释放缓存对象
				reader.release();
			}
			handler().error(this, e);
		}
	}

	// 用于发送数据的锁
	final ReentrantLock writing = new ReentrantLock();

	@SuppressWarnings("unchecked")
	@Override
	public void send(Object message) {
		writing.lock();
		try {
			if (messages().isEmpty()) {
				if (messages().offerLast((M) message)) {
					// 通过message==null表示当前消息是否应立即发送
				} else {
					message = null;
				}
			} else {
				if (messages().offerLast((M) message)) {
					message = null;
				} else {
					throw new IllegalStateException("消息发送队列满");
				}
			}
		} finally {
			writing.unlock();
		}
		if (message == null) {
			return;
		} else {
			write(message);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void write(Object message) {
		if (message == null) {
			writing.lock();
			try {
				message = messages().pollFirst();
				message = messages().peekFirst();
			} finally {
				writing.unlock();
			}
			if (message == null) {
				return;
			}
		}
		// 执行消息编码
		DataBuffer buffer = null;
		try {
			buffer = handler().encode(this, (M) message);
			if (buffer == null) {
				throw new IllegalStateException("消息未编码数据 " + message);
			} else if (buffer.readable() <= 0) {
				throw new IllegalStateException("消息编码0数据 " + message);
			} else {
				write(buffer);
				handler().sent(this, (M) message);
			}
		} catch (Exception e) {
			if (buffer != null) {
				buffer.release();
			}
			handler().error(this, e);
		}
	}

	private void write(DataBuffer buffer) throws IOException {
		// TODO :UDP发送方式需要调整为一次性将所有ByteBuffer发送
		int length;
		while (buffer.hasReadableBuffer()) {
			length = datagram_channel.write(buffer.getReadableBuffer());
			if (length > 0) {
				buffer.readBuffers(length);
			} else if (length == 0) {
				continue;
			} else {
				// 好像没有补救措施
				break;
			}
		}
		buffer.release();
		// 数据发送完成，请求继续编码
		write((Object) null);
	}

	@Override
	public void close() {
		if (datagram_channel != null && datagram_channel.isConnected()) {
			ChainGroup.off(this);
			try {
				datagram_channel.disconnect();
				datagram_channel.close();
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
	}
}
