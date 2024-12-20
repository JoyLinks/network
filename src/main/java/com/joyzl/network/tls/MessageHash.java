package com.joyzl.network.tls;

public class MessageHash extends Handshake {

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.MESSAGE_HASH;
	}

}