package com.joyzl.network.tls;

/**
 * <pre>
 * key_exchange:Diffie-Hellman
 * 
 * key_exchange:Elliptic Curve Diffie-Hellman
 * struct {
 *     uint8 legacy_form = 4;
 *     opaque X[coordinate_length];
 *     opaque Y[coordinate_length];
 * } UncompressedPointRepresentation;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class KeyShareEntry {

	private final NamedGroup group;
	private byte[] key_exchange;

	public KeyShareEntry(NamedGroup group) {
		this.group = group;
	}

	public KeyShareEntry(NamedGroup group, byte[] key_exchange) {
		this.group = group;
		this.key_exchange = key_exchange;
	}

	public NamedGroup group() {
		return group;
	}

	public byte[] getKeyExchange() {
		return key_exchange;
	}

	public void setKeyExchange(byte[] value) {
		key_exchange = value;
	}
}