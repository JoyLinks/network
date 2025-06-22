/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Referer
 * 
 * <pre>
 * Referer: url
 * </pre>
 * 
 * @author ZhangXi 2024年11月15日
 */
public final class Referer extends Header {

	public final static String NAME = HTTP1.Referer;

	private String url;

	public Referer() {
	}

	public Referer(String value) {
		url = value;
	}

	@Override
	public String getHeaderName() {
		return HTTP1.Referer;
	}

	@Override
	public String getHeaderValue() {
		return url;
	}

	@Override
	public void setHeaderValue(String value) {
		url = value;
	}

	public final static Referer parse(String value) {
		if (Utility.noEmpty(value)) {
			Referer header = new Referer();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}