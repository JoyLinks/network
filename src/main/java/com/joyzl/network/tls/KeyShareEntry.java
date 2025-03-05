package com.joyzl.network.tls;

/**
 * <pre>
 * key_exchange:Diffie-Hellman (DHE)
 * key_exchange:Elliptic Curve Diffie-Hellman (ECDHE)
 * 
 * struct {
 *     uint8 legacy_form = 4;
 *     opaque X[coordinate_length];
 *     opaque Y[coordinate_length];
 * } UncompressedPointRepresentation;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class KeyShareEntry {

	private short group;
	private byte[] key_exchange;

	public KeyShareEntry() {
	}

	public KeyShareEntry(short group) {
		this.group = group;
	}

	public KeyShareEntry(short group, byte[] key_exchange) {
		this.group = group;
		this.key_exchange = key_exchange;
	}

	public short getGroup() {
		return group;
	}

	public void setGroup(short value) {
		group = value;
	}

	public byte[] getKeyExchange() {
		return key_exchange;
	}

	public void setKeyExchange(byte[] value) {
		key_exchange = value;
	}
}