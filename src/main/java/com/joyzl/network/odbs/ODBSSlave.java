/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.IndexMap;
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
	private final MessageStream<ODBSMessage> sends = new MessageStream<>();
	private final IndexMap<ODBSMessage> receives = new IndexMap<>();
	private int id = 2;

	public ODBSSlave(ODBSServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	/** 生成自增偶数编号，溢出后归2 */
	private int evenId() {
		int i = id;
		id += 2;
		if (id <= 0) {
			id = 2;
		}
		return i;
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
					if (om.chain() == this) {
						// 响应客户端请求消息，延用标识
						sends.add(om, om.tag());
					} else {
						// 客户端响应被转发，新建标识
						// 客户端响应被广播，新建标识
						sends.add(om, evenId());
					}
				} else {
					// 发送服务端推送消息，新建标识
					// 发送服务端广播消息，新建标识
					sends.add(om, evenId());
				}

				if (sends.size() == 1 && sendMessage() == null) {
					sendMessage(message = sends.stream());
				} else {
					return;
				}
			} finally {
				k.unlock();
			}
			super.send(message);
		} else {
			throw new IllegalArgumentException("无效的消息对象类型");
		}
	}

	protected void sendNext() {
		k.lock();
		try {
			if (sends.isDone()) {
				sends.remove();
			}
			if (sends.isEmpty()) {
				return;
			}
			if (sendMessage() == null) {
				sendMessage(sends.stream());
			} else {
				return;
			}
		} finally {
			k.unlock();
		}
		super.send(sendMessage());
	}

	IndexMap<ODBSMessage> receives() {
		return receives;
	}

	MessageStream<ODBSMessage> sends() {
		return sends;
	}
}