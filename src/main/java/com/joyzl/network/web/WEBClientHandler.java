/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.ChainType;
import com.joyzl.network.http.Connection;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.http.Message;

/**
 * HTTP CLIENT
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class WEBClientHandler extends WEBContentCoder implements ChainHandler<Message> {

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		// 客户端连接后不能立即启动接收
		// HTTP 必须由客户端主动发起请求后才能接收
	}

	@Override
	public final Message decode(ChainChannel<Message> chain, DataBuffer buffer) throws Exception {
		final WEBClient client = (WEBClient) chain;
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			// 阻止超过最大限制的数据帧
			if (buffer.readable() > WEBContentCoder.MAX) {
				client.setResponse(null);
				buffer.clear();
				return null;
			}

			WEBResponse response = client.getResponse();
			if (response == null) {
				client.setResponse(response = new WEBResponse());
			}

			// 消息逐段解码
			final HTTPReader reader = new HTTPReader(buffer);
			if (response.state() == Message.COMMAND) {
				if (HTTPCoder.readCommand(reader, response)) {
					response.state(Message.HEADERS);
				} else {
					return null;
				}
			}
			if (response.state() == Message.HEADERS) {
				if (HTTPCoder.readHeaders(reader, response)) {
					response.state(Message.CONTENT);
				} else {
					return null;
				}
			}
			if (response.state() == Message.CONTENT) {
				if (WEBContentCoder.read(reader, response)) {
					response.state(Message.COMPLETE);
				} else {
					return null;
				}
			}
			if (response.state() == Message.COMPLETE) {
				client.setResponse(null);
				return response;
			}

			throw new IllegalStateException("消息状态无效:" + response.state());
		} else //
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			WebSocketMessage message = null;
			if (WEBSocketCoder.read(message, buffer)) {
				return message;
			} else {
				return null;
			}
		} else {
			throw new IllegalStateException("链路状态异常:" + chain.type());
		}
	}

	@Override
	public final void beat(ChainChannel<Message> chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		if (Utility.equals(Connection.CLOSE, message.getHeader(Connection.NAME), false)) {
			chain.setType(HTTPStatus.CLOSE.code());
			chain.close();
		}
	}

	@Override
	public final DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			final WEBRequest request = (WEBRequest) message;
			final DataBuffer buffer = DataBuffer.instance();
			final HTTPWriter writer = new HTTPWriter(buffer);

			// 消息逐段编码
			if (request.state() == Message.COMMAND) {
				WEBContentCoder.prepare(request);
				if (HTTPCoder.writeCommand(writer, request)) {
					request.state(Message.HEADERS);
				} else {
					return buffer;
				}
			}
			if (request.state() == Message.HEADERS) {
				if (HTTPCoder.writeHeaders(writer, request)) {
					request.state(Message.CONTENT);
				} else {
					return buffer;
				}
			}
			if (request.state() == Message.CONTENT) {
				if (WEBContentCoder.write(writer, request)) {
					request.state(Message.COMPLETE);
				} else {
					return buffer;
				}
			}
			if (request.state() == Message.COMPLETE) {
				request.setContent(null);
				return buffer;
			}

			throw new IllegalStateException("消息状态无效:" + message.state());
		} else //
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			final WebSocketMessage websocket = (WebSocketMessage) message;
			final DataBuffer buffer = DataBuffer.instance();
			if (WEBSocketCoder.write(websocket, buffer)) {
				websocket.state(Message.COMPLETE);
			}
			return buffer;
		} else {
			throw new IllegalStateException("链路状态异常:" + chain.type());
		}
	}

	@Override
	public final void sent(ChainChannel<Message> chain, Message message) throws Exception {
		chain.receive();
	}

	@Override
	public void disconnected(ChainChannel<Message> chain) throws Exception {
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		chain.close();
	}
}