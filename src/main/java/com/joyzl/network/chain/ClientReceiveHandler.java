package com.joyzl.network.chain;

import java.nio.channels.CompletionHandler;

/**
 * READ CompletionHandler
 * 
 * @author ZhangXi
 * @date 2023年8月25日
 */
public class ClientReceiveHandler implements CompletionHandler<Integer, Client<?>> {

	final static ClientReceiveHandler INSTANCE = new ClientReceiveHandler();

	@Override
	public void completed(Integer result, Client<?> chain) {
		chain.received(result);
	}

	@Override
	public void failed(Throwable e, Client<?> chain) {
		chain.received(e);
	}
}