/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 中翌智联（重庆）科技有限公司
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.TimeUnit;

import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * TCP从连接，由TCPServer创建，此链路不会单独维护连接状态，从链路也不能断开后再重置连接
 * <p>
 * 工作机制：
 * <ol>
 * <li>receive()请求接收数据（异步）；</li>
 * <li>received()接收数据返回（成功/失败）；</li>
 * <li>send()请求发送数据（异步）；</li>
 * <li>sent()发送数据返回（成功/失败）；</li>
 * <li>close()关闭链路；</li>
 * </ol>
 * 使用者根据通信协议确定发送和接收数据的时机和机制；
 * 发送和接收数据可同时进行，但不能同时请求多个发送或多个接收，应等待上一次发送或接收返回后才能再次请求发送或接收数据。
 * 链路可关闭后不能重复使用。对象不是多线程安全的，在多线程收发情形下使用者应实现消息排队。
 * <p>
 *
 * @author ZhangXi 2019年7月9日
 *
 */
public class TCPSlave<M> extends Slave<M> {

	private final SocketAddress address;
	private final AsynchronousSocketChannel socket_channel;

	public TCPSlave(TCPServer<M> server, AsynchronousSocketChannel channel) throws IOException {
		super(server, Point.getPoint(channel.getRemoteAddress()));
		socket_channel = channel;
		socket_channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
		socket_channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
		address = channel.getRemoteAddress();
		server.addSlave(this);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_SLAVE;
	}

	@Override
	public boolean active() {
		return socket_channel != null && socket_channel.isOpen();
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
				this, SlaveReceiveHandler.INSTANCE // Handler
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
							this, SlaveReceiveHandler.INSTANCE // Handler
						);
						break;
					} else {
						// 已解析消息对象
						if (read.readable() >= size) {
							// 解析数据应减少
							throw new IllegalStateException("已解析消息但字节数据未减少");
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
			close();
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
		// 关闭链路
		close();
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
						this, SlaveSendHandler.INSTANCE // Handler
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
					this, SlaveSendHandler.INSTANCE// Handler
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
			close();
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
		// 关闭链路
		close();
	}

	@Override
	public void close() {
		if (socket_channel.isOpen()) {
			server().offSlave(this);
			try {
				socket_channel.shutdownInput();
				socket_channel.shutdownOutput();
				socket_channel.close();
			} catch (Exception e) {
				server().handler().error(this, e);
			} finally {
				// socket_channel = null;
				try {
					server().handler().disconnected(this);
				} catch (Exception e) {
					server().handler().error(this, e);
				}
			}
		}
	}
}