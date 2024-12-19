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