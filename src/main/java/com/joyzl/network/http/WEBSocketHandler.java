package com.joyzl.network.http;

import com.joyzl.network.chain.ChainType;

/**
 * WEB Socket Handler
 * 
 * @author ZhangXi 2024年12月12日
 */
public interface WEBSocketHandler {

	default ChainType type() {
		return ChainType.TCP_HTTP_SLAVE_WEB_SOCKET;
	};

	void connected(HTTPSlave slave) throws Exception;

	void received(HTTPSlave slave, WEBSocketMessage message) throws Exception;

	void sent(HTTPSlave slave, WEBSocketMessage message) throws Exception;

	void disconnected(HTTPSlave slave) throws Exception;
}