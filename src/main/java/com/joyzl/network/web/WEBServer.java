/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPServer;
import com.joyzl.network.http.Message;

/**
 * WEB HTTP服务端
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBServer extends TCPServer<Message> {

	public WEBServer(WEBServerHandler handler, String host, int port) throws IOException {
		super(handler, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_SERVER;
	}

	@Override
	protected WEBSlave accepted(AsynchronousSocketChannel socket_channel) throws IOException {
		return new WEBSlave(this, socket_channel);
	}
}