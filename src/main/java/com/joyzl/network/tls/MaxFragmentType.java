package com.joyzl.network.tls;

public enum MaxFragmentType {

	/** 2^9 */
	MAX_512(1),
	/** 2^10 */
	MAX_1024(2),
	/** 2^11 */
	MAX_2048(3),
	/** 2^12 */
	MAX_4096(4),
	// MAX(255)
	;

	private final int code;

	private MaxFragmentType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static MaxFragmentType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}