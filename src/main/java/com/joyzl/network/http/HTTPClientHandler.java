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
		// 客户端连接后不能立即启动接收
		// HTTP 必须由客户端主动发起请求后才能接收
	}

	@Override
	public Message decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final HTTPClient client = (HTTPClient) chain;
		final Response response = client.getResponse();

		// 消息逐段解码

		if (response.state() == Message.COMMAND) {
			if (HTTPCoder.readCommand(buffer, response)) {
				response.state(Message.HEADERS);
				response.clearHeaders();
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
			response.state(Message.COMMAND);
			return response;
		}

		throw new IllegalStateException("消息状态无效:" + response.state());
	}

	@Override
	public void beat(ChainChannel chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final Request request = (Request) message;
		final DataBuffer buffer = DataBuffer.instance();

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

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		// HTTPClient仅发送Request
		final Request request = (Request) message;
		if (message == null) {
		} else if (request.state() == Message.COMPLETE) {
			// 消息发送完成
			// 接收响应消息
			chain.receive();
		} else {
			// 再次发送当前消息直至完成
			chain.send(message);
		}
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		chain.close();
	}
}