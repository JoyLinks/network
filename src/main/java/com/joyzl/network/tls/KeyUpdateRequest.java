package com.joyzl.network.tls;

public enum KeyUpdateRequest {

	UPDATE_NOT_REQUESTED(0), UPDATE_REQUESTED(1),
	// MAX(255)
	;

	private final int code;

	private KeyUpdateRequest(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static KeyUpdateRequest code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}