/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * 基于NIO.1 Selector选择器
 * <p>
 * 为基于Selector的链路提供全局的选择器实现和执行
 *
 * @author ZhangXi 2019年8月15日
 *
 */
public final class UDPClientReceiver implements Runnable {

	// NIO.1 选择器线程
	private static Thread THREAD_READS_SELECTOR;
	// 为NIO.1提供统一的选择器(读)
	private final static Selector SELECTOR_READS;

	static {
		// NIO.1仅用于UDP链路
		// UDP 的连接和绑定通常是立即完成，但是为了异步获得handler().connect()通知为此建立连接操作的选择器
		// 原来是在创建链路的构造方法中激活连接事件，这会造成handler().connect()无法访问链路实例
		try {
			SELECTOR_READS = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final void initialize() {
		// 初始化NIO.1选择器线程
		THREAD_READS_SELECTOR = new Thread(new UDPClientReceiver(), "nio.1-reads");
		THREAD_READS_SELECTOR.start();
	}

	public static void register(UDPClient client, SelectableChannel channel) throws IOException {
		channel.register(SELECTOR_READS, SelectionKey.OP_READ, client);
		SELECTOR_READS.wakeup();
	}

	public static void unRegister(UDPClient client, SelectableChannel channel) {
		final SelectionKey key = channel.keyFor(SELECTOR_READS);
		if (key != null) {
			SELECTOR_READS.wakeup();
			key.cancel();
		}
	}

	public static final void shutdown() {
		// 关闭读选择器
		if (THREAD_READS_SELECTOR != null) {
			if (UDPClientReceiver.SELECTOR_READS != null && UDPClientReceiver.SELECTOR_READS.isOpen()) {
				try {
					UDPClientReceiver.SELECTOR_READS.wakeup();
					UDPClientReceiver.SELECTOR_READS.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				THREAD_READS_SELECTOR.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				THREAD_READS_SELECTOR = null;
			}
		}
	}

	private UDPClientReceiver() {
	}

	@Override
	public void run() {
		Iterator<SelectionKey> selection_keys;
		SelectionKey selection_key;
		UDPClient client;

		try {
			while (SELECTOR_READS.isOpen()) {
				while (SELECTOR_READS.select() > 0) {
					selection_keys = SELECTOR_READS.selectedKeys().iterator();
					while (selection_keys.hasNext()) {
						selection_key = selection_keys.next();
						selection_keys.remove();

						if (selection_key.isReadable()) {
							client = (UDPClient) selection_key.attachment();
							// 通知链路接收并读取数据
							client.received();
						} else {
							// 忽略
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}