/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.chain.ChainType;

/**
 * HTTP SERVER Handler
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTPServerHandler implements ChainHandler<Message> {

	@Override
	public long getTimeoutRead() {
		return 3000L;
	}

	@Override
	public long getTimeoutWrite() {
		return 3000L;
	}

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		chain.receive();
	}

	@Override
	public Message decode(ChainChannel<Message> chain, DataBuffer buffer) throws Exception {
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			final HTTPSlave slave = (HTTPSlave) chain;
			// 获取暂存消息,通常是数据接收不足解码未完成的消息
			final Request request = slave.getRequest();

			// 消息逐段解码
			if (request.state() <= Message.COMMAND) {
				if (HTTPCoder.readCommand(buffer, request)) {
					request.state(Message.HEADERS);
					request.clearHeaders();
				} else {
					return null;
				}
			}
			if (request.state() == Message.HEADERS) {
				if (HTTPCoder.readHeaders(buffer, request)) {
					request.state(Message.CONTENT);
					request.clearParameters();
					request.clearContent();
				} else {
					return null;
				}
			}
			if (request.state() == Message.CONTENT) {
				if (HTTPCoder.readContent(buffer, request)) {
					request.state(Message.COMPLETE);
				} else {
					return null;
				}
			}
			if (request.state() == Message.COMPLETE) {
				request.state(Message.COMMAND);
				return request;
			}

			throw new IllegalStateException("消息状态无效:" + request.state());
		}
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			return WEBSocketCoder.read(null, buffer);
		}
		throw new IllegalStateException("链路状态异常:" + chain.type());
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		final HTTPSlave slave = (HTTPSlave) chain;
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			if (message == null) {
				// TIMEOUT
				final Response response = new Response();
				response.addHeader(Connection.NAME, Connection.CLOSE);
				response.setStatus(HTTPStatus.REQUEST_TIMEOUT);
				response.needClose();
				slave.send(response);
			} else {
				final Request request = (Request) message;
				// 管道模式响应对象不能复用
				final Response response = new Response();
				// 设置响应默认版本
				response.setVersion(request.getVersion());
				// 设置响应后关闭标志
				if (Utility.same(Connection.CLOSE, request.getHeader(Connection.NAME))) {
					response.needClose();
				}
				// 业务处理
				received(slave, request, response);
			}
			return;
		}
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			if (message == null) {
				// TIMEOUT
				chain.close();
			} else {
				final WEBSocketMessage webSocketMessage = (WEBSocketMessage) message;
				if (webSocketMessage.getType() == WEBSocketMessage.CLOSE) {
					slave.getUpgradedHandler().received(slave, webSocketMessage);
					chain.close();
				} else {
					slave.getUpgradedHandler().received(slave, webSocketMessage);
					chain.receive();
				}
			}
		}
		throw new IllegalStateException("链路状态异常:" + chain.type());
	}

	protected abstract void received(HTTPSlave slave, Request request, Response response);

	@Override
	public void beat(ChainChannel<Message> chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		if (chain.type() == ChainType.TCP_HTTP_SLAVE) {
			final DataBuffer buffer = DataBuffer.instance();
			final Response response = (Response) message;

			// 消息逐段编码
			if (response.state() <= Message.COMMAND) {
				if (HTTPCoder.writeCommand(buffer, response)) {
					response.state(Message.HEADERS);
				} else {
					return buffer;
				}
			}
			if (response.state() == Message.HEADERS) {
				if (HTTPCoder.writeHeaders(buffer, response)) {
					response.state(Message.CONTENT);
				} else {
					return buffer;
				}
			}
			if (response.state() == Message.CONTENT) {
				if (HTTPCoder.writeContent(buffer, response)) {
					response.state(Message.COMPLETE);
				} else {
					return buffer;
				}
			}
			if (response.state() == Message.COMPLETE) {
				response.clearContent();
				response.clearHeaders();
				return buffer;
			}

			throw new IllegalStateException("消息状态无效:" + message.state());
		}
		if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
			final WEBSocketMessage websocket = (WEBSocketMessage) message;
			final DataBuffer buffer = DataBuffer.instance();
			if (WEBSocketCoder.write(websocket, buffer)) {
				websocket.state(Message.COMPLETE);
			}
			return buffer;
		}
		throw new IllegalStateException("链路状态异常:" + chain.type());
	}

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		if (message == null) {
			// 超时
		} else if (message.state() == Message.COMPLETE) {
			if (message.after() == Message.CLOSE) {
				chain.close();
			} else {
				if (message.after() == Message.UPGRADE) {
					((HTTPSlave) chain).upgrade((WEBSocketHandler) message.getContent());
				}
				message.reset();
				// 链路保持，继续接收消息
				chain.receive();
			}
		} else {
			// 再次发送当前消息直至完成
			chain.send(message);
		}
	}

	@Override
	public void disconnected(ChainChannel<Message> chain) throws Exception {
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		chain.close();
	}
}