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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.Executor;
import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * 基于TCP连接的客户端，短链接无心跳和重连机制
 * <p>
 * 调用connect()连接，调用receive()接收一个周期(既一次解码完成)，需要继续接收数据需要再次调用
 *
 * @author simon(ZhangXi TEL : 13883833982) 2019年7月12日
 */
public class TCPShort<M> extends Client<M> {

	private final SocketAddress address;
	private AsynchronousSocketChannel socket_channel;

	/**
	 * 创建TCPShort由接点标识指定连接信息
	 * <p>
	 * 自动生成接点标识 "192.168.0.1:1030"
	 *
	 * @param handler {@link ChainHandler}
	 * @param host 主机
	 * @param port 端口
	 */
	public TCPShort(ChainHandler<M> handler, String host, int port) {
		super(handler);
		address = new InetSocketAddress(host, port);
		// ChainGroup.add(this);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_SHORT;
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
					socket_channel.connect(address, this, TCP_CONNECT_HANDLER);
				} else {
					socket_channel = null;
				}
			} catch (Exception e) {
				handler().error(this, e);
			}
		}
	}

	@Override
	public void close() {
		if (socket_channel == null) {
			return;
		} else {
			if (socket_channel.isOpen()) {
				try {
					socket_channel.shutdownInput();
					socket_channel.shutdownOutput();
					// socket_channel.wait(10);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						handler().disconnected(this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (socket_channel != null) {
				try {
					socket_channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					socket_channel = null;
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	protected final CompletionHandler<Void, TCPShort<M>> TCP_CONNECT_HANDLER = new CompletionHandler<Void, TCPShort<M>>() {

		@Override
		public void completed(Void result, TCPShort<M> chain) {
			// chain.state = ChainState.CONNECTED;
			// Simon(ZhangXi) 2020-05-25
			// 在执行handler().connected()之前必须设置连接状态connected=true，否则handler().connected()事件中将无法发送数据
			// 因为Chain发送时会检查连接状态Chain.Active()，如果connected不为true，数据将不会发送
			try {
				chain.handler().connected(chain);
			} catch (Exception e) {
				chain.handler().error(chain, e);
			}
		}

		@Override
		public void failed(Throwable e, TCPShort<M> chain) {
			// completed()方法抛出的异常不会到达此方法
			// 连接不成功通道处于关闭状态isOpen()为false
			if (e instanceof AsynchronousCloseException) {
				// 主动关闭正在尝试连接的链路
				return;
			}
			chain.handler().error(chain, e);
			chain.close();
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
		socket_channel.read(buffer.getWriteableBuffer(), handler().getTimeoutRead(), TimeUnit.MILLISECONDS, buffer, TCP_READ_HANDLER);
	}

	protected final CompletionHandler<Integer, DataBuffer> TCP_READ_HANDLER = new CompletionHandler<>() {

		@Override
		public void completed(Integer result, DataBuffer buffer) {
			if (result > 0) {
				buffer.writtenBuffers(result.intValue());
				M source;
				try {
					while (true) {
						// 继续请求解包直到没有对象返回
						// 在数据包粘连的情况下，可能会接收到两个数据包的数据
						source = handler().decode(TCPShort.this, buffer);
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

									handler().received(TCPShort.this, source);
									// 有剩余数据,继续尝试解包,继续接收数据
									continue;
								} else {
									// 释放被持有的对象(此时并不会真正释放，而是将持有状态恢复为无多重引用)
									buffer.release();
									buffer = null;
									reading.set(false);

									handler().received(TCPShort.this, source);
									// 没有剩余数据,停止尝试解包,停止接收数据
									return;
								}
							} else {
								if (buffer.hasResidue() || buffer.readable() > 0) {
									// 解包成功但是还有剩余数据

									handler().received(TCPShort.this, source);
									// 有剩余数据,继续尝试解包,继续接收数据
								} else {
									buffer.release();
									buffer = null;
									reading.set(false);

									handler().received(TCPShort.this, source);
									// 没有剩余数据,停止尝试解包,停止接收数据
									return;
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
				// 关闭链路
				close();
			}
		}

		@Override
		public void failed(Throwable e, DataBuffer buffer) {
			// IO操作失败进入此方法
			// completed()方法抛出的异常不会到达此方法

			if (buffer != null) {
				reading.set(false);
				// IO失败时DataBuffer不会进入处理方法，须释放
				buffer.release();
			}
			if (e instanceof AsynchronousCloseException) {
				// 正在执行通道关闭
				return;
			}
			if (e instanceof InterruptedByTimeoutException) {
				// 接收数据超时
				try {
					handler().received(TCPShort.this, null);
				} catch (Exception e1) {
					handler().error(TCPShort.this, e1);
				}
			}
			handler().error(TCPShort.this, e);

			// 连接异常关闭由业务处理决定
			// chain.close();
		}
	};

	// 用于发送数据的锁
	final ReentrantLock writing = new ReentrantLock();

	@Override
	@SuppressWarnings("unchecked")
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

	@Override
	@SuppressWarnings("unchecked")
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

	private void write(DataBuffer buffer) {
		if (buffer == null) {
		} else if (buffer.hasReadableBuffer()) {
			// 当前数据未发送完,须继续发送
			socket_channel.write(buffer.getReadableBuffer(), handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, buffer, TCP_WRITE_HANDLER);
		} else {
			buffer.release();
			// 数据发送完成，请求继续编码
			write((Object) null);
		}
	}

	protected final CompletionHandler<Integer, DataBuffer> TCP_WRITE_HANDLER = new CompletionHandler<>() {

		@Override
		public void completed(Integer result, DataBuffer write_buffer) {
			if (result > 0) {
				write_buffer.readBuffers(result.intValue());
				write(write_buffer);
			} else if (result == 0) {
				// 客户端缓存满，继续尝试发送
				// 恶意程序，可能会导致无限尝试
				write(write_buffer);
			} else {
				write_buffer.release();
				// 连接被客户端断开
				close();
			}
		}

		@Override
		public void failed(Throwable e, DataBuffer write_buffer) {
			// 发送失败,释放数据和对象
			// completed()方法抛出的异常不会到达此方法

			if (write_buffer != null) {
				write_buffer.release();
			}
			if (e instanceof AsynchronousCloseException) {
				// 正在执行通道关闭
				return;
			}
			if (e instanceof InterruptedByTimeoutException) {
				// 发送数据超时
				try {
					handler().sent(TCPShort.this, null);
				} catch (Exception e1) {
					handler().error(TCPShort.this, e1);
				}
			}
			handler().error(TCPShort.this, e);

			// 连接异常关闭由业务处理决定
			// chain.close();
		}
	};
}