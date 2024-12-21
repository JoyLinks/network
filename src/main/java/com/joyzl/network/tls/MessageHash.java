package com.joyzl.network.tls;

public class MessageHash extends Handshake {

	@Override
	public byte msgType() {
		return MESSAGE_HASH;
	}

}