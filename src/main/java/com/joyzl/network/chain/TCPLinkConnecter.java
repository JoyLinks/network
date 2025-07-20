/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

import java.nio.channels.CompletionHandler;

/**
 * CONNECT CompletionHandler
 * 
 * @author ZhangXi
 * @date 2023年8月25日
 */
public class TCPLinkConnecter implements CompletionHandler<Void, TCPLink> {

	final static TCPLinkConnecter INSTANCE = new TCPLinkConnecter();

	@Override
	public void completed(Void result, TCPLink chain) {
		chain.connected();
	}

	@Override
	public void failed(Throwable e, TCPLink chain) {
		// completed()方法抛出的异常不会到达此方法
		// 连接不成功通道处于关闭状态isOpen()为false
		chain.connected(e);
	}
}