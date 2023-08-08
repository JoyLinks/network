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
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.Executor;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * 基于TCP连接的客户端，提供心跳，中断重连等机制
 * <p>
 * 调用connect(true)连接，连接成功后可调用receive()并持续接收
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月12日
 *
 */
public class TCPClient<M> extends Client<M> {

	private final SocketAddress address;
	private final ReconnectTask reconnect_task;
	private final HeartbeatTask heartbeat_task;

	private AsynchronousSocketChannel socket_channel;

	/** 重新连接间隔时间(秒) */
	private int reconnect_interval = 6;
	/** 连接空闲心跳间隔时间(秒) */
	private int heartbeat_interval = 60 * 3;
	/** 连接状态 */
	private volatile boolean connected;
	/** 关闭状态 */
	private volatile boolean closed;

	/**
	 * 创建TCPClient由接点标识指定连接信息
	 * <p>
	 * 自动生成接点标识 "192.168.0.1:1030"
	 *
	 * @param handler {@link ChainHandler}
	 * @param host 主机
	 * @param port 端口
	 */
	public TCPClient(ChainHandler<M> handler, String host, int port) {
		super(handler);

		address = new InetSocketAddress(host, port);
		reconnect_task = new ReconnectTask();
		heartbeat_task = new HeartbeatTask();

		closed = false;
		connected = false;
		ChainGroup.add(this);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_CLIENT;
	}

	@Override
	public boolean active() {
		return !closed && connected && socket_channel != null && socket_channel.isOpen();
	}

	@Override
	public String getPoint() {
		return Point.getPoint(address);
	}

