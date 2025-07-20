/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPServer;

/**
 * HTTP Server
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class HTTPServer extends TCPServer {

	public HTTPServer(ChainHandler handler, String host, int port, int backlog) throws IOException {
		super(handler, host, port, backlog);
	}

	public HTTPServer(ChainHandler handler, String host, int port) throws IOException {
		super(handler, host, port);
	}

	public HTTPServer(ChainHandler handler, int port) throws IOException {
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