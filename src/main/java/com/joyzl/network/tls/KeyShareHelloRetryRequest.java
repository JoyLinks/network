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