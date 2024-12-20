package com.joyzl.network.tls;

/**
 * <pre>
 * struct { } HelloRequest;
 * </pre>
 * 
 * @author ZhangXi 2024年12月20日
 */
public class HelloRequest extends Handshake {

	public static final HelloRequest INSTANCE = new HelloRequest();

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.HELLO_REQUEST;
	}
}