/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.odbs;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.Slave;
import com.joyzl.network.chain.TCPServer;

/**
 * TCP服务端，监听指定端口接收连接（TCPSlave）
 * 
 * @author ZhangXi 2019年7月12日
 *
 */
public class ODBSServer extends TCPServer {

	public ODBSServer(ODBSServerHandler<?> handler, String host, int port) throws IOException {
		super(handler, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_SERVER;
	}

	@Override
	protected Slave create(AsynchronousSocketChannel socket_channel) throws Exception {
		return new ODBSSlave(this, socket_channel);
	}
}