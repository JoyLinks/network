/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.util.Iterator;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;

/**
 * HTTP CLIENT
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTP1ClientHandler implements ChainGenericsHandler<HTTPClient, Message> {

	@Override
	public void connected(HTTPClient client) throws Exception {
		client.receive();
	}

	@Override
	public DataBuffer encode(HTTPClient client, Message message) throws Exception {
		final Request request = (Request) message;
		final DataBuffer buffer = DataBuffer.instance();
		// 消息逐段编码
		if (request.state() == Message.COMMAND) {
			if (HTTP1Coder.writeCommand(buffer, request)) {
				request.state(Message.HEADERS);
			} else {
				return buffer;
			}
		}
		if (request.state() == Message.HEADERS) {
			if (HTTP1Coder.writeHeaders(buffer, request)) {
				request.state(Message.CONTENT);
			} else {
				return buffer;
			}
		}
		if (request.state() == Message.CONTENT) {
			if (HTTP1Coder.writeContent(buffer, request)) {
				request.state(Message.COMPLETE);
			} else {
				return buffer;
			}
		}
		if (request.state() == Message.COMPLETE) {
			return buffer;
		}
		buffer.release();
		throw new IllegalStateException("HTTP1:消息状态无效" + request.state());
	}

	@Override
	public void sent(HTTPClient client, Message message) throws Exception {
		if (message == null) {
			final Iterator<Message> iterator = client.queue().iterator();
			while (iterator.hasNext()) {
				message = iterator.next();
				iterator.remove();
				if (message instanceof Request request) {
					received(client, request, new Response(HTTPStatus.REQUEST_TIMEOUT));
				}
			}
		} else {
			if (message.state() == Message.COMPLETE) {
				client.sendNext(true);
			} else {
				client.sendNext(false);
			}
		}
	}

	@Override
	public Object decode(HTTPClient client, DataBuffer buffer) throws Exception {
		// 消息逐段解码
		Response response = client.getResponse();
		if (response == null) {
			client.setResponse(response = new Response());
		}
		if (response.state() == Message.COMMAND) {
			if (HTTP1Coder.readCommand(buffer, response)) {
				response.state(Message.HEADERS);
			} else {
				return null;
			}
		}
		if (response.state() == Message.HEADERS) {
			if (HTTP1Coder.readHeaders(buffer, response)) {
				response.state(Message.CONTENT);
			} else {
				return null;
			}
		}
		if (response.state() == Message.CONTENT) {
			if (HTTP1Coder.readContent(buffer, response)) {
				response.state(Message.COMPLETE);
			} else {
				return null;
			}
		}
		if (response.state() == Message.COMPLETE) {
			return response;
		}
		throw new IllegalStateException("HTTP1:消息状态无效" + response.state());
	}

	@Override
	public void received(HTTPClient client, Message message) throws Exception {
		if (message == null) {
			if (client.getResponse() != null) {
				client.getResponse().clearContent();
				client.setResponse(null);
			}
			final Iterator<Message> iterator = client.queue().iterator();
			while (iterator.hasNext()) {
				message = iterator.next();
				iterator.remove();
				if (message instanceof Request request) {
					received(client, request, new Response(HTTPStatus.REQUEST_TIMEOUT));
				}
			}
		} else {
			received(client, (Request) client.queue().poll(), (Response) message);
		}
	}

	protected abstract void received(HTTPClient client, Request request, Response response);

	@Override
	public void disconnected(HTTPClient client) throws Exception {
		client.queue().clear();
		if (client.getResponse() != null) {
			client.getResponse().clearContent();
			client.setResponse(null);
		}
	}

	@Override
	public void beat(HTTPClient client) throws Exception {
		throw new IllegalStateException("HTTP1:不支持链路检测");
	}

	@Override
	public void error(HTTPClient client, Throwable e) {
	}
}