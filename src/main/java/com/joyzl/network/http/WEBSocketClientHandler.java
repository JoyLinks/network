/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;

/**
 * WEBSocket CLIENT Handler
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class WEBSocketClientHandler implements ChainGenericsHandler<HTTPClient, WEBSocketMessage> {

	@Override
	public void connected(HTTPClient client) throws Exception {
		client.receive();
	}

	@Override
	public DataBuffer encode(HTTPClient client, WEBSocketMessage message) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		if (WEBSocketCoder.write(message, buffer)) {
			message.state(Message.COMPLETE);
		}
		return buffer;
	}

	@Override
	public void sent(HTTPClient client, WEBSocketMessage message) throws Exception {
		if (message == null) {
			client.queue().clear();
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
		return WEBSocketCoder.read(null, buffer);
	}

	@Override
	public void received(HTTPClient client, WEBSocketMessage message) throws Exception {
		if (message == null) {
			client.queue().clear();
		} else {
			if (message.getType() == WEBSocketMessage.BINARY) {

			} else if (message.getType() == WEBSocketMessage.TEXT) {

			} else if (message.getType() == WEBSocketMessage.PING) {
				message.setType(WEBSocketMessage.PONG);
				client.send(message);
			} else if (message.getType() == WEBSocketMessage.PONG) {
				client.close();
			} else if (message.getType() == WEBSocketMessage.CLOSE) {
				client.close();
			}
		}
	}

	@Override
	public void beat(HTTPClient client) throws Exception {
		client.send(new WEBSocketMessage(WEBSocketMessage.PING));
	}

	@Override
	public void disconnected(HTTPClient client) throws Exception {
		client.stream().clear();
	}

	@Override
	public void error(HTTPClient client, Throwable e) {
	}
}