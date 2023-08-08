/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPClient;

/**
 * 基于TCP Odbs连接的客户端，提供心跳，中断重连等机制
 * 
 * @author simon(ZhangXi TEL:13883833982) 2019年7月12日
 *
 */
public class TCPOdbsClient<M extends ODBSMessage> extends TCPClient<M> {

	public TCPOdbsClient(OdbsClientHandler<M> h, String host, int port) {
		super(h, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_ODBS_CLIENT;
	}
}