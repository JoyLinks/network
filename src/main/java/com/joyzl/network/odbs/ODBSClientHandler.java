/*-
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
public abstract class ODBSClientHandler<M extends ODBSMessage> extends ODBSClientCoder<M> {

	public ODBSClientHandler(ODBS o) {
		super(o);
	}

	@Override
	public M take(ChainChannel<M> chain, int tag) {
		return ((ODBSClient<M>) chain).take(tag);
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
		((ODBSClient<M>) chain).sent(message);
	}

	public abstract void beat(ChainChannel<M> chain) throws Exception;
}