/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * HTTP 请求
 * 
 * @author ZhangXi
 * @date 2021年9月30日
 */
public class Request extends Message {

	public final static String VERSION = "HTTP/1.1";

	private String method;
	private String uri;
	private String version = VERSION;

	public String getMethod() {
		return method;
	}

	public void setMethod(String value) {
		method = value;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String value) {
		uri = value;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String value) {
		version = value;
	}

	@Override
	public String toString() {
		return method + HTTPCoder.SPACE + version + HTTPCoder.SPACE + uri;
	}
}
