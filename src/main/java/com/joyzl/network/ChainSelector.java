/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import com.joyzl.network.chain.ChainChannel;

/**
 * 基于NIO.1 Selector选择器的链路超类
 * <p>
 * 为基于Selector的链路提供全局的选择器实现和执行
 *
 * @author ZhangXi 2019年8月15日
 *
 */
public final class ChainSelector {

	// NIO.1 选择器线程
	private static Thread THREAD_READS_SELECTOR;
	// NIO.1 选择器线程
	private static Thread THREAD_WRITES_SELECTOR;
	// NIO.1 选择器线程
	private static Thread THREAD_CONNECTS_SELECTOR;

	// 为NIO.1提供统一的选择器(读)
	private final static Selector SELECTOR_READS;
	// 为NIO.1提供统一的选择器(写)
	private final static Selector SELECTOR_WRITES;
	// 为NIO.1提供统一的选择器(连)
	private final static Selector SELECTOR_CONNECTS;

	static {
		// NIO.1仅用于UDP链路
		// UDP 的连接和绑定通常是立即完成，但是为了异步获得handler().connect()通知为此建立连接操作的选择器
		// 原来是在创建链路的构造方法中激活连接事件，这会造成handler().connect()无法访问链路实例
		try {
			SELECTOR_READS = Selector.open();
			SELECTOR_WRITES = Selector.open();
			SELECTOR_CONNECTS = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static final void initialize() {
		// NIO.1 READ SELECTOR
		final Runnable reads = new Runnable() {
			@Override
			public void run() {
				Iterator<SelectionKey> selection_keys;
				SelectionKey selection_key;
				ChainChannel chain;

				try {
					while (SELECTOR_READS.isOpen()) {
						while (SELECTOR_READS.select() > 0) {
							selection_keys = SELECTOR_READS.selectedKeys().iterator();
							while (selection_keys.hasNext()) {
								selection_key = selection_keys.next();
								selection_keys.remove();

								if (selection_key.isReadable()) {
									chain = (ChainChannel) selection_key.attachment();
									// 通知链路接收并读取数据
									chain.receive();
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
		};
		// NIO.1 WRITE SELECTOR
		final Runnable writes = new Runnable() {
			@Override
			public void run() {
				Iterator<SelectionKey> selection_keys;
				SelectionKey selection_key;
				ChainChannel chain;

				try {
					while (SELECTOR_WRITES.isOpen()) {
						while (SELECTOR_WRITES.select() > 0) {
							selection_keys = SELECTOR_WRITES.selectedKeys().iterator();
							while (selection_keys.hasNext()) {
								selection_key = selection_keys.next();
								selection_keys.remove();

								if (selection_key.isWritable()) {
									chain = (ChainChannel) selection_key.attachment();
									// 通知链路数据发送完成
									chain.send(null);
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
		};
		// NIO.1 CONNECT SELECTOR
		final Runnable connects = new Runnable() {
			@Override
			public void run() {
				Iterator<SelectionKey> selection_keys;
				SelectionKey selection_key;
				ChainChannel chain;

				try {
					while (SELECTOR_CONNECTS.isOpen()) {
						while (SELECTOR_CONNECTS.select() > 0) {
							selection_keys = SELECTOR_CONNECTS.selectedKeys().iterator();
							while (selection_keys.hasNext()) {
								selection_key = selection_keys.next();
								selection_keys.remove();

								if (selection_key.isConnectable()) {
									chain = (ChainChannel) selection_key.attachment();
									// 通知链路连接完成
									chain.active();
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
		};

		// 初始化NIO.1选择器主线程

		THREAD_READS_SELECTOR = new Thread(reads, "nio.1-reads");
		THREAD_READS_SELECTOR.start();

		THREAD_WRITES_SELECTOR = new Thread(writes, "nio.1-writes");
		THREAD_WRITES_SELECTOR.start();

		THREAD_CONNECTS_SELECTOR = new Thread(connects, "nio.1-connects");
		THREAD_CONNECTS_SELECTOR.start();
	}

	public static void register(ChainChannel chain, SelectableChannel channel, int key) throws IOException {
		switch (key) {
			case SelectionKey.OP_ACCEPT:
			case SelectionKey.OP_CONNECT:
				channel.register(SELECTOR_CONNECTS, key, chain);
				SELECTOR_CONNECTS.wakeup();
				break;
			case SelectionKey.OP_WRITE:
				channel.register(SELECTOR_WRITES, key, chain);
				SELECTOR_WRITES.wakeup();
				break;
			case SelectionKey.OP_READ:
				channel.register(SELECTOR_READS, key, chain);
				SELECTOR_READS.wakeup();
				break;
		}
	}

	public static void unRegister(ChainChannel chain, SelectableChannel channel) {
		SelectionKey key = channel.keyFor(SELECTOR_CONNECTS);
		if (key != null) {
			SELECTOR_CONNECTS.wakeup();
			key.cancel();
		}
		key = channel.keyFor(SELECTOR_WRITES);
		if (key != null) {
			SELECTOR_WRITES.wakeup();
			key.cancel();
		}
		key = channel.keyFor(SELECTOR_READS);
		if (key != null) {
			SELECTOR_READS.wakeup();
			key.cancel();
		}
	}

	public static final void shutdown() {
		// 关闭读选择器
		if (THREAD_READS_SELECTOR != null) {
			if (ChainSelector.SELECTOR_READS != null && ChainSelector.SELECTOR_READS.isOpen()) {
				try {
					ChainSelector.SELECTOR_READS.wakeup();
					ChainSelector.SELECTOR_READS.close();
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
		// 关闭写选择器
		if (THREAD_WRITES_SELECTOR != null) {
			if (ChainSelector.SELECTOR_WRITES != null && ChainSelector.SELECTOR_WRITES.isOpen()) {
				try {
					ChainSelector.SELECTOR_WRITES.wakeup();
					ChainSelector.SELECTOR_WRITES.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				THREAD_WRITES_SELECTOR.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				THREAD_WRITES_SELECTOR = null;
			}
		}
		// 关闭连接选择器
		if (THREAD_CONNECTS_SELECTOR != null) {
			if (ChainSelector.SELECTOR_CONNECTS != null && ChainSelector.SELECTOR_CONNECTS.isOpen()) {
				try {
					ChainSelector.SELECTOR_CONNECTS.wakeup();
					ChainSelector.SELECTOR_CONNECTS.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				THREAD_CONNECTS_SELECTOR.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				THREAD_CONNECTS_SELECTOR = null;
			}
		}
	}
}