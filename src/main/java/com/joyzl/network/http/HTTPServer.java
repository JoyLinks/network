/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPServer;

/**
 * HTTP Server
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class HTTPServer extends TCPServer {

	public HTTPServer(HTTPServerHandler handler, String host, int port) throws IOException {
		super(handler, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_SERVER;
	}

	@Override
	protected HTTPSlave create(AsynchronousSocketChannel socket_channel) throws IOException {
		return new HTTPSlave(this, socket_channel);
	}
}