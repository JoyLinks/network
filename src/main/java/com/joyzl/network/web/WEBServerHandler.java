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
import com.joyzl.network.http.Connection;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPMessage;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.http.Message;

/**
 * HTTP SERVER
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class WEBServerHandler implements ChainHandler<Message> {

	// RFC 2616 HTTP/1.1
	// RFC 7540 HTTP/2 暂不支持
	// RFC 1867 multipart/form-data
	// 超大文件上传通过分片方式接收，由专门的Servlet处理

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		chain.receive();
	}

	@Override
	public Message decode(ChainChannel<Message> chain, DataBuffer buffer) throws Exception {
		final WEBSlave slave = (WEBSlave) chain;

		// 阻止超过最大限制的数据帧
		if (buffer.readable() > WEBContentCoder.MAX) {
			slave.setRequest(null);
			buffer.clear();
			return null;
		}

		// 获取暂存消息,通常是数据接收不足解码未完成的消息
		final WEBRequest request = slave.getRequest();

		// 消息逐段解码
		final HTTPReader reader = new HTTPReader(buffer);
		if (request.state() <= Message.COMMAND) {
			if (HTTPCoder.readCommand(reader, request)) {
				request.state(Message.HEADERS);
			} else {
				return null;
			}
		}
		if (request.state() == Message.HEADERS) {
			if (HTTPCoder.readHeaders(reader, request)) {
				request.state(Message.CONTENT);
			} else {
				return null;
			}
		}
		if (request.state() == Message.CONTENT) {
			if (WEBContentCoder.read(reader, request)) {
				request.state(Message.COMPLETE);
			} else {
				return null;
			}
		}
		if (request.state() == Message.COMPLETE) {
			// slave.setRequest(null);
			return request;
		}

		throw new IllegalStateException("消息状态无效:" + request.state());
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		// 当HTTP/1.0版本不再支持时以下判断可移除
		final HTTPMessage httpMessage = (HTTPMessage) message;
		if (Utility.equals(Connection.CLOSE, httpMessage.getHeader(Connection.NAME), false)) {
			chain.setType(HTTPStatus.CLOSE.code());
		}
	}

	@Override
	public void beat(ChainChannel<Message> chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		final WEBResponse response = (WEBResponse) message;
		final DataBuffer buffer = DataBuffer.instance();
		final HTTPWriter writer = new HTTPWriter(buffer);

		// 消息逐段编码
		if (response.state() <= Message.COMMAND) {
			WEBContentCoder.prepare(response);
			if (HTTPCoder.writeCommand(writer, response)) {
				response.state(Message.HEADERS);
			} else {
				return buffer;
			}
		}
		if (response.state() == Message.HEADERS) {
			if (HTTPCoder.writeHeaders(writer, response)) {
				response.state(Message.CONTENT);
			} else {
				return buffer;
			}
		}
		if (response.state() == Message.CONTENT) {
			if (WEBContentCoder.write(writer, response)) {
				response.state(Message.COMPLETE);
			} else {
				return buffer;
			}
		}
		if (response.state() == Message.COMPLETE) {
			response.setContent(null);
			return buffer;
		}

		throw new IllegalStateException("消息状态无效:" + message.state());
	}

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		if (message.state() == Message.COMPLETE) {
			// 消息发送完成
			if (chain.getType() == HTTPStatus.CLOSE.code()) {
				// 链路标记为须关闭
				chain.close();
			} else {
				// 链路保持，继续接收消息
				// HTTP/1.1默认持久连接
				message.state(Message.COMMAND);
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