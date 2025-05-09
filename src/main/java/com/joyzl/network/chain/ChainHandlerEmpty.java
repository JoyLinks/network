/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
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
public final class ChainHandlerEmpty implements ChainHandler {

	@Override
	public void connected(ChainChannel chain) throws Exception {
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		return null;
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		return null;
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
	}
}