/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Server
 * 
 * <pre>
 * Server: Apache/2.4.1 (Unix)
 * </pre>
 * 
 * @author ZhangXi 2024年11月15日
 */
public final class Server extends Header {

	public final static String NAME = HTTP1.Server;

	private String value;

	public Server() {
		this("JOYZL");
	}

	public Server(String value) {
		this.value = value;
	}

	@Override
	public String getHeaderName() {
		return HTTP1.Server;
	}

	@Override
	public String getHeaderValue() {
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}

	public final static Server parse(String value) {
		if (Utility.noEmpty(value)) {
			Server header = new Server();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}