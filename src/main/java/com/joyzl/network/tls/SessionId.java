package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 会话标识，需要缓存会话时构建此类实例，此类提供基于会话标识(23Byte)生成的HashCode
 * 
 * @author ZhangXi 2024年12月17日
 */
public class SessionId {

	private final byte[] id;
	private final int hashCode;

	public SessionId() {
		id = new byte[32];
		int h = 0;
		for (int i = 0; i < id.length; i++) {
			id[i] = (byte) (Math.random() * 256);
			h += id[i];
		}
		hashCode = h;
	}

	public SessionId(byte[] d) {
		id = d;
		int h = 0;
		for (int i = 0; i < id.length; i++) {
			h += id[i];
		}
		hashCode = h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof SessionId) {
			final SessionId oid = (SessionId) o;
			return Arrays.equals(id, oid.id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}