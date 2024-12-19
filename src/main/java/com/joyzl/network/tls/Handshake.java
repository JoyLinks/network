package com.joyzl.network.tls;

/**
 * 握手消息
 * 
 * @author ZhangXi 2024年12月13日
 */
public abstract class Handshake extends Record {

	public abstract HandshakeType getMsgType();

	@Override
	public String toString() {
		return getMsgType().name();
	}
}