/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;

/**
 * HTTP2 CLIENT Handler
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTP2ClientHandler implements ChainGenericsHandler<HTTPClient, Message> {

	@Override
	public void connected(HTTPClient client) throws Exception {
		client.receive();
	}

	@Override
	public DataBuffer encode(HTTPClient client, Message message) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		if (message instanceof Request request) {
			if (request.state() <= Message.COMMAND || request.state() <= Message.HEADERS) {
				if (HTTP2Coder.writeHeaders(client.responseHPACK(), buffer, request)) {
					request.state(Request.COMPLETE);
				} else {
					request.state(Request.CONTENT);
				}
			}
			if (request.state() == Message.CONTENT) {
				if (HTTP2Coder.writeData(request, buffer, client.responseHPACK().getMaxFrameSize())) {
					request.state(Message.COMPLETE);
				}
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
		buffer.release();
		throw new IllegalStateException("HTTP2:意外消息类型" + message);
	}

	@Override
	public void sent(HTTPClient client, Message message) throws Exception {
		if (message == null) {
			client.stream().clear();
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
		return HTTP2Coder.readResponse(client.responseHPACK(), client.messages(), buffer);
	}

	@Override
	public void received(HTTPClient client, Message message) throws Exception {
		if (message == null) {
			client.messages().clear();
		} else {
			received(client, (Response) message);
		}
	}

	protected abstract void received(HTTPClient client, Response response);

	@Override
	public void beat(HTTPClient client) throws Exception {
		client.send(new Ping());
	}

	@Override
	public void disconnected(HTTPClient client) throws Exception {
		client.messages().clear();
		client.stream().clear();
	}

	@Override
	public void error(HTTPClient client, Throwable e) {
	}
}