/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
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

	public final static String NAME = HTTP1.Connection;
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
		return HTTP1.Connection;
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