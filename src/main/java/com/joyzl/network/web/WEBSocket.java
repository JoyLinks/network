/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.Utility;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.Connection;
import com.joyzl.network.http.Date;
import com.joyzl.network.http.HTTPServlet;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.Origin;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.SecWebSocketAccept;
import com.joyzl.network.http.SecWebSocketKey;
import com.joyzl.network.http.SecWebSocketVersion;
import com.joyzl.network.http.Upgrade;

/**
 * WEB Socket
 * 
 * @author ZhangXi
 * @date 2023年9月4日
 */
public abstract class WEBSocket extends HTTPServlet {

	// WebSocket协议握手
	// RFC6455

	// 1 REQUEST
	// Connection: Upgrade
	// Upgrade: websocket
	// Origin: http://127.0.0.1
	// Sec-WebSocket-Key: lqy8ApLbw+oVNBkMGrpceg==
	// Sec-WebSocket-Extensions: permessage-deflate
	// Sec-WebSocket-Version: 13
	// Sec-WebSocket-Extensions:
	// Sec-WebSocket-Protocol:

	// 2 RESPONSE
	// HTTP/1.1 101 Switching Protocols
	// Upgrade: websocket
	// Connection: Upgrade
	// Sec-WebSocket-Accept: A6IfD3WS44QyuV2I/XubFkImAH8=
	// Sec-WebSocket-Version: 13

	// 浏览器差异
	// Chrome
	// connection="Upgrade"
	// FireFox
	// connection="keep-alive,Upgrade"

	@Override
	public void service(ChainChannel<Message> chain, Request request, Response response) throws Exception {
		if (Utility.equals(request.getMethod(), WEBServlet.GET, false)) {
			// Connection: Upgrade
			final String connection = request.getHeader(Connection.NAME);
			if (Utility.isEmpty(connection)) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}
			if (Utility.ends(connection, Connection.UPGRADE, false)) {
				// OK
			} else {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}

			// Upgrade: websocket
			final String upgrade = request.getHeader(Upgrade.NAME);
			if (Utility.isEmpty(upgrade)) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}
			if (Utility.equals(upgrade, Upgrade.WEBSOCKET, true)) {
				// OK
			} else {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}

			// Origin: http://127.0.0.1
			final String origin = request.getHeader(Origin.NAME);
			if (Utility.isEmpty(origin)) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}

			// Sec-WebSocket-Key: lqy8ApLbw+oVNBkMGrpceg==
			final String key = request.getHeader(SecWebSocketKey.NAME);
			if (Utility.isEmpty(key)) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}

			// Sec-WebSocket-Version: 13
			final String version = request.getHeader(SecWebSocketVersion.NAME);
			if (Utility.isEmpty(version)) {
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}
			if (Utility.equals(version, SecWebSocketVersion.VERSION, false)) {
				// OK
			} else {
				// 目前只有13版本
				response.setStatus(HTTPStatus.BAD_REQUEST);
			}

			final WEBSlave slave = (WEBSlave) chain;
			response.setStatus(HTTPStatus.SWITCHING_PROTOCOL);
			response.addHeader(new SecWebSocketAccept(key));
			// response.setHeader(HTTPHeader.SEC_WEBSOCKET_ORIGIN,"WebSocket");
			response.addHeader(SecWebSocketVersion.NAME, version);
			response.addHeader(new Date());

			// 升级链路类型为WEBSOCKET
			slave.upgrade();
			// WebSocket必须绑定Servlet实例
			// 因为握手之后的收发不在具有URI无法通过URI获取Servlet
			slave.bind(this);
			chain.send(response);
		}
	}

	/** WebSocket */
	protected void received(WEBSlave chain, WebSocketMessage message) throws Exception {
	}
}
