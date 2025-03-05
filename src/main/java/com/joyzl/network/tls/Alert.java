package com.joyzl.network.tls;

/**
 * 告警
 * 
 * <pre>
 * struct {
 *     AlertLevel level;
 *     AlertDescription description;
 * } Alert;
 * </pre>
 * 
 * @author ZhangXi 2024年12月20日
 */
class Alert extends Record {

	// AlertLevel MAX(255)

	public final static byte WARNING = 1;
	public final static byte FATAL = 2;

	// AlertDescription MAX(255)

	public final static byte CLOSE_NOTIFY = 0;
	public final static byte UNEXPECTED_MESSAGE = 10;
	public final static byte BAD_RECORD_MAC = 20;
	public final static byte DECRYPTION_FAILED = 21;
	public final static byte RECORD_OVERFLOW = 22;
	public final static byte DECOMPRESSION_FAILURE = 30;
	public final static byte HANDSHAKE_FAILURE = 40;
	public final static byte NO_CERTIFICATE = 41;
	public final static byte BAD_CERTIFICATE = 42;
	public final static byte UNSUPPORTED_CERTIFICATE = 43;
	public final static byte CERTIFICATE_REVOKED = 44;
	public final static byte CERTIFICATE_EXPIRED = 45;
	public final static byte CERTIFICATE_UNKNOWN = 46;
	public final static byte ILLEGAL_PARAMETER = 47;
	public final static byte UNKNOWN_CA = 48;
	public final static byte ACCESS_DENIED = 49;
	public final static byte DECODE_ERROR = 50;
	public final static byte DECRYPT_ERROR = 51;
	public final static byte EXPORT_RESTRICTION = 60;
	public final static byte PROTOCOL_VERSION = 70;
	public final static byte INSUFFICIENT_SECURITY = 71;
	public final static byte INTERNAL_ERROR = 80;
	public final static byte INAPPROPRIATE_FALLBACK = 86;
	public final static byte USER_CANCELED = 90;
	public final static byte NO_RENEGOTIATION = 100;
	public final static byte MISSING_EXTENSION = 109;
	public final static byte UNSUPPORTED_EXTENSION = 110;
	public final static byte CERTIFICATE_UNOBTAINABLE_RESERVED = 111;
	public final static byte UNRECOGNIZED_NAME = 112;
	public final static byte BAD_CERTIFICATE_STATUS_RESPONSE = 113;
	public final static byte BAD_CERTIFICATE_HASH_VALUE_RESERVED = 114;
	public final static byte UNKNOWN_PSK_IDENTITY = 115;
	public final static byte CERTIFICATE_REQUIRED = 116;
	public final static byte NO_APPLICATION_PROTOCOL = 120;

	////////////////////////////////////////////////////////////////////////////////

	private byte level;
	private byte description;

	public Alert() {
	}

	public Alert(byte description) {
		this.description = description;
		this.level = FATAL;
	}

	public Alert(byte level, byte description) {
		this.description = description;
		this.level = level;
	}

	public Alert(TLSException e) {
		this(e.getDescription());
	}

	@Override
	public byte contentType() {
		return ALERT;
	}

	public byte getDescription() {
		return description;
	}

	public void setDescription(byte value) {
		description = value;
	}

	public byte getLevel() {
		return level;
	}

	public void setLevel(byte value) {
		level = value;
	}

	@Override
	public String toString() {
		return level(level) + ':' + description(description);
	}

	public final static String level(byte code) {
		if (code == WARNING) {
			return "WARNING";
		}
		if (code == FATAL) {
			return "FATAL";
		}
		return "UNKNOWN";
	}

	public final static String description(byte code) {
		switch (code) {
			case CLOSE_NOTIFY:
				return "close notify";
			case UNEXPECTED_MESSAGE:
				return "unexpected message";
			case BAD_RECORD_MAC:
				return "bad record mac";
			case DECRYPTION_FAILED:
				return "decryption failed";
			case RECORD_OVERFLOW:
				return "record overflow";
			case DECOMPRESSION_FAILURE:
				return "decompression failure";
			case HANDSHAKE_FAILURE:
				return "handshake failure";
			case NO_CERTIFICATE:
				return "no certificate";
			case BAD_CERTIFICATE:
				return "bad certificate";
			case UNSUPPORTED_CERTIFICATE:
				return "unsupported certificate";
			case CERTIFICATE_REVOKED:
				return "certificate revoked";
			case CERTIFICATE_EXPIRED:
				return "certificate expired";
			case CERTIFICATE_UNKNOWN:
				return "certificate unknown";
			case ILLEGAL_PARAMETER:
				return "illegal parameter";
			case UNKNOWN_CA:
				return "unknown ca";
			case ACCESS_DENIED:
				return "access denied";
			case DECODE_ERROR:
				return "decode error";
			case DECRYPT_ERROR:
				return "decrypt error";
			case EXPORT_RESTRICTION:
				return "export restriction";
			case PROTOCOL_VERSION:
				return "protocol version";
			case INSUFFICIENT_SECURITY:
				return "insufficient security";
			case INTERNAL_ERROR:
				return "internal error";
			case INAPPROPRIATE_FALLBACK:
				return "inappropriate fallback";
			case USER_CANCELED:
				return "user canceled";
			case NO_RENEGOTIATION:
				return "no renegotiation";
			case MISSING_EXTENSION:
				return "missing extension";
			case UNSUPPORTED_EXTENSION:
				return "unsupported extension";
			case CERTIFICATE_UNOBTAINABLE_RESERVED:
				return "certificate unobtainable reserved";
			case UNRECOGNIZED_NAME:
				return "unrecognized name";
			case BAD_CERTIFICATE_STATUS_RESPONSE:
				return "bad certificate status response";
			case BAD_CERTIFICATE_HASH_VALUE_RESERVED:
				return "bad certificate hash value reserved";
			case UNKNOWN_PSK_IDENTITY:
				return "unknown psk identity";
			case CERTIFICATE_REQUIRED:
				return "certificate required";
			case NO_APPLICATION_PROTOCOL:
				return "no application protocol";
			default:
				return "unknown";
		}
	}
}