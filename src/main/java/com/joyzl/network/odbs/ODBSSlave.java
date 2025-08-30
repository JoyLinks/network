/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.odbs;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.IndexMap;
import com.joyzl.network.chain.TCPSlave;

/**
 * TCP从连接，由TCPServer创建
 *
 * @author ZhangXi 2019年7月9日
 *
 */
public class ODBSSlave extends TCPSlave {

	private final ReentrantLock k = new ReentrantLock(true);
	private final ODBSStream<ODBSMessage> sends = new ODBSStream<>();
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

	protected int sendId() {
		return sends.id();
	}

	protected void sendDone() {
		sends.done();
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

	protected void sendClear() throws IOException {
		k.lock();
		try {
			sends.clear();
		} finally {
			k.unlock();
		}
	}

	protected ODBSMessage receiveGet(int id) {
		k.lock();
		try {
			return receives.get(id);
		} finally {
			k.unlock();
		}
	}

	protected void receivePut(int id, ODBSMessage m) {
		k.lock();
		try {
			receives.put(id, m);
		} finally {
			k.unlock();
		}
	}

	protected void receiveRemove(int id) {
		k.lock();
		try {
			receives.remove(id);
		} finally {
			k.unlock();
		}
	}

	protected void receiveClear() {
		k.lock();
		try {
			receives.clear();
		} finally {
			k.unlock();
		}
	}

	// IndexMap<ODBSMessage> receives() {
	// return receives;
	// }

	// ODBSStream<ODBSMessage> sends() {
	// return sends;
	// }
}