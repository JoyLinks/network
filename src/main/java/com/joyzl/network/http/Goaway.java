package com.joyzl.network.http;

public class Goaway extends Message {

	/** 本地发现的连接错误 */
	private final boolean local;
	private int lastStreamID;
	private int error;

	public Goaway() {
		super(0, COMPLETE);
		local = false;
	}

	public Goaway(int error) {
		super(0, COMPLETE);
		this.error = error;
		local = true;
	}

	public Goaway(int last, int error) {
		super(0, COMPLETE);
		this.error = error;
		lastStreamID = last;
		local = true;
	}

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

	public boolean isLocal() {
		return local;
	}

	@Override
	public String toString() {
		return "GOAWAY:lastStreamID=" + lastStreamID + ",error=" + HTTP2.errorText(error);
	}
}