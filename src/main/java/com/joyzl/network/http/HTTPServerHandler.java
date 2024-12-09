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

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		final HTTPSlave slave = (HTTPSlave) chain;
		if (message == null) {
			// TIMEOUT
			final Response response = new Response();
			response.addHeader(Connection.NAME, Connection.CLOSE);
			response.setStatus(HTTPStatus.REQUEST_TIMEOUT);
			slave.send(response);
		} else {
			final Request request = (Request) message;
			// 管道模式响应对象不能复用
			final Response response = new Response();
			// 设置响应默认版本
			response.setVersion(request.getVersion());
			// 设置响应后关闭标志
			response.setClose(Utility.same(Connection.CLOSE, request.getHeader(Connection.NAME)));
			// 业务处理
			received(slave, request, response);
		}
	}

	protected abstract void received(HTTPSlave slave, Request request, Response response);

	@Override
	public void beat(ChainChannel<Message> chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
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

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		if (message == null) {
			// 超时
		} else if (message.state() == Message.COMPLETE) {
			if (message instanceof Response) {
				// HTTP/1.1 默认长连接
				// HTTP/1.0 默认短连接
				final Response response = (Response) message;
				if (response.needClose() || response.getVersion() == HTTP.V10) {
					chain.close();
				} else {
					// 链路保持，继续接收消息
					message.state(Message.COMMAND);
					chain.receive();
				}
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