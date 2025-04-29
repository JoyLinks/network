package com.joyzl.network.http;

public class WindowUpdate extends Message {

	private int increment;

	public WindowUpdate(int id) {
		super(id);
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int value) {
		increment = value;
	}

	@Override
	public String toString() {
		return "WINDOW_UPDATE:increment=" + increment;
	}
}