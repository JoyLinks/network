/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.util.Map;

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
	private Map<String, String> attachHeaders;

	public Response() {
	}

	public Response(HTTPStatus status) {
		setStatus(status);
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
		return status + " " + getVersion() + HTTP1Coder.SPACE + text;
	}

	/**
	 * 获取响应内容是否分块发送
	 */
	public boolean isChunked() {
		return Utility.same(TransferEncoding.CHUNKED, getHeader(TransferEncoding.NAME));
	}

	/** 响应后是否关闭链路 */
	public boolean isClose() {
		return Utility.same(Connection.CLOSE, getHeader(Connection.NAME));
	}

	/**
	 * 获取附加头信息，这些头信息不会影响输出内容，仅原样输出到客户端
	 */
	public Map<String, String> getAttachHeaders() {
		return attachHeaders;
	}

	/**
	 * 设置附加头信息，这些头信息不会影响输出内容，仅原样输出到客户端
	 */
	public void setAttachHeaders(Map<String, String> value) {
		attachHeaders = value;
	}
}