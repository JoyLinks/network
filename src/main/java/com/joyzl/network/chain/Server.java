/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链路服务端
 * 
 * @param <C> 从链路类型
 * @param <M> 消息类型
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public abstract class Server extends ChainChannel {

	/** 从链路 */
	private final ConcurrentHashMap<String, Slave> slaves = new ConcurrentHashMap<>();
	/** 消息处理对象 */
	private final ChainHandler handler;

	public Server(ChainHandler h, String k) {
		super(k);
		handler = h;
	}

	protected abstract void accepted(AsynchronousSocketChannel channel);

	protected abstract void accepted(Throwable e);

	public final ChainHandler handler() {
		return handler;
	}

	protected void addSlave(Slave chain) {
		chain = slaves.put(chain.key(), chain);
		if (chain != null) {
			chain.close();
		}
	}

	protected void offSlave(Slave chain) {
		if (slaves.remove(chain.key(), chain)) {
		} else {
			if (slaves.containsValue(chain)) {
				if (slaves.values().remove(chain)) {
					throw new RuntimeException("移除链路失败:" + chain);
				}
			}
		}
	}

	public Slave getSlave(String key) {
		return slaves.get(key);
	}

	public Collection<Slave> getSlaves() {
		return slaves.values();
	}

	@Override
	public void close() {
		// 关闭并移除所有从链路
		final Iterator<Entry<String, Slave>> iterator = slaves.entrySet().iterator();
		while (iterator.hasNext()) {
			iterator.next().getValue().close();
			iterator.remove();
		}
	}
}