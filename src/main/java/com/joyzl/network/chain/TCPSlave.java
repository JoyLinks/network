/*
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
import java.nio.channels.CompletionHandler;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.Point;
import com.joyzl.network.buffer.DataBuffer;

/**
 * TCP从连接，由TCPServer创建
 *
 * <p>
 * 此连接提供持续不断的数据接收工作方式
 * </p>
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月9日
 *
 */
public class TCPSlave<M> extends Slave<M> {

	private final SocketAddress address;
	private AsynchronousSocketChannel socket_channel;

	public TCPSlave(TCPServer<M> server, AsynchronousSocketChannel channel) throws IOException {
		super(server, Point.getPoint(channel.getRemoteAddress()));
		socket_channel = channel;
		socket_channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
		socket_channel.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
		address = channel.getRemoteAddress();
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

	@Override
	public void close() {
		if (socket_channel != null) {
			server().offSlave(this);
			try {
				if (socket_channel.isOpen()) {
					socket_channel.shutdownInput();
					socket_channel.shutdownOutput();
					socket_channel.close();
					server().handler().disconnected(this);
				}
			} catch (Exception e) {
				socket_channel = null;
				server().handler().error(this, e);
			} finally {
				socket_channel = null;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

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
		socket_channel.read(buffer.getWriteableBuffer(), server().handler().getTimeoutRead(), TimeUnit.MILLISECONDS, buffer, TCP_READ_HANDLER);
	}

	protected final CompletionHandler<Integer, DataBuffer> TCP_READ_HANDLER = new CompletionHandler<>() {

		@Override
		public void completed(Integer result, DataBuffer buffer) {
			if (result > 0) {
				refreshLastRead();
				buffer.writtenBuffers(result.intValue());
				try {
					// 多次请求解包直到没有对象返回
					// A可能会接收到两个数据包的数据，粘连帧
					// B可能会收到不完整的数据包，半截帧
					// 多次解包没有残留数据则无须自动继续接收
					// 多次解包后还有残留数据则认为数据未接收完
					M source;
					while (true) {
						source = server().handler().decode(TCPSlave.this, buffer);
						if (source == null) {
							// 未能解析消息对象
							// 可能是数据未接收完，应继续接收数据
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

									server().handler().received(TCPSlave.this, source);
									// 解包成功且还有剩余数据,继续尝试解包
									continue;
								} else {
									// 释放被持有的对象(此时并不会真正释放，而是将持有状态恢复为无多重引用)
									buffer.release();
									buffer = null;
									reading.set(false);

									server().handler().received(TCPSlave.this, source);
									// 解包成功且没有剩余数据,停止继续解包
									break;
								}
							} else {
								if (buffer.hasResidue() || buffer.readable() > 0) {
									server().handler().received(TCPSlave.this, source);
									// 解包成功且还有剩余数据,继续尝试解包
									continue;
								} else {
									buffer.release();
									buffer = null;
									reading.set(false);

									server().handler().received(TCPSlave.this, source);
									// 没有剩余数据,停止继续解包
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					failed(e, buffer);
				}

				// DataBuffer是否可继续用于接收数据；
				// 1业务对象不再需要DataBuffer的数据/数据已经全部转移
				// 2业务对象需要DataBuffer原始数据/数据将在未来被使用
				// DataBuffer是否被实体对象持有，解包完成后是否继续用于接收数据
				// Netty的处理方式:release()将计数器减1,等于零时回收,调用retain()将计数器加1,谁用完谁释放
				// 是否继续接收:业务处理可决定是否继续接收数据
				// 如果DataBuffer不可继续接收,残留数据须转移
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
				// 通道关闭
				close();
			}
		}

		@Override
		public void failed(Throwable e, DataBuffer buffer) {
			// IO操作失败进入此方法
			// completed()方法抛出的异常不会到达此方法

			// IO失败时DataBuffer不会进入处理方法，须释放
			if (buffer != null) {
				reading.set(false);
				buffer.release();
			}

			if (e instanceof AsynchronousCloseException) {
				// 正在执行通道关闭
				return;
			}
			if (e instanceof InterruptedByTimeoutException) {
				// 接收数据超时
				try {
					server().handler().received(TCPSlave.this, null);
				} catch (Exception e1) {
					server().handler().error(TCPSlave.this, e1);
				}
			}
			server().handler().error(TCPSlave.this, e);
			// 连接异常关闭由业务处理决定
			// chain.close();
		}
	};

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
		// 此方法用于将消息对象编码为字节串
		// 通道数据发送完成后会再次调用此方法请求继续下一个消息编码
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
			socket_channel.write(buffer.getReadableBuffer(), server().handler().getTimeoutWrite(), TimeUnit.MILLISECONDS, buffer, TCP_WRITE_HANDLER);
		} else {
			buffer.release();
			// 数据发送完成，请求继续编码
			write((Object) null);
		}
	}

	protected final CompletionHandler<Integer, DataBuffer> TCP_WRITE_HANDLER = new CompletionHandler<>() {

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
				// 连接被客户端断开
				close();
			}
		}

		@Override
		public void failed(Throwable e, DataBuffer buffer) {
			// 发送失败,关闭链路,释放数据和对象
			// completed()方法抛出的异常不会到达此方法
			if (buffer != null) {
				buffer.release();
			}
			if (e instanceof AsynchronousCloseException) {
				// 正在执行通道关闭
				return;
			}
			if (e instanceof InterruptedByTimeoutException) {
				// 发送数据超时
				try {
					server().handler().sent(TCPSlave.this, null);
				} catch (Exception e1) {
					server().handler().error(TCPSlave.this, e1);
				}
			}
			server().handler().error(TCPSlave.this, e);
			// 连接异常关闭由业务处理决定
			// 并不是所有异常都需要关闭链路
			// chain.close();
		}
	};
}