/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * 请求报文中必须包含Host。
 * 
 * <pre>
 * Host: <host>:<port>
 * </pre>
 * 
 * @author ZhangXi
 * @date 2024年11月13日
 */
public final class Host extends Header {

	public final static String NAME = HTTP1.Host;

	private String host;
	private int port;

	public Host() {
	}

	public Host(String host) {
		this(host, 0);
	}

	public Host(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public String getHeaderName() {
		return HTTP1.Host;
	}

	@Override
	public String getHeaderValue() {
		if (port > 0) {
			return host + HTTP1Coder.COLON + port;
		}
		return host;
	}

	@Override
	public void setHeaderValue(String value) {
		int index;
		if ((index = value.indexOf(HTTP1Coder.COLON)) > 0) {
			host = value.substring(0, index);
			port = Integer.parseInt(value, index + 1, value.length(), 10);
		} else {
			host = value;
			port = 0;
		}
	}

	public final static Host parse(String value) {
		if (Utility.noEmpty(value)) {
			Host header = new Host();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String value) {
		host = value;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int value) {
		port = value;
	}
}