/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * HTTP 响应
 * 
 * @author ZhangXi
 * @date 2021年9月30日
 */
public class Response extends Message {

	private int status;
	private String text;
	private String version;

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
}
