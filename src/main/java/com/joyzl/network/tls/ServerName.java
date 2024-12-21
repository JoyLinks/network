package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

class ServerName {

	// NameType MAX(255)

	public final static byte HOST_NAME = 0;

	////////////////////////////////////////////////////////////////////////////////

	private final byte type;
	private byte[] name;

	public ServerName(String name) {
		type = HOST_NAME;
		setName(name);
	}

	public ServerName(byte type, byte[] name) {
		this.type = type;
		this.name = name;
	}

	public ServerName(byte type, String name) {
		this.type = type;
		setName(name);
	}

	public byte type() {
		return type;
	}

	public byte[] getName() {
		return name;
	}

	public void setName(byte[] value) {
		name = value;
	}

	public void setName(String value) {
		this.name = value.getBytes(StandardCharsets.US_ASCII);
	}

	@Override
	public String toString() {
		return new String(name, StandardCharsets.US_ASCII);
	}
}