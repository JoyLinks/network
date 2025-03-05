package com.joyzl.network.tls;

class MessageHash extends Handshake {

	@Override
	public byte msgType() {
		return MESSAGE_HASH;
	}
}