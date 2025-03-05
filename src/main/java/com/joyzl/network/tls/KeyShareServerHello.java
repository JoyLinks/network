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
}