/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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