/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPSlave;
import com.joyzl.network.odbs.MessageIndex.MessageEvenIndex;
import com.joyzl.network.odbs.MessageIndex.MessageOddIndex;

/**
 * TCP从连接，由TCPServer创建
 *
 * @author ZhangXi 2019年7月9日
 *
 */
public class ODBSSlave extends TCPSlave {

	private final ReentrantLock k = new ReentrantLock(false);
	private final MessageStream<ODBSMessage> streams = new MessageStream<>();
	private final MessageOddIndex<ODBSMessage> receives = new MessageOddIndex<>();
	private final MessageEvenIndex<ODBSMessage> pushes = new MessageEvenIndex<>();

	public ODBSSlave(ODBSServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_SLAVE;
	}

	@Override
	public void send(Object message) {
		if (message instanceof ODBSMessage om) {
			k.lock();
			try {
				if (om.tag() > 0) {
					if (om.theChain() == this) {
						// 响应客户端请求消息，延用标识
						streams.add(om, om.tag());
					} else {
						// 客户端响应被转发，新建标识
						// 客户端响应被广播，新建标识
						streams.add(om, pushes.add(om));
					}
				} else {
					// 发送服务端推送消息，新建标识
					// 发送服务端广播消息，新建标识
					streams.add(om, pushes.add(om));
				}

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
				if (streams.id() % 2 == 0) {
					pushes.remove(streams.id());
				}
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

	MessageIndex<ODBSMessage> receives() {
		return receives;
	}

	MessageIndex<ODBSMessage> pushes() {
		return pushes;
	}

	MessageStream<ODBSMessage> streams() {
		return streams;
	}
}