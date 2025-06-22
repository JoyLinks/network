/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

class TLSException extends Exception {

	private static final long serialVersionUID = 1L;

	private final byte description;

	public TLSException(byte description) {
		this.description = description;
	}

	public byte getDescription() {
		return description;
	}
}