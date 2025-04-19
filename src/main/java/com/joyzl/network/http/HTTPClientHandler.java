/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * HTTP CLIENT
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTPClientHandler implements ChainHandler {

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.receive();
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final HTTPClient client = (HTTPClient) chain;

		if (client.isWEBSocket()) {
			return WEBSocketCoder.read(null, buffer);
		}

		if (client.isHTTP2()) {
			return HTTP2Coder.readRequest(client.responseHPACK(), client.streams(), buffer);
		}

		// HTTP1.1
		// 消息逐段解码
		final Response response = client.getResponse();
		if (response.state() == Message.COMMAND) {
			if (HTTPCoder.readCommand(buffer, response)) {
				response.state(Message.HEADERS);
			} else {
				return null;
			}
		}
		if (response.state() == Message.HEADERS) {
			if (HTTPCoder.readHeaders(buffer, response)) {
				response.state(Message.CONTENT);
			} else {
				return null;
			}
		}
		if (response.state() == Message.CONTENT) {
			if (HTTPCoder.readContent(buffer, response)) {
				response.state(Message.COMPLETE);
			} else {
				return null;
			}
		}
		if (response.state() == Message.COMPLETE) {
			return response;
		}

		throw new IllegalStateException("消息状态无效:" + response.state());
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
		final HTTPClient client = (HTTPClient) chain;
		if (client.isHTTP2()) {
			client.send(new Ping());
		}
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();

		if (message instanceof Request request) {
			if (request.getVersion() == HTTP.V20) {
				final HTTPClient client = (HTTPClient) chain;
				if (request.state() <= Message.COMMAND || request.state() <= Message.HEADERS) {
					HTTP2Coder.writeHeaders(client.responseHPACK(), buffer, request);
				}
				if (request.state() == Message.CONTENT) {
					HTTP2Coder.writeData(request, buffer, null);
				}
				return buffer;
			} else {
				// HTTP1.1
				// 消息逐段编码
				if (request.state() == Message.COMMAND) {
					if (HTTPCoder.writeCommand(buffer, request)) {
						request.state(Message.HEADERS);
					} else {
						return buffer;
					}
				}
				if (request.state() == Message.HEADERS) {
					if (HTTPCoder.writeHeaders(buffer, request)) {
						request.state(Message.CONTENT);
					} else {
						return buffer;
					}
				}
				if (request.state() == Message.CONTENT) {
					if (HTTPCoder.writeContent(buffer, request)) {
						request.state(Message.COMPLETE);
					} else {
						return buffer;
					}
				}
				if (request.state() == Message.COMPLETE) {
					request.clearParameters();
					request.clearHeaders();
					request.clearContent();
					return buffer;
				}
				throw new IllegalStateException("消息状态无效:" + request.state());
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
	public void received(ChainChannel chain, Object message) throws Exception {
		final HTTPClient client = (HTTPClient) chain;
		

	}

	protected abstract void received(HTTPClient client, Request request, Response response);

	protected abstract void received(HTTPClient client, WEBSocketMessage message);

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		if (message == null) {
			// 超时
			chain.reset();
		} else {
			final Message m = (Message) message;
			if (m.state() == Message.COMPLETE) {
				// 消息发送完成
				// 接收响应消息
			} else {
				// 再次发送当前消息直至完成
				chain.send(message);
			}
		}
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		chain.reset();
	}
}