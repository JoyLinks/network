package com.joyzl.network.tls;

public class ServerKeyExchange extends Handshake {

	@Override
	public byte msgType() {
		return SERVER_KEY_EXCHANGE;
	}

}