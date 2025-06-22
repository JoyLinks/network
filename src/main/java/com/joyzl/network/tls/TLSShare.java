/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;

/**
 * TLS各版本之间公用对象
 * 
 * @author ZhangXi 2025年3月24日
 */
public class TLSShare {

	private final DataBuffer buffer = DataBuffer.instance();
	private DataBuffer clientHello, serverHello;
	private String serverName;

	public DataBuffer buffer() {
		return buffer;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String value) {
		serverName = value;
	}

	public DataBuffer getClientHello() {
		return clientHello;
	}

	public void setClientHello(DataBuffer value) {
		if (clientHello != null) {
			clientHello.release();
		}
		clientHello = value;
	}

	public DataBuffer getServerHello() {
		return serverHello;
	}

	public void setServerHello(DataBuffer value) {
		if (serverHello != null) {
			serverHello.release();
		}
		serverHello = value;
	}
}