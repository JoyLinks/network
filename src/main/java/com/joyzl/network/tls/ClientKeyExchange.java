package com.joyzl.network.tls;

class ClientKeyExchange extends Handshake {

	@Override
	public byte msgType() {
		return CLIENT_KEY_EXCHANGE;
	}

}