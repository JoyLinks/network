/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.nio.channels.CompletionHandler;

/**
 * WRITE CompletionHandler
 * 
 * @author ZhangXi
 * @date 2023年8月25日
 */
public class ClientSendHandler implements CompletionHandler<Integer, Client> {

	final static ClientSendHandler INSTANCE = new ClientSendHandler();

	@Override
	public void completed(Integer result, Client chain) {
		chain.sent(result);
	}

	@Override
	public void failed(Throwable e, Client chain) {
		chain.sent(e);
	}
}