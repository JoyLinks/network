package com.joyzl.network.tls;

public class ServerHelloDone extends Handshake {

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.SERVER_HELLO_DONE;
	}

}