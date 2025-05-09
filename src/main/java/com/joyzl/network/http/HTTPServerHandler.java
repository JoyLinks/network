/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;

/**
 * HTTP SERVER Handler
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTPServerHandler implements ChainGenericsHandler<HTTPSlave, Message> {

	/** HTTP 2 */
	final HTTP2ServerHandler HANDLER2 = new HTTP2ServerHandler() {
		@Override
		public void error(HTTPSlave slave, Throwable e) {
			HTTPServerHandler.this.error(slave, e);
		}

		@Override
		protected void received(HTTPSlave slave, Request request, Response response) {
			HTTPServerHandler.this.received(slave, request, response);
		}
	};
	/** HTTP 1.1 1.0 */
	final HTTP1ServerHandler HANDLER1 = new HTTP1ServerHandler() {
		@Override
		public void error(HTTPSlave slave, Throwable e) {
			HTTPServerHandler.this.error(slave, e);
		}

		@Override
		protected void received(HTTPSlave slave, Request request, Response response) {
			HTTPServerHandler.this.received(slave, request, response);
		}
	};
	/** WEB Socket */
	final WEBSocketServerHandler WSHANDLER = new WEBSocketServerHandler() {
		@Override
		public void error(HTTPSlave slave, Throwable e) {
			HTTPServerHandler.this.error(slave, e);
		}
	};

	protected abstract void received(HTTPSlave slave, Request request, Response response);

	@Override
	public void connected(HTTPSlave slave) throws Exception {
		HANDLER1.connected(slave);
	}

	@Override
	public Object decode(HTTPSlave slave, DataBuffer buffer) throws Exception {
		if (slave.isHTTP2()) {
			return HANDLER2.decode(slave, buffer);
		} else if (slave.isWEBSocket()) {
			return WSHANDLER.decode(slave, buffer);
		} else {
			return HANDLER1.decode(slave, buffer);
		}
	}

	@Override
	public void received(HTTPSlave slave, Message message) throws Exception {
		if (slave.isHTTP2()) {
			HANDLER2.received(slave, message);
		} else if (slave.isWEBSocket()) {
			WSHANDLER.received(slave, message);
		} else {
			HANDLER1.received(slave, message);
		}
	}

	@Override
	public DataBuffer encode(HTTPSlave slave, Message message) throws Exception {
		if (slave.isHTTP2()) {
			return HANDLER2.encode(slave, message);
		} else if (slave.isWEBSocket()) {
			return WSHANDLER.encode(slave, message);
		} else {
			return HANDLER1.encode(slave, message);
		}
	}

	@Override
	public void sent(HTTPSlave slave, Message message) throws Exception {
		if (slave.isHTTP2()) {
			HANDLER2.sent(slave, message);
		} else if (slave.isWEBSocket()) {
			WSHANDLER.sent(slave, message);
		} else {
			HANDLER1.sent(slave, message);
		}
	}

	@Override
	public void disconnected(HTTPSlave slave) throws Exception {
		if (slave.isHTTP2()) {
			HANDLER2.disconnected(slave);
		} else if (slave.isWEBSocket()) {
			WSHANDLER.disconnected(slave);
		} else {
			HANDLER1.disconnected(slave);
		}
	}

	@Override
	public void beat(HTTPSlave chain) throws Exception {
		// 服务端不主动发送心跳，从链路也不触发此方法
		throw new IllegalStateException("HTTP:服务端不应触发链路检测");
	}
}