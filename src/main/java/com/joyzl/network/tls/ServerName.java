package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

class ServerName {

	private final NameType type;
	private byte[] name;

	public ServerName(String name) {
		type = NameType.HOST_NAME;
		setName(name);
	}

	public ServerName(NameType type, byte[] name) {
		this.type = type;
		this.name = name;
	}

	public ServerName(NameType type, String name) {
		this.type = type;
		setName(name);
	}

	public NameType type() {
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