/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 中翌智联（重庆）科技有限公司
 */
package com.joyzl.network.chain;

import java.io.Closeable;
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
public class TCPSlave extends Slave {

	private final static Object BUSY = new Object();

	private final SocketAddress remote;
	private final AsynchronousSocketChannel socketChannel;
	private volatile boolean connected = true;

	public TCPSlave(TCPServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, Point.getPoint(channel.getRemoteAddress()));
		socketChannel = channel;
		socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
		socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
		remote = channel.getRemoteAddress();
		server.addSlave(this);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_SLAVE;
	}

	@Override
	public boolean active() {
		return connected && socketChannel.isOpen();
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
		if (connected) {
			try {
				return socketChannel.getLocalAddress();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////

	private Object receiveMessage;
	private DataBuffer read;

	/**
	 * 当前收到的消息
	 */
	public Object receiveMessage() {
		if (receiveMessage == BUSY) {
			return null;
		}
		return receiveMessage;
	}

	/**
	 * 从网络接收数据
	 * <p>
	 * 此方法不是多线程安全的，调用者应确保上一次数据接收返回之后才能再次接收数据
	 * </p>
	 */
	@Override
	public void receive() {
		if (connected) {
			if (receiveMessage == null) {
				receiveMessage = BUSY;
				if (read == null) {
					read = DataBuffer.instance();
				}
				// SocketChannel不能投递多个接收操作，否则会收到ReadPendingException异常
				socketChannel.read(//
					read.write(), // ByteBuffer
					handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
					this, SlaveReceiveHandler.INSTANCE // Handler
				);
			}
		}
	}

	@Override
	protected void received(int size) {
		if (size > 0) {
			read.written(size);
			try {
				// 多次请求解包直到没有对象返回
				while (true) {
					size = read.readable();
					// 在数据包粘连的情况下，可能会接收到两个数据包的数据
					receiveMessage = handler().decode(this, read);
					if (receiveMessage == null) {
						// 数据未能解析消息对象,继续接收数据
						socketChannel.read(//
							read.write(), // ByteBuffer
							handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
							this, SlaveReceiveHandler.INSTANCE // Handler
						);
						break;
					} else {
						// 已解析消息对象
						if (read.readable() >= size) {
							throw new IllegalStateException("TCPSlave:已解析消息但数据未减少");
						}
						if (read.readable() > 0) {
							// 注意:handler().received()方法中可能会调用receive()
							handler().received(this, receiveMessage);
							// 有剩余数据,继续尝试解包
							continue;
						} else {
							final Object message = receiveMessage;
							receiveMessage = null;
							handler().received(this, message);
							// 没有剩余数据,停止尝试解包
							break;
						}
					}
				}
			} catch (Exception e) {
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
			handler().error(this, new IllegalStateException("TCPSlave:零读"));
		} else {
			// 链路被关闭
			read.release();
			read = null;
			close();
		}
	}

	@Override
	protected void received(Throwable e) {
		// 读取失败
		if (read != null) {
			read.release();
			read = null;
		}
		if (e instanceof AsynchronousCloseException) {
			// 正在执行通道关闭
			// 忽略此异常
			return;
		} else if (e instanceof InterruptedByTimeoutException) {
			// 接收数据超时
			// 通知处理程序
			try {
				handler().received(this, null);
			} catch (Exception e1) {
				handler().error(this, e1);
				close();
			}
		} else {
			handler().error(this, e);
			close();
		}
	}

	private Object sendMessage;
	private DataBuffer write;

	/**
	 * 当前正在发送的消息
	 */
	public Object sendMessage() {
		return sendMessage;
	}

	protected void sendMessage(Object message) {
		sendMessage = message;
	}

	/**
	 * 发送数据到网络
	 * <p>
	 * 此方法不是多线程安全的，调用者应确保上一次发送返回后才能执行下一个消息发送。
	 * </p>
	 */
	@Override
	public void send(Object message) {
		if (connected) {
			if (sendMessage == null || sendMessage == message) {
				sendMessage = message;
				try {
					// 执行消息编码
					write = handler().encode(this, message);
					if (write == null) {
						throw new IllegalStateException("TCPSlave:未编码数据" + message);
					} else if (write.readable() <= 0) {
						throw new IllegalStateException("TCPSlave:编码零数据" + message);
					} else {
						socketChannel.write(//
							write.read(), // ByteBuffer
							handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, // Timeout
							this, SlaveSendHandler.INSTANCE // Handler
						);
					}
				} catch (Exception e) {
					if (write != null) {
						write.release();
						write = null;
					}
					handler().error(this, e);
					close();
				}
			} else {
				System.out.println(message);
			}
		} else {
			System.out.println(message);
		}
	}

	@Override
	protected void sent(int size) {
		if (size > 0) {
			write.read(size);
			if (write.readable() > 0) {
				// 数据未发完,继续发送
				socketChannel.write(//
					write.read(), // ByteBuffer
					handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, // Timeout
					this, SlaveSendHandler.INSTANCE// Handler
				);
			} else {
				// 数据已发完
				// 必须在通知处理对象之前清空当前消息关联
				final Object message = sendMessage;
				write.release();
				write = null;
				sendMessage = null;
				try {
					handler().sent(this, message);
				} catch (Exception e) {
					handler().error(this, e);
					close();
				}
			}
		} else if (size == 0) {
			// 客户端缓存满会导致零发送
			// 恶意程序，可能会导致无限尝试
			write.release();
			write = null;
			handler().error(this, new IllegalStateException("TCPSlave:零写"));
			close();
		} else {
			// 连接被客户端断开
			write.release();
			write = null;
			close();
		}
	}

	@Override
	protected void sent(Throwable e) {
		// 发送失败
		if (write != null) {
			write.release();
			write = null;
		}
		if (e instanceof AsynchronousCloseException) {
			// 正在执行通道关闭
			// 忽略此异常
			return;
		} else if (e instanceof InterruptedByTimeoutException) {
			// 发送数据超时
			// 通知处理程序
			try {
				handler().sent(this, null);
			} catch (Exception e1) {
				handler().error(this, e1);
				close();
			}
		} else {
			handler().error(this, e);
			close();
		}
	}

	@Override
	public void close() {
		if (connected) {
			synchronized (this) {
				if (connected) {
					connected = false;
				} else {
					return;
				}
			}

			if (socketChannel.isOpen()) {
				server().offSlave(this);
				try {
					socketChannel.shutdownOutput();
					socketChannel.shutdownInput();
					socketChannel.close();
				} catch (Exception e) {
					server().handler().error(this, e);
				} finally {
					try {
						server().handler().disconnected(this);
					} catch (Exception e) {
						server().handler().error(this, e);
					}
				}

				// if (read != null) {
				// read.release();
				// read = null;
				// }
				// if (write != null) {
				// write.release();
				// write = null;
				// }

				// 消息中可能有打开的资源
				// 例如发送未完成的文件
				if (receiveMessage != null) {
					try {
						if (receiveMessage instanceof Closeable) {
							((Closeable) receiveMessage).close();
						}
					} catch (IOException e) {
						handler().error(this, e);
					} finally {
						receiveMessage = null;
					}
				}
				if (sendMessage != null) {
					try {
						if (sendMessage instanceof Closeable) {
							((Closeable) sendMessage).close();
						}
					} catch (IOException e) {
						handler().error(this, e);
					} finally {
						sendMessage = null;
					}
				}
			}
		}
	}
}