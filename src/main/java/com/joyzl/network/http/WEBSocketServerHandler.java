/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;

/**
 * WEBSocket SERVER Handler
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class WEBSocketServerHandler implements ChainGenericsHandler<HTTPSlave, WEBSocketMessage> {

	@Override
	public void connected(HTTPSlave slave) throws Exception {
		slave.getWEBSocketHandler().connected(slave);
		slave.receive();
	}

	@Override
	public Object decode(HTTPSlave slave, DataBuffer buffer) throws Exception {
		return WEBSocketCoder.read(null, buffer);
	}

	@Override
	public void received(HTTPSlave slave, WEBSocketMessage message) throws Exception {
		if (message == null) {
			slave.queue().clear();
		} else {
			if (message.getType() == WEBSocketMessage.BINARY) {
				slave.getWEBSocketHandler().received(slave, message);
			} else if (message.getType() == WEBSocketMessage.TEXT) {
				slave.getWEBSocketHandler().received(slave, message);
			} else if (message.getType() == WEBSocketMessage.PING) {
				message.setType(WEBSocketMessage.PONG);
				slave.send(message);
			} else if (message.getType() == WEBSocketMessage.PONG) {
				slave.close();
			} else if (message.getType() == WEBSocketMessage.CLOSE) {
				slave.close();
			}
		}
	}

	@Override
	public DataBuffer encode(HTTPSlave slave, WEBSocketMessage message) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		if (WEBSocketCoder.write(message, buffer)) {
			message.state(Message.COMPLETE);
		}
		return buffer;
	}

	@Override
	public void sent(HTTPSlave slave, WEBSocketMessage message) throws Exception {
		if (message == null) {
			slave.queue().clear();
		} else {
			if (message.state() == Message.COMPLETE) {
				slave.getWEBSocketHandler().sent(slave, message);
				if (message.isClose()) {
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
		slave.getWEBSocketHandler().disconnected(slave);
	}

	@Override
	public void beat(HTTPSlave slave) throws Exception {
		// 服务端不主动发送心跳，从链路也不触发此方法
		throw new IllegalStateException("WEBSocket:服务端不应触发链路检测");
	}
}