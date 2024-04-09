/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
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
	public Message decode(ChainChannel<Message> chain, DataBuffer buffer) throws Exception {
		final WEBClient client = (WEBClient) chain;

		// 阻止超过最大限制的数据帧
		if (buffer.readable() > WEBContentCoder.MAX) {
			buffer.clear();
			return null;
		}

		final WEBResponse response = client.getResponse();

		// 消息逐段解码
		final HTTPReader reader = new HTTPReader(buffer);
		if (response.state() == Message.COMMAND) {
			if (HTTPCoder.readCommand(reader, response)) {
				response.state(Message.HEADERS);
				response.clearHeaders();
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
			response.state(Message.COMMAND);
			return response;
		}

		throw new IllegalStateException("消息状态无效:" + response.state());
	}

	@Override
	public void beat(ChainChannel<Message> chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
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
			request.clearParameters();
			request.clearHeaders();
			request.setContent(null);
			return buffer;
		}

		throw new IllegalStateException("消息状态无效:" + message.state());
	}

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		// HTTPClient仅发送WEBRequest
		if (message == null) {
		} else if (message.state() == Message.COMPLETE) {
			// 消息发送完成
			// 接收响应消息
			chain.receive();
		} else {
			// 再次发送当前消息直至完成
			chain.send(message);
		}
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		chain.close();
	}
}