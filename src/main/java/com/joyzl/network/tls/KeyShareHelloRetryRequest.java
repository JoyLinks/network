package com.joyzl.network.tls;

public class KeyShareHelloRetryRequest extends KeyShare {

	private final short selected_group;

	public KeyShareHelloRetryRequest(short group) {
		selected_group = group;
	}

	public short selectedGroup() {
		return selected_group;
	}
}