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