/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.Utility;
import com.joyzl.network.http.Connection;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Origin;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.SecWebSocketAccept;
import com.joyzl.network.http.Upgrade;
import com.joyzl.network.http.WEBSocketHandler;

/**
 * WEB Socket handshake<br>
 * 此处理类用于完成 WEB Socket 升级握手过程，握手成功之后创建 WEBSocketHandler 实例并绑定到当前链路；
 * WEBSocketHandler 实例将接收或发送 WEB Socket 消息，实例通常需要持有链路并执行发送消息排队。
 * 
 * @author ZhangXi
 * @date 2023年9月4日
 */
public abstract class WEBSocket extends Servlet {

	/** RFC6455 */
	public final static String VERSION = "13";
	public final static String WEBSOCKET = "websocket";

	@Override
	public void service(HTTPSlave chain, Request request, Response response) throws Exception {
		if (upgrade(request, response)) {
			// 升级链路类型为WEBSOCKET
			response.setContent(create(chain));
			response.needUpgrade();
		}
	}

	/**
	 * 升级链路为 WebSocket
	 */
	protected boolean upgrade(Request request, Response response) {
		// RFC6455 WebSocket 协议握手
		// HTTP/2 明确禁止使用此机制；这个机制只属于 HTTP/1.1

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

		// Connection: Upgrade
		String value = request.getHeader(Connection.NAME);
		if (Utility.isEmpty(value)) {
			response.setStatus(HTTPStatus.UPGRADE_REQUIRED);
			return false;
		}
		if (!value.contains(HTTP.Upgrade)) {
			// 浏览器之间存在差异
			// Chrome connection: Upgrade
			// FireFox connection: keep-alive,Upgrade
			response.setStatus(HTTPStatus.UPGRADE_REQUIRED);
			return false;
		}

		// Upgrade: websocket
		value = request.getHeader(Upgrade.NAME);
		if (!Utility.same(value, WEBSOCKET)) {
			response.setStatus(HTTPStatus.UPGRADE_REQUIRED);
			return false;
		}

		// Origin: http://127.0.0.1
		value = request.getHeader(Origin.NAME);
		if (Utility.isEmpty(value)) {
			response.setStatus(HTTPStatus.UPGRADE_REQUIRED);
			return false;
		}
		if (!allowOrigin(value)) {
			response.setStatus(HTTPStatus.FORBIDDEN);
			return false;
		}

		// Sec-WebSocket-Version: 13
		value = request.getHeader(HTTP.Sec_WebSocket_Version);
		if (!Utility.equal(value, VERSION)) {
			// 目前仅13版本，未支持其它过渡版本
			response.addHeader(HTTP.Sec_WebSocket_Version, VERSION);
			response.setStatus(HTTPStatus.UPGRADE_REQUIRED);
			return false;
		}

		// Sec-WebSocket-Protocol: subprotocols
		value = request.getHeader(HTTP.Sec_WebSocket_Protocol);
		value = protocol(value);
		if (Utility.noEmpty(value)) {
			response.addHeader(HTTP.Sec_WebSocket_Protocol, value);
		}

		// Sec-WebSocket-Extensions: permessage-deflate
		value = request.getHeader(HTTP.Sec_WebSocket_Extensions);
		value = extensions(value);
		if (Utility.noEmpty(value)) {
			response.addHeader(HTTP.Sec_WebSocket_Extensions, value);
		}

		// Sec-WebSocket-Key: lqy8ApLbw+oVNBkMGrpceg==
		// Sec-WebSocket-Accept: response-key
		value = request.getHeader(HTTP.Sec_WebSocket_Key);
		if (Utility.isEmpty(value)) {
			response.setStatus(HTTPStatus.UPGRADE_REQUIRED);
		}

		response.setStatus(HTTPStatus.SWITCHING_PROTOCOL);
		response.addHeader(HTTP.Connection, HTTP.Upgrade);
		response.addHeader(HTTP.Upgrade, WEBSOCKET);
		response.addHeader(HTTP.Sec_WebSocket_Accept, SecWebSocketAccept.hash(value));
		return true;
	}

	/**
	 * 创建消息处理对象接收或发送 WEB Socket 消息
	 */
	protected abstract WEBSocketHandler create(HTTPSlave chain);

	/**
	 * 请求的协议升级扩展
	 * 
	 * <pre>
	 * Sec-WebSocket-Extensions: permessage-deflate
	 * </pre>
	 */
	protected String extensions(String value) {
		return null;
	}

	/**
	 * 请求的子协议
	 * 
	 * <pre>
	 * Sec-WebSocket-Protocol: subprotocols
	 * </pre>
	 */
	protected String protocol(String value) {
		return null;
	}

	/**
	 * 是否允许请求源
	 */
	protected boolean allowOrigin(String origin) {
		return true;
	}
}