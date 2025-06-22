/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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