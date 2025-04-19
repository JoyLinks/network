package com.joyzl.network.http;

/**
 * HTTP 2 Ping
 * 
 * @author ZhangXi 2025年4月2日
 */
class Ping extends Message {

	private final long value;

	public Ping() {
		value = System.currentTimeMillis();
	}

	public Ping(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}
}