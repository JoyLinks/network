package com.joyzl.network.tls;

public enum ExtensionType {

	/** RFC 6066 */
	SERVER_NAME(0),
	/** RFC 6066 */
	MAX_FRAGMENT_LENGTH(1),
	/** RFC 6066 */
	CLIENT_CERTIFICATE_URL(2),
	/** RFC 6066 */
	TRUSTED_CA_KEYS(3),
	/** RFC 6066 */
	TRUNCATED_HMAC(4),
	/** RFC 6066 */
	STATUS_REQUEST(5),
	/** RFC 8422,7919 */
	SUPPORTED_GROUPS(10),
	/** RFC 8446 */
	SIGNATURE_ALGORITHMS(13),
	/** RFC 5764 */
	USE_SRTP(14),
	/** RFC 6520 */
	HEARTBEAT(15),
	/** RFC 7301 */
	APPLICATION_LAYER_PROTOCOL_NEGOTIATION(16),
	/** RFC 6962 */
	SIGNED_CERTIFICATE_TIMESTAMP(18),
	/** RFC 7250 */
	CLIENT_CERTIFICATE_TYPE(19),
	/** RFC 7250 */
	SERVER_CERTIFICATE_TYPE(20),
	/** RFC 7685 */
	PADDING(21),
	/** RESERVED */
	RESERVED1(40),
	/** RFC 8446 */
	PRE_SHARED_KEY(41),
	/** RFC 8446 */
	EARLY_DATA(42),
	/** RFC 8446 */
	SUPPORTED_VERSIONS(43),
	/** RFC 8446 */
	COOKIE(44),
	/** RFC 8446 */
	PSK_KEY_EXCHANGE_MODES(45),
	/** RESERVED */
	RESERVED2(46),
	/** RFC 8446 */
	CERTIFICATE_AUTHORITIES(47),
	/** RFC 8446 */
	OID_FILTERS(48),
	/** RFC 8446 */
	POST_HANDSHAKE_AUTH(49),
	/** RFC 8446 */
	SIGNATURE_ALGORITHMS_CERT(50),
	/** RFC 8446 */
	KEY_SHARE(51);

	// MAX(65535)

	private final int code;

	private ExtensionType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}

	public static ExtensionType code(int value) {
		for (int index = 0; index < values().length; index++) {
			if (values()[index].code() == value) {
				return values()[index];
			}
		}
		return null;
	}
}