/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Content-Length 是一个实体消息首部，用来指明发送给接收方的消息主体的大小，即用十进制数字表示的八位元组的数目。
 * 
 * @author ZhangXi
 * @date 2021年1月9日
 */
public final class ContentLength extends Header {

	public final static String NAME = HTTP1.Content_Length;
	public final static String ZERO = "0";

	private int length;

	public ContentLength() {
	}

	public ContentLength(int l) {
		length = l;
	}

	@Override
	public final String getHeaderName() {
		return HTTP1.Content_Length;
	}

	@Override
	public String getHeaderValue() {
		return Integer.toString(length);
	}

	@Override
	public void setHeaderValue(String value) {
		length = Integer.parseInt(value);
	}

	public final static ContentLength parse(String value) {
		if (Utility.noEmpty(value)) {
			ContentLength header = new ContentLength();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int value) {
		if (value < 0) {
			throw new IllegalArgumentException("长度值不合法:" + value);
		}
		length = value;
	}
}