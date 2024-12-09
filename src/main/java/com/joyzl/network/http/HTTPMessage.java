/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Request和Response的父类 提供HTTP Header支持
 * 
 * @author ZhangXi
 * @date 2021年10月8日
 */
public class HTTPMessage extends Message {

	private final Map<String, String> headers = new HashMap<>();

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addHeader(Header value) {
		headers.put(value.getHeaderName(), value.getHeaderValue());
	}

	public boolean hasHeader(String name) {
		return headers.containsKey(name);
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void clearHeaders() {
		headers.clear();
	}
}