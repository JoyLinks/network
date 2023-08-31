/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.IndexItems;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPClient;

/**
 * 基于TCP Odbs连接的客户端
 * 
 * @author simon(ZhangXi TEL:13883833982) 2019年7月12日
 *
 */
public class TCPOdbsClient<M extends ODBSMessage> extends TCPClient<M> {

	private final ReentrantLock k = new ReentrantLock(true);
	private final IndexItems<M> items = new IndexItems<>(Byte.MAX_VALUE);
	private final ArrayDeque<M> messages = new ArrayDeque<>(Byte.MAX_VALUE);

	public TCPOdbsClient(OdbsClientHandler<M> h, String host, int port) {
		super(h, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_CLIENT;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(Object message) {
		k.lock();
		try {
			if (messages.isEmpty()) {
				messages.addLast((M) message);
				((M) message).tag(items.put((M) message));
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
			} else {
				((M) message).tag(items.put((M) message));
			}
		} finally {
			k.unlock();
		}
		super.send(message);
	}

	/**
	 * 取出标识的消息
	 * 
	 * @param tag
	 * @return M / null
	 */
	protected M take(int tag) {
		return items.take(tag);
	}
}