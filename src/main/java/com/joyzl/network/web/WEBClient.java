/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.chain.TCPShort;
import com.joyzl.network.http.Message;

/**
 * WEB HTTP 客户端
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public final class WEBClient extends TCPShort<Message> {

	public WEBClient(WEBClientHandler handler, String host, int port) {
		super(handler, host, port);
	}

	private WEBResponse response;

	protected WEBResponse getResponse() {
		return response;
	}

	protected void setResponse(WEBResponse value) {
		response = value;
	}
}