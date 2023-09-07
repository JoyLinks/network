/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 空处理对象
 * 
 * @author ZhangXi
 * @date 2021年4月6日
 */
public final class ChainHandlerEmpty<M> implements ChainHandler<M> {

	@Override
	public void connected(ChainChannel<M> chain) throws Exception {
	}

	@Override
	public M decode(ChainChannel<M> chain, DataBuffer reader) throws Exception {
		return null;
	}

	@Override
	public void received(ChainChannel<M> chain, M source) throws Exception {
	}

	@Override
	public void beat(ChainChannel<M> chain) throws Exception {
	}

	@Override
	public DataBuffer encode(ChainChannel<M> chain, M source) throws Exception {
		return null;
	}

	@Override
	public void sent(ChainChannel<M> chain, M source) throws Exception {
	}

	@Override
	public void disconnected(ChainChannel<M> chain) throws Exception {
	}

	@Override
	public void error(ChainChannel<M> chain, Throwable e) {
	}
}