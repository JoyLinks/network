/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

/**
 * HTTP 2 Ping
 * 
 * @author ZhangXi 2025年4月2日
 */
class Ping extends Message {

	private boolean ack = false;
	private final long value;

	public Ping() {
		super(0, COMPLETE);
		value = System.currentTimeMillis();
	}

	public Ping(long value) {
		super(0, COMPLETE);
		this.value = value;
	}

	public Ping(boolean ack, long value) {
		super(0, COMPLETE);
		this.value = value;
		this.ack = ack;
	}

	public Ping forACK() {
		ack = true;
		return this;
	}

	public long getValue() {
		return value;
	}

	public boolean isACK() {
		return ack;
	}

	@Override
	public String toString() {
		if (ack) {
			return "PING:ACK " + value;
		} else {
			return "PING:" + value;
		}
	}
}