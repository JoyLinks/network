/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 重庆骄智科技有限公司
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
		return receiveMessage;
	}

	/**
	 * 从网络接收数据
	 */
	@Override
	public void receive() {
		if (connected) {
			if (receiveMessage == null) {
				if (read == null) {
					read = DataBuffer.instance();
					// SocketChannel不能投递多个接收操作，否则会收到ReadPendingException异常
					socketChannel.read(//
						read.write(), // ByteBuffer
						handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
						this, SlaveReceiveHandler.INSTANCE // Handler
					);
				}
			}
		}
	}

	@Override
	protected void received(int size) {
		if (size > 0) {
			read.written(size);
			try {
				// 多次请求解包直到没有对象返回
				// 在数据包粘连的情况下，可能会接收到两个数据包的数据
				// 在数据包截断的情况下，可能会收到半个数据包
				while (true) {
					size = read.readable();
					receiveMessage = handler().decode(this, read);
					if (receiveMessage == null) {
						// 数据未能解析消息对象,继续接收数据
						break;
					} else {
						// 已解析消息对象
						if (read.readable() >= size) {
							throw new IllegalStateException("TCPSlave:已解析消息但数据未减少");
						}
						if (read.readable() > 0) {
							handler().received(this, receiveMessage);
							// 有剩余数据,继续尝试解包
							continue;
						} else {
							handler().received(this, receiveMessage);
							// 没有剩余数据,停止尝试解包
							break;
						}
					}
				}
				if (connected) {
					// 继续接收数据
					socketChannel.read(//
						read.write(), // ByteBuffer
						handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
						this, SlaveReceiveHandler.INSTANCE // Handler
					);
				} else {
					read.release();
					read = null;
				}
			} catch (Exception e) {
				read.release();
				read = null;
				handler().error(this, e);
				close();
			}
		} else if (size == 0) {
			// 没有数据并且没有达到流的末端时返回0
			// 如果用于接收的ByteBuffer缓存满则会出现读零情况
			// 代码存在问题才会导致提供了一个已满的ByteBuffer
			read.release();
			read = null;
			handler().error(this, new IllegalStateException("TCPSlave:零读"));
			close();
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
				throw new IllegalStateException("TCPSlave:发送冲突" + message);
			}
		} else {
			throw new IllegalStateException("TCPSlave:连接断开" + message);
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
				write.release();
				write = null;
				final Object message = sendMessage;
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
				// 20250420
				// 重置时清空关联消息导致处理程序收到空消息通知而被判定为超时
				// 实际上是由以下清空消息代码执行于通知之前导致
				if (receiveMessage != null) {
					try {
						if (receiveMessage instanceof Closeable) {
							((Closeable) receiveMessage).close();
						}
					} catch (IOException e) {
						handler().error(this, e);
					} finally {
						// 仅关闭不清空
						// receiveMessage = null;
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
						// 仅关闭不清空
						// sendMessage = null;
					}
				}

				try {
					clearContext();
				} catch (IOException e) {
					handler().error(this, e);
				}
			}
		}
	}

	@Override
	public void reset() {
		close();
	}
}