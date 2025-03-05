package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * pre shared key
 * 
 * @author ZhangXi 2025年2月15日
 */
class PskIdentity {

	/** ticket */
	private byte[] identity = TLS.EMPTY_BYTES;
	/** obfuscated */
	private int ticket_age = 0;
	/** binder */
	private byte[] binder = TLS.EMPTY_BYTES;

	public PskIdentity() {
	}

	public PskIdentity(int ticket_age, byte[] identity) {
		setTicketAge(ticket_age);
		setIdentity(identity);
	}

	public int getTicketAge() {
		return ticket_age;
	}

	public void setTicketAge(int value) {
		ticket_age = value;
	}

	public byte[] getIdentity() {
		return identity;
	}

	public void setIdentity(byte[] value) {
		if (value == null) {
			identity = TLS.EMPTY_BYTES;
		} else {
			identity = value;
		}
	}

	public byte[] getBinder() {
		return binder;
	}

	public void setBinder(byte[] value) {
		if (value == null) {
			binder = TLS.EMPTY_BYTES;
		} else {
			binder = value;
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(identity);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof PskIdentity p) {
			return Arrays.equals(identity, p.identity);
		}
		return false;
	}
}