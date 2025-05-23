/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Pragma 是在HTTP/1.0 中规定的通用首部。
 * 
 * @author ZhangXi
 * @date 2021年1月13日
 */
public final class Pragma extends Header {

	public final static String NAME = HTTP1.Pragma;
	public final static String NO_CACHE = "no-cache";

	private String control;

	@Override
	public String getHeaderName() {
		return HTTP1.Pragma;
	}

	@Override
	public String getHeaderValue() {
		return control;
	}

	@Override
	public void setHeaderValue(String value) {
		control = value.strip();
	}

	public final static Pragma parse(String value) {
		if (Utility.noEmpty(value)) {
			Pragma header = new Pragma();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String value) {
		control = value;
	}
}
