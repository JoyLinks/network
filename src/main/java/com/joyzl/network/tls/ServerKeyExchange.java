package com.joyzl.network.tls;

public class ServerKeyExchange extends Handshake {

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.SERVER_KEY_EXCHANGE;
	}

}