package com.joyzl.network.tls;

public class ClientKeyExchange extends Handshake {

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CLIENT_KEY_EXCHANGE;
	}

}