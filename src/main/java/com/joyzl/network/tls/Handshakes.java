package com.joyzl.network.tls;

/**
 * 表示多个握手对象
 * 
 * @author ZhangXi 2025年2月12日
 */
public class Handshakes extends Record {

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

	public int size() {
		return size;
	}
}