/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

class KeyShareServerHello extends KeyShare {

	private KeyShareEntry server_share;

	public KeyShareServerHello() {
	}

	public KeyShareServerHello(KeyShareEntry value) {
		server_share = value;
	}

	public KeyShareEntry getServerShare() {
		return server_share;
	}

	public void setServerShare(KeyShareEntry value) {
		server_share = value;
	}

	@Override
	public String toString() {
		if (getServerShare() != null) {
			return name() + ":" + getServerShare();
		} else {
			return name() + ":EMPTY";
		}
	}
}