package com.joyzl.network.tls;

public class PskIdentity {

	private byte[] identity;
	private int ticket_age;
	private byte[] binder;

	public PskIdentity(int ticket_age, byte[] identity) {
		this.ticket_age = ticket_age;
		this.identity = identity;
		// TODO HMAC
		// this.binder = HMAC(identity);
	}

	public int getTicket_age() {
		return ticket_age;
	}

	public void setTicket_age(int value) {
		ticket_age = value;
	}

	public byte[] getIdentity() {
		return identity;
	}

	public void setIdentity(byte[] value) {
		identity = value;
	}

	public byte[] getBinder() {
		return binder;
	}

	public void setBinder(byte[] value) {
		binder = value;
	}
}