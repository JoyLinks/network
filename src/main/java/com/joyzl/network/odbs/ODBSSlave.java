/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 *
 */
package com.joyzl.network.odbs;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPSlave;

/**
 * TCP从连接，由TCPServer创建
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月9日
 *
 */
public class ODBSSlave<M extends ODBSMessage> extends TCPSlave<M> {

	private final ReentrantLock k = new ReentrantLock(true);
	private final ArrayDeque<M> messages = new ArrayDeque<>(Byte.MAX_VALUE);

	public ODBSSlave(ODBSServer<M> server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_SLAVE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(Object message) {
		k.lock();
		try {
			if (messages.isEmpty()) {
				messages.addLast((M) message);
			} else {
				messages.addLast((M) message);
				return;
			}
		} finally {
			k.unlock();
		}
		super.send(message);
	}

	/**
	 * 当前消息发送完成，如果有其它消息继续发送
	 */
	protected void sent(M message) {
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