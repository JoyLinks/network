package com.joyzl.network.tls;

public class SessionTicket extends Extension {

	private byte[] ticket = TLS.EMPTY_BYTES;

	@Override
	public short type() {
		return SESSION_TICKET;
	}

	public byte[] getTicket() {
		return ticket;
	}

	public void setTicket(byte[] value) {
		if (value == null) {
			ticket = TLS.EMPTY_BYTES;
		} else {
			ticket = value;
		}
	}
}