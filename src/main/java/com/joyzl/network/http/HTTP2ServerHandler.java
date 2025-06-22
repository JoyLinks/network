/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;

/**
 * HTTP2 SERVER Handler
 * <p>
 * 实现纯粹HTTP2服务时，客户端不应发送PRI(SM)连接前奏请求；此前奏请求是在HTTP1.1/1.0兼容模式时使用的。
 * </p>
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class HTTP2ServerHandler implements ChainGenericsHandler<HTTPSlave, Message> {

	@Override
	public void connected(HTTPSlave slave) throws Exception {
		slave.upgradeHTTP2();
		slave.receive();
	}

	@Override
	public Object decode(HTTPSlave slave, DataBuffer buffer) throws Exception {
		return HTTP2Coder.read(slave.requestHPACK(), slave.messages(), buffer);
	}

	@Override
	public void received(HTTPSlave slave, Message message) throws Exception {
		if (message == null) {
			slave.messages().clear();
		} else if (message instanceof Request request) {
			if (request.state() == Message.COMPLETE) {
				final Response response = new Response();
				response.setVersion(request.getVersion());
				response.id(request.id());
				received(slave, request, response);
			}
		} else if (message instanceof Settings settings) {
			if (settings.isACK()) {
			} else {
				if (settingRequest(slave, settings)) {
					slave.send(settings.forACK());
				}
			}
		} else if (message instanceof Priority priority) {
			if (priority.getDependency() > 0 && priority.getDependency() != priority.id()) {

			} else {
				slave.send(new Goaway(HTTP2.PROTOCOL_ERROR));
			}
		} else if (message instanceof WindowUpdate windowUpdate) {
			if (windowUpdate.getIncrement() > 0) {
				// ？？为何要发送错误
				slave.send(new Goaway(HTTP2.PROTOCOL_ERROR));
			} else {
				slave.send(new Goaway(HTTP2.PROTOCOL_ERROR));
			}
		} else if (message instanceof ResetStream resetStream) {
			if (resetStream.isLocal()) {
				// 服务端终止流
				slave.send(resetStream);
			} else {
				// 客户端终止流
				final Request request = slave.messages().remove(resetStream.id());
				if (request != null) {
					request.clearContent();
				} else {
					slave.send(new Goaway(HTTP2.PROTOCOL_ERROR));
				}
			}
		} else if (message instanceof Goaway goaway) {
			if (goaway.isLocal()) {
				// 服务端错误
				slave.send(goaway);
			} else {
				// 客户端错误
				slave.close();
			}
		} else if (message instanceof Ping ping) {
			if (ping.isACK()) {
			} else {
				slave.send(ping.forACK());
			}
		}
	}

	protected abstract void received(HTTPSlave slave, Request request, Response response);

	@Override
	public DataBuffer encode(HTTPSlave slave, Message message) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		if (message instanceof Response response) {
			if (response.state() <= Message.COMMAND || response.state() <= Message.HEADERS) {
				if (HTTP2Coder.writeHeaders(slave.responseHPACK(), buffer, response)) {
					response.state(Message.COMPLETE);
				} else {
					response.state(Message.CONTENT);
				}
			}
			if (response.state() == Message.CONTENT) {
				if (HTTP2Coder.writeData(response, buffer, slave.responseHPACK().getMaxFrameSize())) {
					response.state(Message.COMPLETE);
				}
			}
			return buffer;
		} else if (message instanceof Settings settings) {
			HTTP2Coder.write(buffer, settings);
			return buffer;
		} else if (message instanceof Priority priority) {
			HTTP2Coder.write(buffer, priority);
			return buffer;
		} else if (message instanceof ResetStream resetStream) {
			HTTP2Coder.write(buffer, resetStream);
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
	public void sent(HTTPSlave slave, Message message) throws Exception {
		if (message == null) {
			slave.stream().clear();
		} else {
			if (message.state() == Message.COMPLETE) {
				if (message instanceof Response response) {
					if (response.isClose()) {
						slave.close();
						return;
					}
				}
				if (message instanceof Goaway) {
					// chain.close();
				}
				slave.sendNext(true);
			} else {
				slave.sendNext(false);
			}
		}
	}

	@Override
	public void disconnected(HTTPSlave slave) throws Exception {
		slave.stream().clear();
		slave.messages().clear();
	}

	@Override
	public void beat(HTTPSlave slave) throws Exception {
		// 服务端不主动发送心跳，从链路也不触发此方法
		throw new IllegalStateException("HTTP2:服务端不应触发链路检测");
	}

	/** 设置响应策参数 */
	static void settingResponse(HTTPSlave slave, Settings settings) {
		if (settings.hasHeaderTableSize()) {
			slave.responseHPACK().update(settings.getHeaderTableSize());
		}
		if (settings.hasMaxHeaderListSize()) {
			slave.responseHPACK().setMaxHeaderListSize(settings.getMaxHeaderListSize());
		}
		if (settings.hasMaxFrameSize()) {
			slave.responseHPACK().setMaxFrameSize(settings.getMaxFrameSize());
		}
		if (settings.hasInitialWindowSize()) {
			slave.requestHPACK().setWindowSize(settings.getInitialWindowSize());
		}
		if (settings.hasMaxConcurrentStreams()) {
			slave.stream().capacity(settings.getMaxConcurrentStreams());
		}
	}

	/** 设置请求侧策参数 */
	static boolean settingRequest(HTTPSlave slave, Settings settings) {
		if (settings.hasHeaderTableSize()) {
			slave.requestHPACK().update(settings.getHeaderTableSize());
		}
		if (settings.hasMaxHeaderListSize()) {
			slave.requestHPACK().setMaxHeaderListSize(settings.getMaxHeaderListSize());
		}
		if (settings.hasMaxConcurrentStreams()) {
			slave.stream().capacity(settings.getMaxConcurrentStreams());
		}
		if (settings.hasInitialWindowSize()) {
			if (settings.validInitialWindowSize()) {
				slave.requestHPACK().setWindowSize(settings.getInitialWindowSize());
			} else {
				slave.send(new Goaway(HTTP2.FLOW_CONTROL_ERROR));
				return false;
			}
		}
		if (settings.hasMaxFrameSize()) {
			if (settings.validMaxFrameSize()) {
				slave.requestHPACK().setMaxFrameSize(settings.getMaxFrameSize());
			} else {
				slave.send(new Goaway(HTTP2.PROTOCOL_ERROR));
				return false;
			}
		}
		if (settings.hasEnablePush()) {
			if (settings.validEnablePush()) {

			} else {
				slave.send(new Goaway(HTTP2.PROTOCOL_ERROR));
				return false;
			}
		}
		return true;
	}
}