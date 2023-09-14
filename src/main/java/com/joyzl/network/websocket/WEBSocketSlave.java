/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.websocket;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.http.Message;
import com.joyzl.network.web.WEBSlave;

/**
 * HTTP 服务端连接
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBSocketSlave extends WEBSlave {

	private ReentrantLock k;
	private WEBSocket servlet;
	private ArrayDeque<Message> messages;
	private boolean websocket = false;

	public WEBSocketSlave(WEBSocketServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	@Override
	public ChainType type() {
		if (websocket) {
			return ChainType.TCP_HTTP_SLAVE_WEB_SOCKET;
		}
		return ChainType.TCP_HTTP_SLAVE;
	}

	/**
	 * 升级链路为WEBSocket
	 */
	public void upgrade() {
		websocket = true;
		k = new ReentrantLock(true);
		messages = new ArrayDeque<>(Byte.MAX_VALUE);
	}

	@Override
	public void send(Object message) {
		if (websocket) {
			k.lock();
			try {
				if (messages.isEmpty()) {
					messages.addLast((Message) message);
				} else {
					messages.addLast((Message) message);
					return;
				}
			} finally {
				k.unlock();
			}
		}
		super.send(message);
	}

	/**
	 * 当前消息发送完成，如果有其它消息继续发送
	 */
	protected void sent(Message message) {
		if (websocket) {
			k.lock();
			try {
				message = messages.removeFirst();
				if (message == null) {
					return;
				}
				message = messages.peekFirst();
				if (message == null) {
					return;
				}
			} finally {
				k.unlock();
			}
			super.send(message);
		}
	}

	public WEBSocket getServlet() {
		return servlet;
	}

	public void setServlet(WEBSocket value) {
		servlet = value;
	}
}
