/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.nio.channels.CompletionHandler;

/**
 * READ CompletionHandler
 * 
 * @author ZhangXi
 * @date 2023年8月25日
 */
public class TCPSlaveReceiver implements CompletionHandler<Integer, TCPSlave> {

	final static TCPSlaveReceiver INSTANCE = new TCPSlaveReceiver();

	@Override
	public void completed(Integer result, TCPSlave chain) {
		chain.received(result);
	}

	@Override
	public void failed(Throwable e, TCPSlave chain) {
		chain.received(e);
	}
}