/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPSlave;
import com.joyzl.network.http.HTTPServlet;
import com.joyzl.network.http.Message;

/**
 * HTTP 服务端连接
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBSlave extends TCPSlave<Message> {

	private boolean websocket = false;
	private HTTPServlet servlet;

	public WEBSlave(WEBServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	@Override
	public ChainType type() {
		if (websocket) {
			return ChainType.TCP_HTTP_SLAVE_WEB_SOCKET;
		}
		return ChainType.TCP_HTTP_SLAVE;
	}

	public void upgrade() {
		// 当前仅WEBSocket情形
		websocket = true;
	}

	public HTTPServlet servlet() {
		return servlet;
	}

	public void bind(HTTPServlet value) {
		servlet = value;
	}

	private WEBRequest request;

	protected WEBRequest getRequest() {
		return request;
	}

	protected void setRequest(WEBRequest value) {
		request = value;
	}

}
