/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.chain.ChainType;
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

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_SLAVE;
	}
	// 1问答模式
	// 2管道模式
	// 3多路复用

	@Override
	public void send(Object message) {
		k.lock();
		try {
			streams.add(message);
			if (sendMessage() == null) {
				sendMessage(message = streams.get());
			} else {
				return;
			}
		} finally {
			k.unlock();
		}
		super.send(message);
	}

	protected void sendNext() {
		k.lock();
		try {
			if (sendMessage() == null) {
				sendMessage(streams.get());
			} else {
				return;
			}
		} finally {
			k.unlock();
		}
		super.send(sendMessage());
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

	// HTTP2

	private HPACK request, response;
	private HTTP2Sender streams;

	public boolean isHTTP2() {
		return streams != null;
	}

	/** 切换链路为HTTP2 */
	void upgradeHTTP2() {
		request = new HPACK();
		response = new HPACK();
		streams = new HTTP2Sender();
	}

	HTTP2Sender streams() {
		return streams;
	}

	HPACK requestHPACK() {
		return request;
	}

	HPACK responseHPACK() {
		return response;
	}
}