/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

class KeyShareHelloRetryRequest extends KeyShare {

	private short selected_group;

	public KeyShareHelloRetryRequest() {
	}

	public KeyShareHelloRetryRequest(short group) {
		selected_group = group;
	}

	public short getSelectedGroup() {
		return selected_group;
	}

	public void setSelectedGroup(short value) {
		selected_group = value;
	}

	@Override
	public String toString() {
		return name() + ":RETRY " + NamedGroup.named(getSelectedGroup());
	}
}