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

	private String version = HTTP.V11;
	private int status = HTTPStatus.OK.code();
	private String text = HTTPStatus.OK.text();

	private boolean close;

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String value) {
		version = value;
	}

	@Override
	public String toString() {
		return status + " " + version + HTTPCoder.SPACE + text;
	}

	/**
	 * 响应后是否关闭链路
	 */
	public boolean needClose() {
		return close;
	}

	/**
	 * 设置响应后是否关闭链路，默认由请求时设置
	 */
	public void setClose(boolean value) {
		close = value;
	}

	/**
	 * 获取响应内容是否分块发送
	 */
	public boolean isChunked() {
		return Utility.same(TransferEncoding.CHUNKED, getHeader(TransferEncoding.NAME));
	}
}