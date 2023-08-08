/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 *
 */
package com.joyzl.network.chain;

import java.util.ArrayDeque;
import java.util.Deque;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 链路输入输出支持
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月20日
 *
 */
public abstract class ChainChannel<M> extends Chain {

	// 消息队列大小
	private final static int DEQUE_SIZE = 128;
	private final Deque<M> ObjectESSAGES_DEQUE = new ArrayDeque<>(DEQUE_SIZE);

	public ChainChannel(String k) {
		super(k);
	}

	/**
	 * 消息队列
	 */
	protected final Deque<M> messages() {
		return ObjectESSAGES_DEQUE;
	}

	/**
	 * 读取接收的数据流
	 */
	protected abstract void read(DataBuffer reader) throws Exception;

	/**
	 * 通过链路输出数据流
	 */
	protected abstract void write(Object message) throws Exception;

}
