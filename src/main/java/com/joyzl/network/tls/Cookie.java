package com.joyzl.network.tls;

/**
 * 
 * <pre>
 * struct {
 * 		opaque cookie<1..2^16-1>;
 * } Cookie;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class Cookie extends Extension {

	private byte[] cookie = TLS.EMPTY_BYTES;

	public Cookie() {
	}

	public Cookie(byte[] cookie) {
		this.cookie = cookie;
	}

	@Override
	public short type() {
		return COOKIE;
	}

	public byte[] getCookie() {
		return cookie;
	}

	public void setCookie(byte[] value) {
		if (value == null) {
			cookie = TLS.EMPTY_BYTES;
		} else {
			cookie = value;
		}
	}

	@Override
	public String toString() {
		return "cookie:" + cookie;
	}
}