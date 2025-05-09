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
public class SlaveSendHandler implements CompletionHandler<Integer, Slave> {

	final static SlaveSendHandler INSTANCE = new SlaveSendHandler();

	@Override
	public void completed(Integer result, Slave chain) {
		chain.sent(result);
	}

	@Override
	public void failed(Throwable e, Slave chain) {
		chain.sent(e);
	}
}