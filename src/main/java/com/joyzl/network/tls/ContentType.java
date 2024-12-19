package com.joyzl.network.tls;

public enum ContentType {

	/** 1.3 */
	INVALID(0),
	/** 1.0 */
	CHANGE_CIPHER_SPEC(20),
	/** 1.0 */
	ALERT(21),
	/** 1.0 */
	HANDSHAKE(22),
	/** 1.0 */
	APPLICATION_DATA(23),
	/** 1.3 */
	HEARTBEAT(24),
	// MAX(255)
	;

	private final int code;

	private ContentType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}
}