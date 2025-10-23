/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;

/**
 * HTTP1 SERVER Handler
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTP1ServerHandler implements ChainGenericsHandler<HTTPSlave, Message> {

	@Override
	public void connected(HTTPSlave slave) throws Exception {
		slave.receive();
	}

	@Override
	public Object decode(HTTPSlave slave, DataBuffer buffer) throws Exception {
		// 获取暂存消息
		// 服务端提供请求消息暂存以支持消息解码
		// 在网络传输中可能需要多次接收数据才能完成解码
		Request request = slave.getRequest();
		if (request == null) {
			slave.setRequest(request = new Request());
		}
		// 消息逐段解码
		if (request.state() <= Message.COMMAND) {
			if (HTTP1Coder.readCommand(buffer, request)) {
				request.state(Message.HEADERS);
			} else {
				return null;
			}
		}
		if (request.state() == Message.HEADERS) {
			if (HTTP1Coder.readHeaders(buffer, request)) {
				request.state(Message.CONTENT);
			} else {
				return null;
			}
		}
		if (request.state() == Message.CONTENT) {
			if (HTTP1Coder.readContent(buffer, request)) {
				request.state(Message.COMPLETE);
			} else {
				return null;
			}
		}
		if (request.state() == Message.COMPLETE) {
			slave.setRequest(null);
			return request;
		}
		throw new IllegalStateException("HTTP1:消息状态无效" + request.state());
	}

	@Override
	public void received(HTTPSlave slave, Message message) throws Exception {
		if (message == null) {
			if (slave.getRequest() != null) {
				slave.getRequest().clearContent();
				slave.setRequest(null);
			}
			// RESPONSE TIMEOUT
			final Response response = new Response();
			response.addHeader(Connection.NAME, Connection.CLOSE);
			response.setStatus(HTTPStatus.REQUEST_TIMEOUT);
			slave.send(response);
		} else {
			final Request request = (Request) message;
			if (request.getMethod() == HTTP1.PRI) {
				// HTTP2连接前奏，执行基于HTTP1的PRI(SM)请求
				slave.upgradeHTTP2();
				final Settings settings = new Settings();
				HTTP2ServerHandler.settingResponse(slave, settings.forServer());
				slave.send(settings);
				return;
			} else //
			if (Utility.equal(request.getHeader(Upgrade.NAME), Upgrade.H2C)) {
				// 升级HTTP2模式，如果失败执行常规响应
				// Connection: Upgrade, HTTP2-Settings
				// HTTP2-Settings: <base64>
				// Upgrade: h2c
				final String text = request.getHeader(HTTP1.HTTP2_Settings);
				if (text != null && text.length() > 0) {
					final Settings settings = HTTP2Coder.readSettings(text);
					if (settings != null && settings.valid()) {
						slave.send(HTTP2.RESPONSE_SWITCHING_PROTOCOL);
						slave.upgradeHTTP2();
						HTTP2ServerHandler.settingRequest(slave, settings);
						HTTP2ServerHandler.settingResponse(slave, settings.forServer());
						slave.send(settings);
					}
				}
			}

			// 管道模式响应对象不能复用
			final Response response = new Response();
			response.setVersion(request.getVersion());
			// 业务处理
			received(slave, request, response);
			request.clearContent();
		}
	}

	protected abstract void received(HTTPSlave slave, Request request, Response response);

	@Override
	public DataBuffer encode(HTTPSlave slave, Message message) throws Exception {
		final Response response = (Response) message;
		final DataBuffer buffer = DataBuffer.instance();
		// 消息逐段编码
		if (response.state() <= Message.COMMAND) {
			if (HTTP1Coder.writeCommand(buffer, response)) {
				response.state(Message.HEADERS);
			} else {
				return buffer;
			}
		}
		if (response.state() == Message.HEADERS) {
			if (HTTP1Coder.writeHeaders(buffer, response)) {
				response.state(Message.CONTENT);
			} else {
				return buffer;
			}
		}
		if (response.state() == Message.CONTENT) {
			if (HTTP1Coder.writeContent(buffer, response)) {
				response.state(Message.COMPLETE);
			} else {
				return buffer;
			}
		}
		if (response.state() == Message.COMPLETE) {
			response.clearContent();
			return buffer;
		}
		buffer.release();
		throw new IllegalStateException("HTTP1:消息状态无效" + response.state());
	}

	@Override
	public void sent(HTTPSlave slave, Message message) throws Exception {
		if (message == null) {
			slave.queue().clear();
		} else {
			if (message.state() == Message.COMPLETE) {
				final Response response = (Response) message;
				if (response.isClose()) {
					slave.close();
				} else {
					slave.sendNext(true);
				}
			} else {
				slave.sendNext(false);
			}
		}
	}

	@Override
	public void disconnected(HTTPSlave slave) throws Exception {
		slave.queue().clear();
		if (slave.getRequest() != null) {
			slave.getRequest().clearContent();
			slave.setRequest(null);
		}
	}

	@Override
	public void beat(HTTPSlave slave) throws Exception {
		// 服务端不主动发送心跳，从链路也不触发此方法
		throw new IllegalStateException("HTTP1:服务端不应触发链路检测");
	}
}