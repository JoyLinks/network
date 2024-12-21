package com.joyzl.network.tls;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

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
public class KeyShareEntry {

	private final short group;
	private byte[] key_exchange;

	public KeyShareEntry(short group) {
		this.group = group;
	}

	public KeyShareEntry(short group, byte[] key_exchange) {
		this.group = group;
		this.key_exchange = key_exchange;
	}

	public short group() {
		return group;
	}

	public byte[] getKeyExchange() {
		return key_exchange;
	}

	public void setKeyExchange(byte[] value) {
		key_exchange = value;
	}

	public final static KeyPair make() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("X25519");
		// X25519 使用 255 位长度
		// X25519 的是 32 个字节， X448 的是 56 个字节
		keyPairGenerator.initialize(255);
		return keyPairGenerator.generateKeyPair();
	}
}