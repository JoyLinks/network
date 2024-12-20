package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

public class TLSServerHandler implements ChainHandler<Record> {

	@Override
	public void connected(ChainChannel<Record> chain) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Record decode(ChainChannel<Record> chain, DataBuffer reader) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void received(ChainChannel<Record> chain, Record message) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public DataBuffer encode(ChainChannel<Record> chain, Record message) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sent(ChainChannel<Record> chain, Record message) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnected(ChainChannel<Record> chain) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(ChainChannel<Record> chain, Throwable e) {
		// TODO Auto-generated method stub

	}
}