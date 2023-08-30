package com.joyzl.network.chain.test;

public class Message {

	private int id;
	private int length;
	private byte[] bytes;

	public int getId() {
		return id;
	}

	public void setId(int value) {
		id = value;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int value) {
		length = value;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] value) {
		bytes = value;
	}
}