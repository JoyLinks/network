/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainGenericsHandler;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPServer;

/**
 * HTTP Server
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class HTTPServer extends TCPServer {

	public HTTPServer(ChainGenericsHandler<HTTPSlave, Message> handler, String host, int port, int backlog) throws IOException {
		super(handler, host, port, backlog);
	}

	public HTTPServer(ChainGenericsHandler<HTTPSlave, Message> handler, String host, int port) throws IOException {
		super(handler, host, port);
	}

	public HTTPServer(ChainGenericsHandler<HTTPSlave, Message> handler, int port) throws IOException {
		super(handler, null, port);
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