package com.joyzl.network.tls;

public class ServerHelloDone extends Handshake {

	@Override
	public byte msgType() {
		return SERVER_HELLO_DONE;
	}

}