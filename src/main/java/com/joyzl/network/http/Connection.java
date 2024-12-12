/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * Connection
 * 
 * @author ZhangXi
 * @date 2021年10月18日
 */
public final class Connection extends Header {

	public final static String NAME = HTTP.Connection;
	public final static String CLOSE = "close";
	public final static String KEEP_ALIVE = "keep-alive";

	private String value;

	public Connection() {
	}

	public Connection(String value) {
		this.value = value;
	}

	@Override
	public String getHeaderName() {
		return HTTP.Connection;
	}

	@Override
	public String getHeaderValue() {
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}
}