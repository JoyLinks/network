package com.joyzl.network.http;

public class ResetStream extends Message {

	/** 本地发现的流错误 */
	private final boolean local;
	private int error;

	public ResetStream(int id, int error) {
		super(id, COMPLETE);
		this.error = error;
		local = true;
	}

	public ResetStream(int id) {
		super(id, COMPLETE);
		local = false;
	}

	public int getError() {
		return error;
	}

	public void setError(int value) {
		error = value;
	}

	public boolean isLocal() {
		return local;
	}

	@Override
	public String toString() {
		return "RST_STREAM:error=" + HTTP2.errorText(error);
	}
}