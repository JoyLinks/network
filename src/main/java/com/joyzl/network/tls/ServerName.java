/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * 扩展：服务名称
 * 
 * @author ZhangXi 2025年3月10日
 */
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
		setName(from(remote));
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
	static String from(SocketAddress address) {
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