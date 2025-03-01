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
public abstract class HTTPServerHandler implements ChainHandler {

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.receive();
	}

	@Override
	public Message decode(ChainChannel chain, DataBuffer buffer) throws Exception {
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
	public void received(ChainChannel chain, Object message) throws Exception {
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
				slave.getUpgradedHandler().received(slave, (WEBSocketMessage) message);
			}
			return;
		}
		throw new IllegalStateException("链路状态异常:" + chain.type());
	}

	protected abstract void received(HTTPSlave slave, Request request, Response response);

	@Override
	public void beat(ChainChannel chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
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
				return buffer;
			}

			throw new IllegalStateException("消息状态无效:" + response.state());
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
	public void sent(ChainChannel chain, Object message) throws Exception {
		if (message == null) {
			// 超时
		} else {
			final Message response = (Message) message;
			if (response.state() == Message.COMPLETE) {
				if (response.after() == Message.CLOSE) {
					response.reset();
					chain.close();
				} else {
					if (response.after() == Message.UPGRADE) {
						final WEBSocketHandler handler = (WEBSocketHandler) response.getContent();
						((HTTPSlave) chain).upgrade(handler);
						handler.connected((HTTPSlave) chain);
						response.reset();
						chain.receive();
					} else if (chain.type() == ChainType.TCP_HTTP_SLAVE_WEB_SOCKET) {
						((HTTPSlave) chain).getUpgradedHandler().sent((HTTPSlave) chain, (WEBSocketMessage) response);
					} else {
						response.reset();
						chain.receive();
					}
				}
			} else {
				// 再次发送当前消息直至完成
				chain.send(response);
			}
		}
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		((HTTPSlave) chain).getUpgradedHandler().disconnected((HTTPSlave) chain);
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		chain.close();
	}
}