/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPSlave;
import com.joyzl.network.http.Message;

/**
 * HTTP 服务端连接
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBSlave extends TCPSlave<Message> {

	public WEBSlave(WEBServer server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_SLAVE;
	}

	// 服务端提供请求消息暂存以支持消息解码
	// 在网络传输中可能需要多次接收数据才能完成解码
	private WEBRequest request;

	protected WEBRequest getRequest() {
		if (request == null) {
			return request = new WEBRequest();
		}
		return request;
	}

	protected void setRequest(WEBRequest value) {
		request = value;
	}
}
