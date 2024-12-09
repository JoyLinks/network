/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
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

	public final static String NAME = HTTP.Referer;

	private String url;

	public Referer() {
	}

	public Referer(String value) {
		url = value;
	}

	@Override
	public String getHeaderName() {
		return HTTP.Referer;
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