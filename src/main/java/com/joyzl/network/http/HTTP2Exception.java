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