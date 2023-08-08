/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.odbs.ODBSBinary;

/**
 * ODBS客户端处理类基础实现
 * 
 * @author ZhangXi
 * @date 2020年12月13日
 */
public abstract class OdbsClientHandler<M extends ODBSMessage> extends OdbsClientCoder<M> {

	public OdbsClientHandler(ODBSBinary ob) {
		super(ob);
	}

	@Override
	public void connected(ChainChannel<M> chain) throws Exception {
		chain.receive();
	}

	@Override
	public void received(ChainChannel<M> chain, M source) throws Exception {
		chain.receive();
	}

	@Override
	public void sent(ChainChannel<M> chain, M source) throws Exception {

	}
}