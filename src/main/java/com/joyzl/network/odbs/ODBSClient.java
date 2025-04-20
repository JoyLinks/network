/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPClient;
import com.joyzl.network.odbs.MessageIndex.MessageEvenIndex;
import com.joyzl.network.odbs.MessageIndex.MessageOddIndex;

/**
 * 基于TCP Odbs连接的客户端
 * 
 * @author ZhangXi 2019年7月12日
 *
 */
public class ODBSClient extends TCPClient {

	private final ReentrantLock k = new ReentrantLock(true);
	private final MessageStream<ODBSMessage> streams = new MessageStream<>();
	private final MessageOddIndex<ODBSMessage> sends = new MessageOddIndex<>();
	private final MessageEvenIndex<ODBSMessage> pushes = new MessageEvenIndex<>();

	public ODBSClient(ODBSClientHandler<?> h, String host, int port) {
		super(h, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_CLIENT;
	}

	@Override
	public void send(Object message) {
		if (message instanceof ODBSMessage om) {
			k.lock();
			try {
				// 发送客户端请求消息
				// 每次生成新的消息标识
				om.tag(sends.add(om));
				streams.add(om, om.tag());

				if (streams.size() == 1 && sendMessage() == null) {
					sendMessage(message = streams.stream());
				} else {
					return;
				}
			} finally {
				k.unlock();
			}
		}
		super.send(message);
	}

	protected void sendNext() {
		k.lock();
		try {
			if (streams.isDone()) {
				streams.remove();
			}
			if (streams.isEmpty()) {
				return;
			}
			if (sendMessage() == null) {
				sendMessage(streams.stream());
			} else {
				return;
			}
		} finally {
			k.unlock();
		}
		super.send(sendMessage());
	}

	protected void sendRemove(int id) {
		k.lock();
		try {
			sends.remove(id);
		} finally {
			k.unlock();
		}
	}

	MessageIndex<ODBSMessage> sends() {
		return sends;
	}

	MessageIndex<ODBSMessage> pushes() {
		return pushes;
	}

	MessageStream<ODBSMessage> streams() {
		return streams;
	}
}