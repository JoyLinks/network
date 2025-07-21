/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.LinkQueue;
import com.joyzl.network.chain.TCPSlave;

/**
 * HTTP 服务端从连接
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class HTTPSlave extends TCPSlave {

	private final ReentrantLock k = new ReentrantLock(true);

	public HTTPSlave(HTTPServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	// HTTP2
	private HTTP2Index<Request> messages;
	private Stream<Message> stream;
	private HPACK hpackRequest, hpackResponse;
	private int id = 2;

	public int nextId() {
		// 服务端使用偶数流编号
		return id += 2;
	}

	public boolean isHTTP2() {
		return hpackRequest != null && hpackResponse != null;
	}

	/** 切换链路为HTTP2 */
	protected void upgradeHTTP2() {
		messages = new HTTP2Index<>(100);
		stream = new Stream<>(100);
		hpackRequest = new HPACK();
		hpackResponse = new HPACK();
	}

	protected HTTP2Index<Request> messages() {
		return messages;
	}

	protected Stream<Message> stream() {
		return stream;
	}

	protected HPACK requestHPACK() {
		return hpackRequest;
	}

	protected HPACK responseHPACK() {
		return hpackResponse;
	}

	// HTTP 1.1 1.0
	// 提供缓存请求消息支持
	// 请求消息可能需要多次接收数据解码才能完成
	private LinkQueue<Message> queue = new LinkQueue<>();
	private Request request;

	protected LinkQueue<Message> queue() {
		return queue;
	}

	protected Request getRequest() {
		return request;
	}

	protected void setRequest(Request value) {
		request = value;
	}

	// WEB Socket
	private WEBSocketHandler webSockethandler;

	/** 升级链路为WebSocket，绑定消息处理对象 */
	public void upgrade(WEBSocketHandler handler) {
		webSockethandler = handler;
	}

	public boolean isWEBSocket() {
		return webSockethandler != null;
	}

	public WEBSocketHandler getWEBSocketHandler() {
		return webSockethandler;
	}

	@Override
	public void send(Object message) {
		if (message instanceof Message m) {
			if (isHTTP2()) {
				k.lock();
				try {
					if (m.id() < 0) {
						m.id(nextId());
					}
					if (stream.isEmpty()) {
						stream.add(m);
						if (sendMessage() == null) {
							sendMessage(m = stream.stream());
						} else {
							return;
						}
					} else {
						stream.add(m);
						return;
					}
				} finally {
					k.unlock();
				}
				super.send(message);
			} else {
				k.lock();
				try {
					if (queue.isEmpty()) {
						queue.add(m);
						if (sendMessage() == null) {
							sendMessage(m = queue.read());
						} else {
							return;
						}
					} else {
						queue.add(m);
						return;
					}
				} finally {
					k.unlock();
				}
				super.send(message);
			}
		} else {
			throw new IllegalArgumentException("HTTP:无效的消息对象类型");
		}
	}

	protected void sendNext(boolean complete) {
		if (isHTTP2()) {
			k.lock();
			try {
				if (complete) {
					stream.remove();
				}
				if (stream.isEmpty()) {
					return;
				}
				if (sendMessage() == null) {
					sendMessage(stream.stream());
				} else {
					return;
				}
			} finally {
				k.unlock();
			}
			super.send(sendMessage());
		} else {
			k.lock();
			try {
				if (complete) {
					queue.poll();
				}
				if (queue.isEmpty()) {
					return;
				}
				if (sendMessage() == null) {
					sendMessage(queue.peek());
				} else {
					return;
				}
			} finally {
				k.unlock();
			}
			super.send(sendMessage());
		}
	}
}