/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 8446
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
class NewSessionTicket2 extends NewSessionTicket1 {

	/** 2^32 */
	final static long MOD = 4294967296L;
	/** MAX 7Day */
	final static int LIFETIME_MAX = 604800;

	////////////////////////////////////////////////////////////////////////////////

	private int age_add;
	private byte[] nonce = TLS.EMPTY_BYTES;

	public NewSessionTicket2() {
		super();
	}

	public byte[] getNonce() {
		return nonce;
	}

	public void setNonce(byte[] value) {
		if (value == null) {
			nonce = TLS.EMPTY_BYTES;
		} else {
			nonce = value;
		}
	}

	public int getAgeAdd() {
		return age_add;
	}

	public void setAgeAdd(int value) {
		age_add = value;
	}

	////////////////////////////////////////////////////////////////////////////////

	/** NamedGroup */
	private short group;

	public short getGroup() {
		return group;
	}

	public void setGroup(short value) {
		group = value;
	}

	/**
	 * 计算用于客户端发送的AgeAdd值
	 */
	public int obfuscatedAgeAdd() {
		long a = age_add & 0xFFFFFFFFL;
		return (int) ((System.currentTimeMillis() - timestamp() + a) % MOD);
	}

	/**
	 * 验证客户端发送的AgeAdd值是否有效
	 */
	public boolean checkAgeAdd(int value) {
		if (age_add == 0) {
			return true;
		}
		if (value - age_add < getLifetime() * 1000) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(":lifetime=");
		b.append(getLifetime());
		b.append(",age_add=");
		b.append(age_add & 0xFFFFFFFFL);
		b.append(",nonce=");
		b.append(nonce.length);
		b.append("Byte");
		b.append(",ticket=");
		b.append(getTicket().length);
		b.append("Byte");
		return b.toString();
	}
}