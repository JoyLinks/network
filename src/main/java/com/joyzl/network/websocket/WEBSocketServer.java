/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.websocket;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.web.WEBServer;

/**
 * WEB HTTP服务端
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBSocketServer extends WEBServer {

	public WEBSocketServer(WEBSocketServerHandler handler, String host, int port) throws IOException {
		super(handler, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_SERVER;
	}

	@Override
	protected WEBSocketSlave create(AsynchronousSocketChannel socket_channel) throws IOException {
		return new WEBSocketSlave(this, socket_channel);
	}
}