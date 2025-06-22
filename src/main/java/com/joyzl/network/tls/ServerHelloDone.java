/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * TLS 1.2 1.1 1.0
 * 
 * <pre>
 * struct { } ServerHelloDone;
 * </pre>
 * 
 * @author ZhangXi 2025年3月6日
 */
class ServerHelloDone extends Handshake {

	final static ServerHelloDone INSTANCE = new ServerHelloDone();

	private ServerHelloDone() {
	}

	@Override
	public byte msgType() {
		return SERVER_HELLO_DONE;
	}
}