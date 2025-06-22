/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.io.IOException;

public class HTTP2Exception extends IOException {

	private static final long serialVersionUID = 1L;

	private final int error;

	public HTTP2Exception(int error) {
		super(HTTP2.errorText(error));
		this.error = error;
	}

	public int getErrorCode() {
		return error;
	}
}