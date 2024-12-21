package com.joyzl.network.tls;

public class ClientKeyExchange extends Handshake {

	@Override
	public byte msgType() {
		return CLIENT_KEY_EXCHANGE;
	}

}