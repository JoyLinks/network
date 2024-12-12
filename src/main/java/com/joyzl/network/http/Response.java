/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * HTTP 响应
 * 
 * @author ZhangXi
 * @date 2021年9月30日
 */
public class Response extends HTTPMessage {

	private int status = HTTPStatus.OK.code();
	private String text = HTTPStatus.OK.text();

	@Override
	public void setVersion(String value) {
		super.setVersion(value);
		// HTTP/1.1 默认长连接
		// HTTP/1.0 默认短连接
		if (HTTP.V10 == value) {
			needClose();
		}
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(HTTPStatus value) {
		status = value.code();
		text = value.text();
	}

	public void setStatus(int value) {
		status = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String value) {
		text = value;
	}

	@Override
	public String toString() {
		return status + " " + getVersion() + HTTPCoder.SPACE + text;
	}

	/**
	 * 获取响应内容是否分块发送
	 */
	public boolean isChunked() {
		return Utility.same(TransferEncoding.CHUNKED, getHeader(TransferEncoding.NAME));
	}
}