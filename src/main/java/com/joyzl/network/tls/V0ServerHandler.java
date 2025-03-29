package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * TLSServerHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
public class V0ServerHandler extends ServerHandler implements ChainHandler {

	public V0ServerHandler(ChainHandler handler, TLSParameters parameters) {
		super(handler, parameters);
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.setContext(new TLSContext());
		chain.receive();
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer reader) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		// TODO Auto-generated method stub

	}

	private class TLSContext extends TLSSlaveContext {
		final Signaturer signaturer;
		final V0CipherSuiter cipher;
		final V0SecretCache secret;

		public TLSContext() {
			signaturer = new Signaturer();
			cipher = new V0CipherSuiter();
			secret = new V0SecretCache();
		}
	}
}