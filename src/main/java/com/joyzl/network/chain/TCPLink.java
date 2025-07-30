/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
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
public class TCPLink extends Client {

	private final InetSocketAddress remote;
	private AsynchronousSocketChannel socket_channel;
	private volatile boolean connected = false;

	/**
	 * 创建TCPShort由接点标识指定连接信息
	 * <p>
	 * 自动生成接点标识 "192.168.0.1:1030"
	 *
	 * @param handler {@link ChainHandler}
	 * @param host 主机
	 * @param port 端口
	 */
	public TCPLink(ChainHandler handler, String host, int port) {
		super(handler);
		remote = new InetSocketAddress(host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_CLIENT;
	}

	@Override
	public boolean active() {
		return connected;
	}

	@Override
	public String point() {
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
				return socket_channel.getLocalAddress();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////

	// 待连接:channel==null && connected==false
	// 连接中:channel!=null && connected==false
	// 已连接:channel!=null && connected==true
	// 不可读:
	// 不可写:
	// 关闭中:channel!=null && connected==false
	// 已关闭:channel==null && connected==false

	@Override
	public void connect() {
		// 此方法多次调用须防止已创建的AsynchronousSocketChannel对象实例泄露
		// 持续调用最终会导致AsynchronousSocketChannel创建抛出"文件打开过多异常"
		// 已关闭的AsynchronousSocketChannel不能重用否则抛出ClosedChannelException
		if (connected) {
			throw new IllegalStateException("TCPLink:重复连接");
		}
		if (socket_channel == null) {
			try {
				synchronized (this) {
					if (socket_channel == null) {
						socket_channel = AsynchronousSocketChannel.open(Executor.channelGroup());
					} else {
						return;
					}
				}
				if (socket_channel.isOpen()) {
					// 启用保持连接活跃
					socket_channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
					// 禁用小数据报合并
					socket_channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
				} else {
					socket_channel = null;
					return;
				}
				// 如果无法建立连接，通道状态为关闭。
				socket_channel.connect(remote, this, TCPLinkConnecter.INSTANCE);
			} catch (Exception e) {
				socket_channel = null;
				handler().error(this, e);
			}
		} else {
			throw new IllegalStateException("TCPLink:正在连接");
		}
	}

	protected void connected() {
		// 连接成功
		connected = true;
		receiveMessage = null;
		sendMessage = null;
		try {
			handler().connected(this);
		} catch (Exception e) {
			handler().error(this, e);
			reset();
		}
	}

	protected void connected(Throwable e) {
		// 连接异常
		connected = false;
		socket_channel = null;
		handler().error(this, e);
	}

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
					socket_channel.read(//
						read.write(), // ByteBuffer
						handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
						this, TCPLinkReceiver.INSTANCE // Handler
					);
				}
			}
		}
	}

	protected void received(int size) {
		if (size > 0) {
			read.written(size);
			try {
				// 多次请求解包直到没有对象返回
				// 在数据包粘连的情况下，可能会接收到两个数据包
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
							throw new IllegalStateException("TCPLink:已解析消息但数据未减少");
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
					socket_channel.read(//
						read.write(), // ByteBuffer
						handler().getTimeoutRead(), TimeUnit.MILLISECONDS, // Timeout
						this, TCPLinkReceiver.INSTANCE // Handler
					);
				} else {
					read.release();
					read = null;
				}
			} catch (Exception e) {
				read.release();
				read = null;
				handler().error(this, e);
				reset();
			}
		} else if (size == 0) {
			// 没有数据并且没有达到流的末端时返回0
			// 如果用于接收的ByteBuffer缓存满则会出现读零情况
			// 代码存在问题才会导致提供了一个已满的ByteBuffer
			read.release();
			read = null;
			handler().error(this, new IllegalStateException("TCPLink:零读"));
			reset();
		} else {
			// 链路被关闭
			read.release();
			read = null;
			reset();
		}
	}

	protected void received(Throwable e) {
		// 读取失败
		if (read != null) {
			read.release();
			read = null;
		}
		if (e instanceof ClosedChannelException) {
			// 正在执行通道关闭
			// 忽略此异常
		} else if (e instanceof InterruptedByTimeoutException) {
			// 接收数据超时
			// 通知处理程序
			try {
				handler().received(this, null);
			} catch (Exception e1) {
				handler().error(this, e1);
			} finally {
				reset();
			}
		} else {
			handler().error(this, e);
			reset();
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
						throw new IllegalStateException("TCPLink:未编码数据" + message);
					} else if (write.readable() <= 0) {
						throw new IllegalStateException("TCPLink:编码零数据" + message);
					} else {
						socket_channel.write(//
							write.read(), // ByteBuffer
							handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, // Timeout
							this, TCPLinkSender.INSTANCE // Handler
						);
					}
				} catch (Exception e) {
					if (write != null) {
						write.release();
						write = null;
					}
					handler().error(this, e);
					reset();
				}
			} else {
				throw new IllegalStateException("TCPLink:发送冲突" + message);
			}
		} else {
			throw new IllegalStateException("TCPLink:还未连接" + message);
		}
	}

	protected void sent(int size) {
		if (size > 0) {
			write.read(size);
			try {
				if (write.readable() > 0) {
					// 数据未发完,继续发送
					if (connected) {
						socket_channel.write(//
							write.read(), // ByteBuffer
							handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, // Timeout
							this, TCPLinkSender.INSTANCE// Handler
						);
					} else {
						write.release();
						write = null;
					}
				} else {
					// 数据已发完
					write.release();
					write = null;
					// 必须在通知处理对象之前清空当前消息关联
					// 20250420重置时导致最后发送的消息被置空
					final Object message = sendMessage;
					sendMessage = null;
					handler().sent(this, message);
				}
			} catch (Exception e) {
				if (write != null) {
					write.release();
					write = null;
				}
				handler().error(this, e);
				reset();
			}
		} else if (size == 0) {
			// 客户端缓存满会导致零发送
			// 恶意程序，可能会导致无限尝试
			write.release();
			write = null;
			handler().error(this, new IllegalStateException("TCPLink:零写"));
			reset();
		} else {
			// 连接被客户端断开
			write.release();
			write = null;
			reset();
		}
	}

	protected void sent(Throwable e) {
		// 发送失败
		if (write != null) {
			write.release();
			write = null;
		}
		if (e instanceof ClosedChannelException) {
			// 正在执行通道关闭
			// 忽略此异常
		} else if (e instanceof InterruptedByTimeoutException) {
			// 发送数据超时
			// 通知处理程序
			try {
				handler().sent(this, null);
			} catch (Exception e1) {
				handler().error(this, e1);
			} finally {
				reset();
			}
		} else {
			handler().error(this, e);
			reset();
		}
	}

	/**
	 * 重置链路，重置后可再次执行连接
	 */
	@Override
	public void reset() {
		if (connected) {
			synchronized (this) {
				if (connected) {
					connected = false;
				} else {
					return;
				}
			}

			try {
				if (socket_channel.isOpen()) {
					socket_channel.shutdownInput();
					socket_channel.shutdownOutput();
					socket_channel.close();
				}
			} catch (Exception e) {
				handler().error(this, e);
			} finally {
				socket_channel = null;
				try {
					handler().disconnected(this);
				} catch (Exception e) {
					handler().error(this, e);
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
		}
	}

	@Override
	public void close() {
		reset();
		try {
			clearContext();
		} catch (IOException e) {
			handler().error(this, e);
		}
	}
}