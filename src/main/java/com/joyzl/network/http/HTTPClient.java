/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.IndexMap;
import com.joyzl.network.LinkQueue;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPLink;

/**
 * HTTP 客户端
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class HTTPClient extends TCPLink {

	private final ReentrantLock k = new ReentrantLock(true);

	public HTTPClient(ChainHandler handler, String host, int port) {
		super(handler, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_CLIENT;
	}

	// HTTP 2
	private final IndexMap<Response> messages = new IndexMap<>();
	private final Stream<Message> stream = new Stream<>();
	private HPACK hpackRequest, hpackResponse;
	private int id = 1;

	private int nextId() {
		// 客户端使用奇数流编号
		return id += 2;
	}

	public boolean isHTTP2() {
		return hpackRequest != null && hpackResponse != null;
	}

	/** 切换链路为HTTP2 */
	void upgradeHTTP2() {
		hpackRequest = new HPACK();
		hpackResponse = new HPACK();
	}

	IndexMap<Response> messages() {
		return messages;
	}

	Stream<Message> stream() {
		return stream;
	}

	HPACK requestHPACK() {
		return hpackRequest;
	}

	HPACK responseHPACK() {
		return hpackResponse;
	}

	// HTTP 1.1 1.0
	// 提供缓存请求消息支持
	// 请求消息可能需要多次接收数据解码才能完成
	private LinkQueue<Message> queue = new LinkQueue<>();
	private Response response;

	protected LinkQueue<Message> queue() {
		return queue;
	}

	protected Response getResponse() {
		return response;
	}

	protected void setResponse(Response value) {
		response = value;
	}

	// WEB Socket
	private WEBSocketHandler webSockethandler;

	/** 升级链路为WebSocket，绑定消息处理对象 */
	public void upgrade(WEBSocketHandler handler) {
		webSockethandler = handler;
	}

	public WEBSocketHandler getWEBSocketHandler() {
		return webSockethandler;
	}

	public boolean isWEBSocket() {
		return webSockethandler != null;
	}

	@Override
	public void send(Object message) {
		if (message instanceof Message m) {
			if (isHTTP2()) {
				k.lock();
				try {
					if (message instanceof Request request) {
						request.id(nextId());
					}
					if (stream.isEmpty()) {
						stream.add(m);
						if (sendMessage() == null) {
							sendMessage(message = stream.stream());
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
							sendMessage(message = queue.read());
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
					queue.next();
				}
				if (queue.isEmpty()) {
					return;
				}
				if (sendMessage() == null) {
					sendMessage(queue.read());
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