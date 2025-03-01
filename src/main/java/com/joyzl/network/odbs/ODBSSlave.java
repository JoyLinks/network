/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.MessageQueue;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPSlave;

/**
 * TCP从连接，由TCPServer创建
 *
 * @author ZhangXi 2019年7月9日
 *
 */
public class ODBSSlave extends TCPSlave {

	private final ReentrantLock k = new ReentrantLock(true);
	private final MessageQueue<ODBSMessage> messages = new MessageQueue<>();

	public ODBSSlave(ODBSServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_SLAVE;
	}

	@Override
	public void send(Object message) {
		k.lock();
		try {
			if (sendMessage() != null) {
				messages.add((ODBSMessage) message);
				return;
			}
			if (messages.queue() > 0) {
				messages.add((ODBSMessage) message);
				return;
			}
			sendMessage(message);
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
			message = messages.poll();
			if (message == null) {
				return;
			}
			sendMessage(message);
		} finally {
			k.unlock();
		}
		super.send(message);
	}

	public MessageQueue<ODBSMessage> messages() {
		return messages;
	}
}