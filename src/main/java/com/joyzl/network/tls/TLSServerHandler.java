package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * TLSServerHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
public class TLSServerHandler extends RecordHandler {

	private final ChainHandler<Object> handler;

	public TLSServerHandler(ChainHandler<Object> handler) {
		this.handler = handler;
	}

	@Override
	protected ChainHandler<Object> handler() {
		return handler;
	}

	@Override
	public void connected(ChainChannel<Object> chain) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sent(ChainChannel<Object> chain, Object message) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean handshaked() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected DataBuffer decrypt(DataBuffer buffer, int length) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DataBuffer encrypt(DataBuffer buffer) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Handshake decode(DataBuffer buffer) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void encode(Handshake handshake, DataBuffer buffer) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void received(ChainChannel<Object> chain, Handshake handshake) throws Exception {
		// TODO Auto-generated method stub

	}
}