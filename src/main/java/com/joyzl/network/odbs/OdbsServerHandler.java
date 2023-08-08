/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.odbs.ODBSBinary;

/**
 * @author ZhangXi
 * @date 2020年12月13日
 */
public abstract class OdbsServerHandler<M extends ODBSMessage> extends OdbsServerCoder<M> {

	public OdbsServerHandler(ODBSBinary ob) {
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
		chain.receive();
	}

	@Override
	public void error(ChainChannel<M> chain, Throwable e) {
		chain.close();
	}
}