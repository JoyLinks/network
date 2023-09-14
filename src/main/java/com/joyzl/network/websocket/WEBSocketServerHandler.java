/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.websocket;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.network.web.WEBServerHandler;

/**
 * HTTP SERVER
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class WEBSocketServerHandler extends WEBServerHandler {

	@Override
	public final Message decode(ChainChannel<Message> chain, DataBuffer buffer) throws Exception {
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			return super.decode(chain, buffer);
		} else //
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			return WEBSocketCoder.read(buffer);
		} else {
			throw new IllegalStateException("链路状态异常:" + chain.type());
		}
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			super.received(chain, message);
		} else //
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			final WEBSocketSlave slave = (WEBSocketSlave) chain;
			final WebSocketMessage webSocketMessage = (WebSocketMessage) message;
			if (webSocketMessage.getType() == WebSocketMessage.CLOSE) {
				chain.close();
				if (slave.getServlet() != null) {
					slave.getServlet().received(slave, webSocketMessage);
				}
			} else {
				if (slave.getServlet() != null) {
					slave.getServlet().received(slave, webSocketMessage);
				}
				chain.receive();
			}
		}
	}

	@Override
	public final DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			return super.encode(chain, message);
		} else //
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			final WebSocketMessage websocket = (WebSocketMessage) message;
			final DataBuffer buffer = DataBuffer.instance();
			if (WEBSocketCoder.write(websocket, buffer, false)) {
				websocket.state(Message.COMPLETE);
			}
			return buffer;
		} else {
			throw new IllegalStateException("链路状态异常:" + chain.type());
		}
	}

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		if (message.state() == Message.COMPLETE) {
			// 消息发送完成
			if (chain.getType() == HTTPStatus.CLOSE.code()) {
				// 链路标记为须关闭
				chain.close();
			} else if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
				// 链路保持，继续接收消息
				// HTTP/1.1默认持久连接
				message.state(Message.COMMAND);
				chain.receive();
			} else if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
				// WEB Socket 继续发送排队消息
				((WEBSocketSlave) chain).sent(message);
			}
		} else {
			// 再次发送当前消息直至完成
			chain.send(message);
		}
	}
}