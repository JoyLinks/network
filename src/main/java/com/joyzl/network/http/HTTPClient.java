/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.chain.ChainType;
import com.joyzl.network.chain.TCPLink;

/**
 * HTTP 客户端
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class HTTPClient extends TCPLink {

	public HTTPClient(HTTPClientHandler handler, String host, int port) {
		super(handler, host, port);
	}

	@Override
	public ChainType type() {
		return ChainType.TCP_HTTP_CLIENT;
	}

	@Override
	public void close() {
		try {
			response.clearContent();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			super.close();
		}
	}

	// 客户端提供响应消息暂存以支持消息解码
	// 在网络传输中可能需要多次接收数据才能完成解码
	private final Response response = new Response();

	protected Response getResponse() {
		return response;
	}
}