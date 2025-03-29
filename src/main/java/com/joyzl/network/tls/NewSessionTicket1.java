package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 5077
 * struct {
          uint32 ticket_lifetime_hint;
          opaque ticket<0..2^16-1>;
      } NewSessionTicket;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class NewSessionTicket1 extends HandshakeExtensions {

	private int lifetime;
	private byte[] ticket = TLS.EMPTY_BYTES;

	public NewSessionTicket1() {
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
		if (value == null) {
			ticket = TLS.EMPTY_BYTES;
		} else {
			ticket = value;
		}
	}

	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int value) {
		lifetime = value;
	}

	////////////////////////////////////////////////////////////////////////////////

	/** 票据构建的时间戳 */
	private final long timestamp;
	/** 票据对应恢复密钥(PSK) */
	private byte[] resumption = TLS.EMPTY_BYTES;
	/** CipherSuite */
	private short suite;

	public long timestamp() {
		return timestamp;
	}

	public byte[] getResumption() {
		return resumption;
	}

	public void setResumption(byte[] value) {
		resumption = value;
	}

	public short getSuite() {
		return suite;
	}

	public void setSuite(short value) {
		suite = value;
	}

	/**
	 * 验证票据是否有效（未过期）
	 */
	public boolean valid() {
		if (lifetime == 0) {
			// RFC5077 零表示未指定生命周期
			return true;
		}
		return (System.currentTimeMillis() - timestamp) / 1000 < lifetime;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(":lifetime=");
		b.append(lifetime);
		b.append(",ticket=");
		b.append(ticket.length);
		b.append("byte");
		return b.toString();
	}
}