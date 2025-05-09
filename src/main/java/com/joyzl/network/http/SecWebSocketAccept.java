/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Sec-WebSocket-Accept
 * 
 * @author ZhangXi
 * @date 2021年10月18日
 */
public final class SecWebSocketAccept extends Header {

	public final static String NAME = HTTP1.Sec_WebSocket_Accept;
	public final static String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

	private String key;
	private String value;

	public SecWebSocketAccept() {
	}

	public SecWebSocketAccept(String key) {
		this.key = key;
	}

	@Override
	public String getHeaderName() {
		return HTTP1.Sec_WebSocket_Accept;
	}

	@Override
	public String getHeaderValue() {
		if (value == null) {
			value = hash(key);
		}
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}

	/**
	 * base64( SHA1( Sec-WebSocket-Key + GUID ) )
	 */
	public static String hash(String key) {
		key = key + GUID;
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
			messageDigest.update(key.getBytes(StandardCharsets.US_ASCII));
			return new String(Base64.getEncoder().encode(messageDigest.digest()), StandardCharsets.US_ASCII);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String value) {
		key = value;
		value = null;
	}
}