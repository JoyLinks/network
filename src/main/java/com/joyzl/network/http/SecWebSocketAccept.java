/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Sec-WebSocket-Accept
 * 
 * @author ZhangXi
 * @date 2021年10月18日
 */
public final class SecWebSocketAccept extends Header {

	public final static String NAME = "Sec-WebSocket-Accept";

	private String key;
	private String guid;
	private String value;

	public SecWebSocketAccept() {
	}

	public SecWebSocketAccept(String key) {
		this(key, UUID.randomUUID().toString());
	}

	public SecWebSocketAccept(String key, String guid) {
		this.key = key;
		this.guid = guid;
	}

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		if (value == null) {
			// base64( SHA1( Sec-WebSocket-Key + GUID ) )
			final byte[] bytes = SHA1(key + guid);
			value = new String(Base64.getEncoder().encode(bytes), HTTPCoder.URL_CHARSET);
		}
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}

	/**
	 * SHA1(安全哈希算法)
	 */
	final static byte[] SHA1(String value) {
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
			messageDigest.update(value.getBytes());
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String value) {
		key = value;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String value) {
		guid = value;
	}
}