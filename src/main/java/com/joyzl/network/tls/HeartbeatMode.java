package com.joyzl.network.tls;

public enum HeartbeatMode {

	PEER_ALLOWED_TO_SEND(1), PEER_NOT_ALLOWED_TO_SEND(2),
	// MAX (255)
	;

	private final int code;

	private HeartbeatMode(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static HeartbeatMode code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}