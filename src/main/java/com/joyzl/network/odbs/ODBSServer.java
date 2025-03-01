/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
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