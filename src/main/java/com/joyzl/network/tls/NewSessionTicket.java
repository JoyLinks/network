package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *     uint32 ticket_lifetime;
 *     uint32 ticket_age_add;
 *     opaque ticket_nonce<0..255>;
 *     opaque ticket<1..2^16-1>;
 *     Extension extensions<0..2^16-2>;
 * } NewSessionTicket;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class NewSessionTicket extends HandshakeExtensions {

	private int lifetime;
	private int age_add;
	private byte[] nonce;
	private byte[] ticket;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.NEW_SESSION_TICKET;
	}

	public byte[] getTicket() {
		return ticket;
	}

	public void setTicket(byte[] value) {
		ticket = value;
	}

	public byte[] getNonce() {
		return nonce;
	}

	public void setNonce(byte[] value) {
		nonce = value;
	}

	public int getAgeAdd() {
		return age_add;
	}

	public void setAgeAdd(int value) {
		age_add = value;
	}

	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int value) {
		lifetime = value;
	}
}