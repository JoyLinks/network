package com.joyzl.network.tls;

public enum SRTPProtectionProfile {

	SRTP_AES128_CM_HMAC_SHA1_80(0x0001),
	SRTP_AES128_CM_HMAC_SHA1_32(0x0002),
	SRTP_NULL_HMAC_SHA1_80(0x0005),
	SRTP_NULL_HMAC_SHA1_32(0x0006),;

	private final int code;

	private SRTPProtectionProfile(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static SRTPProtectionProfile code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}