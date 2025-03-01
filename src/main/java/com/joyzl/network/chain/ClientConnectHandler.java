/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.chain;

import java.nio.channels.CompletionHandler;

/**
 * CONNECT CompletionHandler
 * 
 * @author ZhangXi
 * @date 2023年8月25日
 */
public class ClientConnectHandler implements CompletionHandler<Void, Client> {

	final static ClientConnectHandler INSTANCE = new ClientConnectHandler();

	@Override
	public void completed(Void result, Client chain) {
		chain.connected();
	}

	@Override
	public void failed(Throwable e, Client chain) {
		// completed()方法抛出的异常不会到达此方法
		// 连接不成功通道处于关闭状态isOpen()为false
		chain.connected(e);
	}
}