/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.web;

import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.WEBSocketMessage;

/**
 * 提供默认实现的 WEBSocketHandler
 * 
 * @author ZhangXi 2024年12月12日
 */
public abstract class WEBSocketHandler implements com.joyzl.network.http.WEBSocketHandler {

	public void received(HTTPSlave chain, WEBSocketMessage message) throws Exception {
		if (message.getType() == WEBSocketMessage.CLOSE) {
			chain.close();
		} else if (message.getType() == WEBSocketMessage.PING) {
			message.setType(WEBSocketMessage.PONG);
			chain.send(message);
		} else if (message.getType() == WEBSocketMessage.TEXT) {

		} else if (message.getType() == WEBSocketMessage.BINARY) {

		} else {

		}
	}
}