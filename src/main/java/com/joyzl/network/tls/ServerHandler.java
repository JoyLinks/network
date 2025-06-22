/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * TLSServerHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
abstract class ServerHandler extends TLS implements ChainHandler {

	protected final ChainHandler handler;
	protected final TLSParameters parameters;

	public ServerHandler(ChainHandler handler, TLSParameters parameters) {
		this.parameters = parameters;
		this.handler = handler;
	}

	public long getTimeoutRead() {
		return handler.getTimeoutRead();
	}

	public long getTimeoutWrite() {
		return handler.getTimeoutWrite();
	}

	public void disconnected(ChainChannel chain) throws Exception {
		handler.disconnected(chain);
	}

	public void error(ChainChannel chain, Throwable e) {
		handler.error(chain, e);
	}

	protected void heartbeat(ChainChannel chain, HeartbeatMessage message) {
		// 服务端仅响应心跳检查
		if (message.getMessageType() == HeartbeatMessage.HEARTBEAT_REQUEST) {
			message.setMessageType(HeartbeatMessage.HEARTBEAT_RESPONSE);
			chain.send(message);
		} else {
			chain.send(new Alert(Alert.UNEXPECTED_MESSAGE));
		}
	}
}