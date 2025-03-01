/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.MessageQueue;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPClient;

/**
 * 基于TCP Odbs连接的客户端
 * 
 * @author ZhangXi 2019年7月12日
 *
 */
public class ODBSClient extends TCPClient {

	private final ReentrantLock k = new ReentrantLock(true);
	private final MessageQueue<ODBSMessage> messages = new MessageQueue<>();

	public ODBSClient(ODBSClientHandler<?> h, String host, int port) {
		super(h, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_CLIENT;
	}

	@Override
	public void send(Object message) {
		k.lock();
		try {
			((ODBSMessage) message).tag(messages.add((ODBSMessage) message));
			if (messages.queue() > 1) {
				return;
			}
			if (sendMessage() != null) {
				return;
			}
			sendMessage(message = messages.peek());
		} finally {
			k.unlock();
		}
		super.send(message);
	}

	protected void sendNext() {
		ODBSMessage message;
		k.lock();
		try {
			if (sendMessage() != null) {
				return;
			}
			message = messages.peek();
			if (message == null) {
				return;
			}
			sendMessage(message);
		} finally {
			k.unlock();
		}
		super.send(message);
	}

	/**
	 * 取出标识的消息
	 */
	protected ODBSMessage take(int tag) {
		k.lock();
		try {
			return messages.take(tag);
		} finally {
			k.unlock();
		}
	}

	public MessageQueue<ODBSMessage> messages() {
		return messages;
	}
}