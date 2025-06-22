/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 表示多个握手对象
 * 
 * @author ZhangXi 2025年2月12日
 */
class Handshakes extends Record {

	private int size = 0;
	private final Handshake[] handshakes = new Handshake[8];

	public Handshakes() {
	}

	public Handshakes(Handshake v) {
		add(v);
	}

	@Override
	public byte contentType() {
		return HANDSHAKE;
	}

	public void add(Handshake value) {
		handshakes[size++] = value;
	}

	public Handshake get(int index) {
		return handshakes[index];
	}

	public Handshake last() {
		return handshakes[size - 1];
	}

	public int size() {
		return size;
	}

	@Override
	public String toString() {
		if (size() > 0) {
			final StringBuilder b = new StringBuilder();
			for (int i = 0; i < size(); i++) {
				if (b.length() > 0) {
					b.append('\n');
				}
				b.append(get(i));
			}
			return b.toString();
		} else {
			return "handshakes:EMPTY";
		}
	}
}