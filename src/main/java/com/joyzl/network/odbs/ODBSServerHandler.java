/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.odbs.ODBS;

/**
 * @author ZhangXi
 * @date 2020年12月13日
 */
public abstract class ODBSServerHandler<M extends ODBSMessage> extends ODBSServerCoder<M> {

	public ODBSServerHandler(ODBS o) {
		super(o);
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
		((ODBSSlave<M>) chain).sent(message);
	}

	@Override
	public void error(ChainChannel<M> chain, Throwable e) {
		chain.close();
	}
}