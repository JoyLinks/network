package com.joyzl.network.tls;

public enum HeartbeatMessageType {

	HEARTBEAT_REQUEST(1), HEARTBEAT_RESPONSE(2),
	// MAX(255)
	;

	private final int code;

	private HeartbeatMessageType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static HeartbeatMessageType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}