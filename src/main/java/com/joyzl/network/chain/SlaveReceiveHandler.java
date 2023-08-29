package com.joyzl.network.chain;

import java.nio.channels.CompletionHandler;

/**
 * READ CompletionHandler
 * 
 * @author ZhangXi
 * @date 2023年8月25日
 */
public class SlaveReceiveHandler implements CompletionHandler<Integer, Slave<?>> {

	final static SlaveReceiveHandler INSTANCE = new SlaveReceiveHandler();

	@Override
	public void completed(Integer result, Slave<?> chain) {
		chain.received(result);
	}

	@Override
	public void failed(Throwable e, Slave<?> chain) {
		chain.received(e);
	}
}