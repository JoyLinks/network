package com.joyzl.network.tls;

public class KeyShareServerHello extends KeyShare {

	private final KeyShareEntry server_share;

	public KeyShareServerHello(KeyShareEntry value) {
		server_share = value;
	}

	public KeyShareEntry serverShare() {
		return server_share;
	}
}