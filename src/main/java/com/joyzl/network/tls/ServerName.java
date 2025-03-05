package com.joyzl.network.tls;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

class ServerName {

	// NameType MAX(255)

	public final static byte HOST_NAME = 0;

	////////////////////////////////////////////////////////////////////////////////

	private byte type = HOST_NAME;
	private byte[] name = TLS.EMPTY_BYTES;

	public ServerName() {
	}

	public ServerName(String name) {
		setName(name);
	}

	public ServerName(SocketAddress remote) {
		setName(findServerName(remote));
	}

	public ServerName(byte type, byte[] name) {
		this.type = type;
		this.name = name;
	}

	public ServerName(byte type, String name) {
		this.type = type;
		setName(name);
	}

	public byte getType() {
		return type;
	}

	public void setType(byte value) {
		type = value;
	}

	public byte[] getName() {
		return name;
	}

	public String getNameString() {
		return new String(name, StandardCharsets.US_ASCII);
	}

	public void setName(byte[] value) {
		if (value == null) {
			name = TLS.EMPTY_BYTES;
		} else {
			name = value;
		}
	}

	public void setName(String value) {
		if (value == null) {
			name = TLS.EMPTY_BYTES;
		} else {
			name = value.getBytes(StandardCharsets.US_ASCII);
		}
	}

	@Override
	public String toString() {
		return new String(name, StandardCharsets.US_ASCII);
	}

	/**
	 * 从远端地址中获得服务名称(TLS SNI)
	 */
	static String findServerName(SocketAddress address) {
		if (address instanceof InetSocketAddress) {
			final InetSocketAddress i = (InetSocketAddress) address;
			if (i.getHostName() != null) {
				return i.getHostName();
			}
			if (i.getHostString() != null) {
				return i.getHostString();
			}
			if (i.getAddress().getHostName() != null) {
				return i.getAddress().getHostName();
			}
		}
		return "localhost";
	}
}