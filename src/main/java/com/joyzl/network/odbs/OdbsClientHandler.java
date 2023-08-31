/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.odbs.ODBS;

/**
 * ODBS客户端处理类基础实现
 * 
 * @author ZhangXi
 * @date 2020年12月13日
 */
public abstract class OdbsClientHandler<M extends ODBSMessage> extends OdbsClientCoder<M> {

	public OdbsClientHandler(ODBS o) {
		super(o);
	}

	@Override
	public M take(ChainChannel<M> chain, int tag) {
		return ((TCPOdbsClient<M>) chain).take(tag);
	}

	@Override
	public void connected(ChainChannel<M> chain) throws Exception {
		chain.receive();
	}

	@Override
	public void received(ChainChannel<M> chain, M message) throws Exception {
		chain.receive();
	}

	@Override
	public void sent(ChainChannel<M> chain, M message) throws Exception {
		((TCPOdbsClient<M>) chain).sent(message);
	}
}