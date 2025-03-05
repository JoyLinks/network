package com.joyzl.network.tls;

/**
 * <pre>
 * struct { } HelloRequest;
 * </pre>
 * 
 * @author ZhangXi 2024年12月20日
 */
class HelloRequest extends Handshake {

	public static final HelloRequest INSTANCE = new HelloRequest();

	@Override
	public byte msgType() {
		return HELLO_REQUEST;
	}
}