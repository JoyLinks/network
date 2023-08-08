/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * HTTP Header
 * 
 * <p>
 * 继承实现对HTTP Header的结构化编解码。
 * 必须提供NAME的静态常量标明头名称，应当实现toString()方法返回"NAME:VALUE"的字符串形式。
 * </p>
 * 
 * @author ZhangXi
 * @date 2021年10月9日
 */
public abstract class Header {

	public abstract String getHeaderName();

	public abstract String getHeaderValue();

	public abstract void setHeaderValue(String value);

	@Override
	public String toString() {
		return getHeaderName() + HTTPCoder.COLON + HTTPCoder.SPACE + getHeaderValue();
	}

	static boolean isEmpty(CharSequence value) {
		return value == null || value.length() == 0;
	}

	static boolean noEmpty(CharSequence value) {
		return value != null && value.length() > 0;
	}
}