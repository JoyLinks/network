package com.joyzl.network.http;

public class Goaway extends Message {

	private int lastStreamID;
	private int error;

	public int getError() {
		return error;
	}

	public void setError(int value) {
		error = value;
	}

	public int getLastStreamID() {
		return lastStreamID;
	}

	public void setLastStreamID(int value) {
		lastStreamID = value;
	}
}