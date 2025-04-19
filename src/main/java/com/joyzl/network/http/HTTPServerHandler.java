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

/**
 * HTTP SERVER Handler
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTPServerHandler implements ChainHandler {

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.setContext(new Request());
		chain.receive();
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final HTTPSlave slave = (HTTPSlave) chain;

		if (slave.isWEBSocket()) {
			return WEBSocketCoder.read(null, buffer);
		}

		if (slave.isHTTP2()) {
			return HTTP2Coder.readRequest(slave.requestHPACK(), slave.streams(), buffer);
		}

		// HTTP1.1
		// 获取暂存消息
		// 服务端提供请求消息暂存以支持消息解码
		// 在网络传输中可能需要多次接收数据才能完成解码
		final Request request = slave.getContext(Request.class);

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

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		final HTTPSlave slave = (HTTPSlave) chain;
		if (slave.isWEBSocket()) {
			if (message == null) {
				// TIMEOUT
				chain.close();
			} else {
				slave.getWEBSocketHandler().received(slave, (WEBSocketMessage) message);
			}
		} else if (slave.isHTTP2()) {

		} else {
			if (message == null) {
				// TIMEOUT
				final Response response = new Response();
				response.addHeader(Connection.NAME, Connection.CLOSE);
				response.setStatus(HTTPStatus.REQUEST_TIMEOUT);
				slave.send(response);
			} else {
				final Request request = (Request) message;

				if (Utility.equal(request.getHeader(Upgrade.NAME), Upgrade.H2C)) {
					final String text = request.getHeader(HTTP.HTTP2_Settings);
					if (text != null) {
						final Settings settings = HTTP2Coder.readSettings(text);
						if (settings != null) {
							slave.upgradeHTTP2();
							slave.requestHPACK().update(settings.getHeaderTableSize());
							slave.requestHPACK().setMaxHeaderListSize(settings.getMaxHeaderListSize());
							slave.streams().update(settings.getMaxConcurrentStreams());
							slave.streams().setMaxFrameSize(settings.getMaxFrameSize());

							slave.responseHPACK().update(settings.getHeaderTableSize());

							// 响应升级HTTP2
							chain.send(HTTP2.RESPONSE_SWITCHING_PROTOCOL);

							settings.forServer();
							slave.responseHPACK().update(settings.getHeaderTableSize());
							slave.responseHPACK().setMaxHeaderListSize(settings.getMaxHeaderListSize());
							slave.streams().update(settings.getMaxConcurrentStreams());
							slave.streams().setMaxFrameSize(settings.getMaxFrameSize());

							// 发送HTTP2连接前奏
							chain.send(settings);
						}
					}
				}

				// 管道模式响应对象不能复用
				final Response response = new Response();
				// 设置响应默认版本
				response.setVersion(request.getVersion());
				// 业务处理
				received(slave, request, response);
			}
		}
	}

	protected abstract void received(HTTPSlave slave, Request request, Response response);

	@Override
	public void beat(ChainChannel chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final HTTPSlave slave = (HTTPSlave) chain;
		final DataBuffer buffer = DataBuffer.instance();

		if (message instanceof Response response) {
			if (response.getVersion() == HTTP.V20) {
				if (response.state() <= Message.COMMAND || response.state() <= Message.HEADERS) {
					HTTP2Coder.writeHeaders(slave.responseHPACK(), buffer, response);
				}
				if (response.state() == Message.CONTENT) {
					HTTP2Coder.writeData(response, buffer, null);
				}
				return buffer;
			} else {
				// HTTP1.1
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
		} else if (message instanceof WEBSocketMessage wsm) {
			if (WEBSocketCoder.write(wsm, buffer)) {
				wsm.state(Message.COMPLETE);
			}
			return buffer;
		} else if (message instanceof Settings settings) {
			HTTP2Coder.write(buffer, settings);
			return buffer;
		} else if (message instanceof Goaway goaway) {
			HTTP2Coder.write(buffer, goaway);
			return buffer;
		} else if (message instanceof Ping ping) {
			HTTP2Coder.write(buffer, ping);
			return buffer;
		}
		throw new IllegalStateException("未知消息:" + message);
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		final HTTPSlave slave = (HTTPSlave) chain;
		if (message == null) {
			// 超时
			chain.close();
		} else {
			final Message m = (Message) message;
			if (m.state() == Message.COMPLETE) {
				// 消息发送完成执行后处理

				if (m instanceof Response response) {
					if (response.isClose()) {
						chain.close();
					}
				}
				if (m instanceof WEBSocketMessage wsm) {
					slave.getWEBSocketHandler().sent(slave, wsm);
					if (wsm.isClose()) {
						chain.close();
					}
				}
				if (m instanceof Goaway) {
					chain.close();
				}
				m.reset();
			} else {
				// 再次发送当前消息直至完成
				chain.send(m);
				slave.sendNext();
			}
		}
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		final HTTPSlave slave = (HTTPSlave) chain;
		if (slave.isWEBSocket()) {
			slave.getWEBSocketHandler().disconnected(slave);
		}
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		chain.close();
	}
}