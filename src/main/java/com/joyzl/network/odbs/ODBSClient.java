/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.odbs;

import java.util.concurrent.locks.ReentrantLock;

import com.joyzl.network.IndexMap;
import com.joyzl.network.chain.TCPClient;

/**
 * 基于TCP Odbs连接的客户端
 * 
 * @author ZhangXi 2019年7月12日
 *
 */
public class ODBSClient extends TCPClient {

	private final ReentrantLock k = new ReentrantLock(true);
	private final ODBSStream<ODBSMessage> sends = new ODBSStream<>();
	private final IndexMap<ODBSMessage> receives = new IndexMap<>();
	private int id = 1;

	public ODBSClient(ODBSClientHandler<?> h, String host, int port) {
		super(h, host, port);
	}

	/** 生成自增奇数编号，溢出后归1 */
	private int oddId() {
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
				// 发送客户端请求消息
				// 每次生成新的消息标识
				om.tag(oddId());
				receives.put(om.tag(), om);

				sends.add(om, om.tag());
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

	protected void sendRemove(int id) {
		k.lock();
		try {
			receives.remove(id);
		} finally {
			k.unlock();
		}
	}

	IndexMap<ODBSMessage> receives() {
		return receives;
	}

	ODBSStream<ODBSMessage> sends() {
		return sends;
	}
}