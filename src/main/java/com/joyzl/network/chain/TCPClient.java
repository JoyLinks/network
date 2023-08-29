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
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.joyzl.network.Executor;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * 基于TCP连接的客户端，提供心跳，中断重连等机制
 * <p>
 * 工作机制：
 * <ol>
 * <li>connect()请求连接（异步）;</li>
 * <li>connected()连接返回（成功/失败）;</li>
 * <li>receive()请求接收数据（异步）；</li>
 * <li>received()接收数据返回（成功/失败）；</li>
 * <li>send()请求发送数据（异步）；</li>
 * <li>sent()发送数据返回（成功/失败）；</li>
 * <li>close()关闭链路；</li>
 * </ol>
 * 使用者根据通信协议确定发送和接收数据的时机和机制；
 * 发送和接收数据可同时进行，但不能同时请求多个发送或多个接收，应等待上一次发送或接收返回后才能再次请求发送或接收数据。
 * 链路关闭后可再次请求连接。对象不是多线程安全的，在多线程收发情形下使用者应实现消息排队。
 * <p>
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

	public boolean isClosed() {
		return closed;
	}

	////////////////////////////////////////////////////////////////////////////////

	public void connect() {
		if (closed) {
			// 用户关闭
			return;
		}
		if (socket_channel == null) {
			try {
				socket_channel = AsynchronousSocketChannel.open(Executor.channelGroup());
				if (socket_channel.isOpen()) {
					socket_channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
					socket_channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
					socket_channel.connect(address, this, ClientConnectHandler.INSTANCE);
				} else {
					socket_channel = null;
				}
			} catch (Exception e) {
				// 连接异常,延迟后重试
				handler().error(this, e);
				reset();
			}
		}
	}

	@Override
	protected void connected() {
		// 连接成功
		// ZhangXi 2020-05-25
		// 在执行handler().connected()之前必须设置连接状态connected=true，否则handler().connected()事件中将无法发送数据
		// 因为Chain发送时会检查连接状态Chain.Active()，如果connected不为true，数据将不会发送
		connected = true;
		try {
			handler().connected(this);
		} catch (Exception e) {
			handler().error(this, e);
			reset();
		}
		heartbeat_task.start();
	}

	@Override
	protected void connected(Throwable e) {
		// 连接异常
		connected = false;
		if (e instanceof AsynchronousCloseException) {
			// 正在执行通道关闭
			return;
		}
		handler().error(this, e);
		reset();
	}

	private M receive_message;
	private DataBuffer read;

	/**
	 * 从网络接收数据
	 * <p>
	 * 此方法不是多线程安全的，调用者应确保上一次数据接收返回之后才能再次接收数据
	 * </p>
	 */
	@Override
	public void receive() {
		// 读取
		if (receive_message == null) {
			if (read == null) {
				read = DataBuffer.instance();
			}
			// SocketChannel不能投递多个接收操作，否则会收到ReadPendingException异常
			socket_channel.read(//
				read.write(), // ByteBuffer
				handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
				this, ClientReceiveHandler.INSTANCE // Handler
			);
		}
	}

	@Override
	protected void received(int size) {
		// 读取返回
		if (size > 0) {
			read.written(size);
			try {
				// 多次请求解包直到没有对象返回
				while (true) {
					// 在数据包粘连的情况下，可能会接收到两个数据包的数据
					receive_message = handler().decode(this, read);
					if (receive_message == null) {
						// 未能解析消息对象,继续接收数据
						socket_channel.read(//
							read.write(), // ByteBuffer
							handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
							this, ClientReceiveHandler.INSTANCE // Handler
						);
						break;
					} else {
						// 已解析消息对象
						if (read.discard() > 0) {
							// 解析数据不应出现残留
							throw new IllegalStateException("设置了读取范围但数据有残留");
						}
						if (read.readable() > 0) {
							// 注意:以下方法中可能会调用receive()
							handler().received(this, receive_message);
							receive_message = null;
							// 有剩余数据,继续尝试解包
							continue;
						} else {
							final M message = receive_message;
							receive_message = null;
							handler().received(this, message);
							// 没有剩余数据,停止尝试解包
							break;
						}
					}
				}
			} catch (Exception e) {
				receive_message = null;
				read.release();
				read = null;
				handler().error(this, e);
			}
		} else if (size == 0) {
			// 没有数据并且没有达到流的末端时返回0
			// 如果用于接收的ByteBuffer缓存满则会出现读零情况
			// DataBuffer代码存在问题才会导致提供了一个已满的ByteBuffer
			read.release();
			read = null;
			handler().error(this, new IllegalStateException("零读:当前缓存单元已满"));
		} else {
			// 链路被关闭
			// 缓存对象未投递给处理对象，须释放
			read.release();
			read = null;
			// 关闭链路
			reset();
		}
	}

	@Override
	protected void received(Throwable e) {
		// 读取失败
		read.release();
		read = null;
		if (e instanceof AsynchronousCloseException) {
			// 正在执行通道关闭
			return;
		} else if (e instanceof InterruptedByTimeoutException) {
			// 接收数据超时
			try {
				handler().received(this, null);
			} catch (Exception e1) {
				handler().error(this, e1);
			}
			return;
		} else {
			handler().error(this, e);
		}
	}

	private M send_message;
	private DataBuffer write;

	/**
	 * 发送数据到网络
	 * <p>
	 * 此方法不是多线程安全的，调用者应确保上一次发送返回后才能执行下一个消息发送。
	 * </p>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void send(Object message) {
		// 发送
		if (send_message == null) {
			send_message = (M) message;
			try {
				// 执行消息编码
				write = handler().encode(this, (M) message);
				if (write == null) {
					throw new IllegalStateException("消息未编码数据 " + message);
				} else if (write.readable() <= 0) {
					throw new IllegalStateException("消息编码零数据 " + message);
				} else {
					socket_channel.write(//
						write.read(), // ByteBuffer
						handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, // Timeout
						this, ClientSendHandler.INSTANCE // Handler
					);
				}
			} catch (Exception e) {
				send_message = null;
				if (write != null) {
					write.release();
					write = null;
				}
				handler().error(this, e);
			}
		}
	}

	@Override
	protected void sent(int size) {
		// 发送返回
		if (size > 0) {
			// 已发送数据
			write.read(size);
			if (write.readable() > 0) {
				// 数据未发完,继续发送
				socket_channel.write(//
					write.read(), // ByteBuffer
					handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, // Timeout
					this, ClientSendHandler.INSTANCE// Handler
				);
			} else {
				// 数据已发完
				// 必须在通知处理对象之前清空当前消息关联
				final M message = send_message;
				send_message = null;
				write.release();
				write = null;
				try {
					handler().sent(this, message);
				} catch (Exception e) {
					handler().error(this, e);
				}
			}
		} else if (size == 0) {
			// 客户端缓存满会导致零发送
			// 恶意程序，可能会导致无限尝试
			send_message = null;
			write.release();
			write = null;

			handler().error(this, new IllegalStateException("零写:客户端未能接收数据"));
		} else {
			// 连接被客户端断开
			send_message = null;
			write.release();
			write = null;
			reset();
		}
	}

	@Override
	protected void sent(Throwable e) {
		// 发送失败
		// completed()方法抛出的异常不会到达此方法
		send_message = null;
		if (write != null) {
			write.release();
			write = null;
		}
		if (e instanceof AsynchronousCloseException) {
			// 正在执行通道关闭
			return;
		} else if (e instanceof InterruptedByTimeoutException) {
			// 发送数据超时
			try {
				handler().sent(this, null);
			} catch (Exception e1) {
				handler().error(this, e1);
			}
		} else {
			handler().error(this, e);
		}
	}

	private void reset() {
		// 关闭链路
		if (socket_channel != null) {
			if (connected) {
				connected = false;
				if (socket_channel.isOpen()) {
					try {
						socket_channel.shutdownInput();
						socket_channel.shutdownOutput();
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
			try {
				socket_channel.close();
			} catch (IOException e) {
				handler().error(this, e);
			} finally {
				socket_channel = null;
			}
			if (read != null) {
				read.release();
				read = null;
			}
			if (write != null) {
				write.release();
				write = null;
			}
		}

		if (!closed) {
			heartbeat_task.stop();
			// 延迟一定时间后尝试重新连接
			reconnect_task.start();
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
		if (closed) {
		} else {
			closed = true;
			reset();

			ChainGroup.off(this);
			reconnect_task.stop();
			heartbeat_task.stop();
		}
	}
}