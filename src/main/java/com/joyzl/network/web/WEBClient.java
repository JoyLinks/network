/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPLink;
import com.joyzl.network.http.Message;

/**
 * WEB HTTP 客户端
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBClient extends TCPLink<Message> {

	public WEBClient(WEBClientHandler handler, String host, int port) {
		super(handler, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_CLIENT;
	}

	// 客户端提供响应消息暂存以支持消息解码
	// 在网络传输中可能需要多次接收数据才能完成解码
	private WEBResponse response;

	protected WEBResponse getResponse() {
		if (response == null) {
			return response = new WEBResponse();
		}
		return response;
	}

	protected void setResponse(WEBResponse value) {
		response = value;
	}
}