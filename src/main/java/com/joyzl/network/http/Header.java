/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
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
		return getHeaderName() + HTTP1Coder.COLON + HTTP1Coder.SPACE + getHeaderValue();
	}
}