	@Override
	public SocketAddress getLocalAddress() {
		if (active()) {
			try {
				return socket_channel.getLocalAddress();
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
				return socket_channel.getRemoteAddress();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	public void connect() {
		if (closed) {
			return;
		}
		try {
			socket_channel = AsynchronousSocketChannel.open(Executor.channelGroup());
			if (socket_channel.isOpen()) {
				socket_channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
				socket_channel.setOption(StandardSocketOptions.TCP_NODELAY, true);

				socket_channel.connect(address, this, TCP_CONNECT_HANDLER);
			} else {
				throw new IOException("Socket open failed");
			}
		} catch (Exception e) {
			handler().error(this, e);
			// 连接异常,延迟后重试
			reset();
		}
	}

	private void reset() {
		// 重置链路,延迟后重连
		if (closed) {
			shutdown();
		} else {
			shutdown();

			heartbeat_task.stop();
			// 延迟一定时间后尝试重新连接
			reconnect_task.start();
		}
	}

	private void shutdown() {
		// 首先关闭输入输出
		// 其次关闭链路
		if (socket_channel != null) {
			try {
				if (connected) {
					connected = false;
					if (socket_channel.isOpen()) {
						socket_channel.shutdownInput();
						socket_channel.shutdownOutput();
						handler().disconnected(this);
					} else {
						socket_channel.close();
						socket_channel = null;
					}
				} else {
					socket_channel.close();
					socket_channel = null;
				}
			} catch (Exception e) {
				handler().error(this, e);
			}
		} else {
			connected = false;
		}
	}

	private class ReconnectTask implements Runnable {

		private ScheduledFuture<?> future;

		public void start() {
			if (Executor.isActive()) {
				if (getReconnectInterval() > 0) {
					future = Executor.schedule(this, getReconnectInterval(), TimeUnit.SECONDS);
				}
			}
		}

		public void stop() {
			if (future != null) {
				future.cancel(true);
				future = null;
			}
		}

		@Override
		public void run() {
			connect();

			if (future != null) {
				future = null;
			}
		}
	}

	private class HeartbeatTask implements Runnable {
		/**
		 * 最后一次成功读/写的时间戳
		 */
		private ScheduledFuture<?> future;

		public void start() {
			if (Executor.isActive()) {
				if (getHeartbeatInterval() > 0) {
					future = Executor.schedule(this, getHeartbeatInterval(), TimeUnit.SECONDS);
				}
			}
		}

		public void stop() {
			if (future != null) {
				future.cancel(true);
				future = null;
			}
		}

		@Override
		public void run() {
			long delay = getLastRead() > getLastWrite() ? getLastRead() : getLastWrite();
			delay = getHeartbeatInterval() - (System.currentTimeMillis() - delay) / 1000;
			if (delay <= 0) {
				try {
					handler().beat(TCPClient.this);
				} catch (Exception e) {
					handler().error(TCPClient.this, e);
				}
				future = Executor.schedule(this, getHeartbeatInterval(), TimeUnit.SECONDS);
			} else {
				future = Executor.schedule(this, delay, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public void close() {
		if (isClosed()) {
		} else {
			closed = true;
			shutdown();

			ChainGroup.off(this);
			reconnect_task.stop();
			heartbeat_task.stop();
		}
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	 * 获取自动重新连接间隔时间
	 *
	 * @return 秒
	 */
	public int getReconnectInterval() {
		return reconnect_interval;
	}

	/**
	 * 设置自动重新连接间隔时间
	 *
	 * @param interval 10~n秒
	 */
	public void setReconnectInterval(int interval) {
		if (interval < 10) {
			interval = 10;
		}
		reconnect_interval = interval;
	}

	/**
	 * 获取连接空闲心跳间隔时间
	 *
	 * @return 秒
	 */
	public int getHeartbeatInterval() {
		return heartbeat_interval;
	}

	/**
	 * 设置连接空闲心跳间隔时间
	 *
	 * @param time 60~n秒
	 */
	public void setHeartbeatInterval(int time) {
		if (time < 60) {
			heartbeat_interval = 60;
		} else {
			heartbeat_interval = time;
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	protected final CompletionHandler<Void, TCPClient<M>> TCP_CONNECT_HANDLER = new CompletionHandler<Void, TCPClient<M>>() {

		@Override
		public void completed(Void result, TCPClient<M> chain) {
			chain.connected = true;
			// Simon(ZhangXi) 2020-05-25
			// 在执行handler().connected()之前必须设置连接状态connected=true，否则handler().connected()事件中将无法发送数据
			// 因为Chain发送时会检查连接状态Chain.Active()，如果connected不为true，数据将不会发送
			try {
				chain.handler().connected(chain);
			} catch (Exception e) {
				chain.handler().error(chain, e);
				chain.reset();
			}
			chain.heartbeat_task.start();
			// 启动接收数据
			// chain.receive();
		}

		@Override
		public void failed(Throwable e, TCPClient<M> chain) {
			chain.connected = false;
			// completed()方法抛出的异常不会到达此方法
			chain.reset();
			if (e instanceof AsynchronousCloseException) {
				// 正在执行通道关闭
				return;
			}
			chain.handler().error(chain, e);
		}
	};

	// 表示是否正在接收数据中
	private final AtomicBoolean reading = new AtomicBoolean(false);

	@Override
	public void receive() {
		if (reading.compareAndSet(false, true)) {
			read(DataBuffer.getB2048());
		}
	}

	@Override
	protected void read(DataBuffer buffer) {
		// SocketChannel不能投递多个接收操作，否则会收到WritePendingException异常
		socket_channel.read(buffer.getWriteableBuffer(), getHeartbeatInterval() * 2, TimeUnit.SECONDS, buffer, TCP_READ_HANDLER);
	}

	protected final CompletionHandler<Integer, DataBuffer> TCP_READ_HANDLER = new CompletionHandler<>() {

		@Override
		public void completed(Integer result, DataBuffer buffer) {
			if (result > 0) {
				refreshLastRead();
				buffer.writtenBuffers(result.intValue());

				M source;
				try {
					while (true) {
						// 继续请求解包直到没有对象返回
						// 在数据包粘连的情况下，可能会接收到两个数据包的数据
						source = handler().decode(TCPClient.this, buffer);
						if (source == null) {
							// 未能解析消息对象,继续接收数据
							read(buffer);
							break;
						} else {
							// 判断数据缓存对象是否被其它对象持有
							if (buffer.reference()) {
								if (buffer.hasResidue()) {
									// 获取新的数据缓存对象
									final DataBuffer buffer1 = DataBuffer.getB2048();
									// 将剩余的数据转移到新的缓存对象
									buffer.residue(buffer1);
									// 释放被持有的对象(此时并不会真正释放，而是将持有状态恢复为无多重引用)
									buffer.release();
									buffer = buffer1;

									handler().received(TCPClient.this, source);
									// 有剩余数据,继续尝试解包,继续接收数据
									continue;
								} else {
									// 释放被持有的对象(此时并不会真正释放，而是将持有状态恢复为无多重引用)
									buffer.release();
									buffer = null;
									reading.set(false);

									handler().received(TCPClient.this, source);
									// 没有剩余数据,停止尝试解包,停止接收数据
									break;
								}
							} else {
								if (buffer.hasResidue() || buffer.readable() > 0) {
									// 解包成功但是还有剩余数据
									handler().received(TCPClient.this, source);
									// 有剩余数据,继续尝试解包,继续接收数据
									continue;
								} else {
									buffer.release();
									buffer = null;
									reading.set(false);

									handler().received(TCPClient.this, source);
									// 没有剩余数据,停止尝试解包,停止接收数据
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					failed(e, buffer);
				}
			} else if (result == 0) {
				// 没有数据并且没有达到流的末端时返回0
				// 如果用于接收的ByteBuffer缓存满则会出现读零情况
				// DataBuffer代码存在问题才会导致提供了一个已满的ByteBuffer
				try {
					if (buffer.writeable() > 0) {
						read(buffer);
					} else {
						throw new IllegalStateException("当前缓存单元已满");
					}
				} catch (Exception e) {
					failed(e, buffer);
				}
			} else {
				// 缓存对象未投递给处理对象，须释放
				buffer.release();
				buffer = null;
				reading.set(false);
				// 尝试重连接
				reset();
			}
		}

		@Override
		public void failed(Throwable e, DataBuffer buffer) {
			// IO操作失败进入此方法
			// completed()方法抛出的异常不会到达此方法

			// IO失败DataBuffer不会进入处理方法，须释放
			if (buffer != null) {
				reading.set(false);
				buffer.release();
			}
			reset();
			if (e instanceof AsynchronousCloseException) {
				// 正在执行通道关闭
				return;
			}
			if (e instanceof InterruptedByTimeoutException) {
				// 接收数据超时
				try {
					handler().received(TCPClient.this, null);
				} catch (Exception e1) {
					handler().error(TCPClient.this, e1);
				}
			}
			handler().error(TCPClient.this, e);
		}
	};

	// 用于发送数据的锁
	final ReentrantLock lock = new ReentrantLock();

	@SuppressWarnings("unchecked")
	@Override
	public void send(Object message) {
		lock.lock();
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
			lock.unlock();
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
		// 此方法用于将消息对象编码为字节串
		// 通道数据发送完成后会再次调用此方法请求继续下一个消息编码
		if (message == null) {
			lock.lock();
			try {
				message = messages().pollFirst();
				message = messages().peekFirst();
			} finally {
				lock.unlock();
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

	private void write(DataBuffer buffer) {
		// 此方法用于将编码后数据投递给通道
		// CompletionHandler如果一次发送未完成会再次调用此方法发送剩余数据
		if (buffer == null) {
		} else if (buffer.hasReadableBuffer()) {
			socket_channel.write(buffer.getReadableBuffer(), getHeartbeatInterval() * 2, TimeUnit.SECONDS, buffer, TCP_WRITE_HANDLER);
		} else {
			buffer.release();
			// 数据发送完成，继续消息编码
			write((Object) null);
		}
	}

	private final CompletionHandler<Integer, DataBuffer> TCP_WRITE_HANDLER = new CompletionHandler<>() {

		@Override
		public void completed(Integer result, DataBuffer buffer) {
			if (result > 0) {
				refreshLastWrite();
				buffer.readBuffers(result.intValue());
				write(buffer);
			} else if (result == 0) {
				// 客户端缓存满，继续尝试发送
				// 恶意程序，可能会导致无限尝试
				write(buffer);
			} else {
				buffer.release();
				// 连接被客户端断开,尝试重连接
				reset();
			}
		}

		@Override
		public void failed(Throwable e, DataBuffer buffer) {
			// 发送失败,释放数据和对象,尝试重连接
			buffer.release();
			reset();
			if (e instanceof AsynchronousCloseException) {
				// 正在执行通道关闭
				return;
			}
			if (e instanceof InterruptedByTimeoutException) {
				// 发送数据超时
				try {
					handler().sent(TCPClient.this, null);
				} catch (Exception e1) {
					handler().error(TCPClient.this, e1);
				}
			}
			handler().error(TCPClient.this, e);
		}
	};
}