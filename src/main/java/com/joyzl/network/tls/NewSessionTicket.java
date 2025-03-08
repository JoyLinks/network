package com.joyzl.network.tls;

import com.joyzl.network.Utility;

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
class NewSessionTicket extends HandshakeExtensions {

	/** 2^32 */
	final static long MOD = 4294967296L;
	/** MAX 7Day */
	public final static int LIFETIME_MAX = 604800;

	/** 票据构建的时间戳 */
	private final long timestamp;

	private int lifetime;
	private int age_add;
	private byte[] nonce;
	private byte[] ticket;

	public NewSessionTicket() {
		timestamp = System.currentTimeMillis();
	}

	@Override
	public byte msgType() {
		return NEW_SESSION_TICKET;
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

	public long timestamp() {
		return timestamp;
	}

	/**
	 * 计算用于客户端发送的AgeAdd值
	 */
	public int obfuscatedAgeAdd() {
		long a = age_add & 0xFFFFFFFFL;
		return (int) ((System.currentTimeMillis() - timestamp + a) % MOD);
	}

	/**
	 * 验证客户端发送的AgeAdd值是否有效
	 */
	public boolean checkAgeAdd(int value) {
		if (age_add == 0) {
			return true;
		}
		if (value - age_add < lifetime * 1000) {
			return true;
		}
		return false;
	}

	/**
	 * 验证票据是否有效（未过期）
	 */
	public boolean valid() {
		return (System.currentTimeMillis() - timestamp) / 1000 < lifetime;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(":lifetime=");
		b.append(lifetime);
		b.append(",age_add=");
		b.append(age_add & 0xFFFFFFFFL);
		b.append(",nonce=");
		b.append(Utility.hex(nonce));
		b.append(",ticket=");
		b.append(Utility.hex(ticket));
		return b.toString();
	}
}