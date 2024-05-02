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
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.TimeUnit;

import com.joyzl.network.Executor;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * 基于TCP连接的链路，链接无心跳和重连机制
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
 * @author simon(ZhangXi TEL : 13883833982) 2019年7月12日
 */
public class TCPLink<M> extends Client<M> {

	private final SocketAddress address;
	private AsynchronousSocketChannel socket_channel;
	/** 连接状态 */
	private volatile boolean connected;

	/**
	 * 创建TCPShort由接点标识指定连接信息
	 * <p>
	 * 自动生成接点标识 "192.168.0.1:1030"
	 *
	 * @param handler {@link ChainHandler}
	 * @param host 主机
	 * @param port 端口
	 */
	public TCPLink(ChainHandler<M> handler, String host, int port) {
		super(handler);
		address = new InetSocketAddress(host, port);
		connected = false;
		// ChainGroup.add(this);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_SHORT;
	}

	@Override
	public boolean active() {
		return connected && socket_channel != null && socket_channel.isOpen();
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

	////////////////////////////////////////////////////////////////////////////////

	public void connect() {
		// 此方法多次调用须防止已创建的AsynchronousSocketChannel对象实例泄露
		// 持续调用最终会导致AsynchronousSocketChannel创建抛出"文件打开过多异常"
		// 已关闭的AsynchronousSocketChannel不能重用否则抛出java.nio.channels.ClosedChannelException
		if (socket_channel == null) {
			try {
				socket_channel = AsynchronousSocketChannel.open(Executor.channelGroup());
				if (socket_channel.isOpen()) {
					socket_channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
					socket_channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
				} else {
					socket_channel = null;
					return;
				}
				// 如果无法建立连接，则自动关闭通道。
				socket_channel.connect(address, this, ClientConnectHandler.INSTANCE);
			} catch (Exception e) {
				handler().error(this, e);
				// UnresolvedAddressException
				// java.io.IOException: 在其上下文中，该请求的地址无效。
				reset();
			}
		}
	}

	@Override
	protected void connected() {
		// 连接成功
		connected = true;
		try {
			handler().connected(this);
		} catch (Exception e) {
			handler().error(this, e);
		}
	}

	@Override
	protected void connected(Throwable e) {
		// 连接异常
		connected = false;
		if (e instanceof AsynchronousCloseException) {
			// 主动关闭正在尝试连接的链路
			return;
		} else if (e instanceof InterruptedByTimeoutException) {
			// 连接超时
		}
		// java.io.IOException:信号灯超时时间已到
		handler().error(this, e);
		// 重置链路
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
					size = read.readable();
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
						if (read.readable() >= size) {
							// 解析数据应减少
							throw new IllegalStateException("已解析消息但字节数据未减少");
						}
						if (read.readable() > 0) {
							handler().received(this, receive_message);
							// 注意:handler().received()方法中可能会调用receive()
							// 因此必须阻止用户发起数据接收，以下语句后执行
							receive_message = null;
							// 有剩余数据,继续尝试解包
							continue;
						} else {
							final M message = receive_message;
							// 注意:handler().received()方法中可能会调用receive()
							// 因此必须阻止用户发起数据接收，以下语句前执行
							receive_message = null;
							handler().received(this, message);
							// 没有剩余数据,停止尝试解包
							break;
						}
					}
				}
			} catch (Exception e) {
				receive_message = null;
				handler().error(this, e);
				read.release();
				read = null;
			}
		} else if (size == 0) {
			// 没有数据并且没有达到流的末端时返回0
			// 如果用于接收的ByteBuffer缓存满则会出现读零情况
			// DataBuffer代码存在问题才会导致提供了一个已满的ByteBuffer
			handler().error(this, new IllegalStateException("零读:当前缓存单元已满"));
			read.release();
			read = null;
		} else {
			// 链路被关闭
			// 缓存对象未投递给处理对象，须释放
			read.release();
			read = null;
			// 重置链路
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
		} else {
			handler().error(this, e);
		}
		// 重置链路
		reset();
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
				handler().error(this, e);
				if (write != null) {
					write.release();
					write = null;
				}
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
			handler().error(this, new IllegalStateException("零写:客户端未能接收数据"));
			write.release();
			write = null;
		} else {
			// 连接被客户端断开
			send_message = null;
			write.release();
			write = null;
			// 重置链路
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
		// 重置链路
		reset();
	}

	/**
	 * 重置链路，重置后可再次执行连接
	 */
	void reset() {
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
		}
	}

	@Override
	public void close() {
		reset();
	}
}