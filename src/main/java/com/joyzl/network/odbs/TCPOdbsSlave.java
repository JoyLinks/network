/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 *
 */
package com.joyzl.network.odbs;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPSlave;

/**
 * TCP从连接，由TCPServer创建
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月9日
 *
 */
public class TCPOdbsSlave<M extends ODBSMessage> extends TCPSlave<M> {

	public TCPOdbsSlave(TCPOdbsServer<M> server, AsynchronousSocketChannel channel) throws IOException {
		super(server, channel);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_SLAVE;
	}
}