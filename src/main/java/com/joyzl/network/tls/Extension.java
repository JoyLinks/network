package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 5246 TLSv1.2
 * 
 * struct {
 *     ExtensionType extension_type;
 *     opaque extension_data<0..2^16-1>;
 * } Extension;

 * enum {
 *     signature_algorithms(13), (65535)
 * } ExtensionType;
 * </pre>
 * 
 * @author ZhangXi 2024年12月20日
 */
public abstract class Extension {

	// Extension TYPE MAX(65535)

	/** RFC 6066 */
	public final static short SERVER_NAME = 0;
	/** RFC 6066 */
	public final static short MAX_FRAGMENT_LENGTH = 1;
	/** RFC 6066 */
	public final static short CLIENT_CERTIFICATE_URL = 2;
	/** RFC 6066 */
	public final static short TRUSTED_CA_KEYS = 3;
	/** RFC 6066 */
	public final static short TRUNCATED_HMAC = 4;
	/** RFC 6066 */
	public final static short STATUS_REQUEST = 5;
	/** RFC 8422,7919 elliptic_curves */
	public final static short SUPPORTED_GROUPS = 10;
	/** RFC 8422 */
	public final static short EC_POINT_FORMATS = 11;
	/** RFC 8446 */
	public final static short SIGNATURE_ALGORITHMS = 13;
	/** RFC 5764 */
	public final static short USE_SRTP = 14;
	/** RFC 6520 */
	public final static short HEARTBEAT = 15;
	/** RFC 7301 */
	public final static short APPLICATION_LAYER_PROTOCOL_NEGOTIATION = 16;
	/** RFC 6962 */
	public final static short SIGNED_CERTIFICATE_TIMESTAMP = 18;
	/** RFC 7250 */
	public final static short CLIENT_CERTIFICATE_TYPE = 19;
	/** RFC 7250 */
	public final static short SERVER_CERTIFICATE_TYPE = 20;
	/** RFC 7685 */
	public final static short PADDING = 21;
	/** EFC 7627 */
	public final static short EXTENDED_MASTER_SECRET = 23;
	/** RFC 8879 */
	public final static short COMPRESS_CERTIFICATE = 27;
	/** RFC ???? */
	public final static short SESSION_TICKET = 35;
	/** RESERVED */
	public final static short RESERVED1 = 40;
	/** RFC 8446 */
	public final static short PRE_SHARED_KEY = 41;
	/** RFC 8446 */
	public final static short EARLY_DATA = 42;
	/** RFC 8446 */
	public final static short SUPPORTED_VERSIONS = 43;
	/** RFC 8446 */
	public final static short COOKIE = 44;
	/** RFC 8446 */
	public final static short PSK_KEY_EXCHANGE_MODES = 45;
	/** RESERVED */
	public final static short RESERVED2 = 46;
	/** RFC 8446 */
	public final static short CERTIFICATE_AUTHORITIES = 47;
	/** RFC 8446 */
	public final static short OID_FILTERS = 48;
	/** RFC 8446 */
	public final static short POST_HANDSHAKE_AUTH = 49;
	/** RFC 8446 */
	public final static short SIGNATURE_ALGORITHMS_CERT = 50;
	/** RFC 8446 */
	public final static short KEY_SHARE = 51;

	/** RFC 5746 */
	public final static short RENEGOTIATION_INFO = (short) 0xFF01;
	/** RFC ???? */
	public final static short APPLICATION_SETTINGS = 0x4469;
	public final static short ENCRYPTED_CLIENT_HELLO = (short) 0xFE08;

	////////////////////////////////////////////////////////////////////////////////

	public abstract short type();

	public String name() {
		return name(type());
	}

	@Override
	public String toString() {
		return name();
	}

	public final static String name(short code) {
		switch (code) {
			case SERVER_NAME:
				return "server_name";
			case MAX_FRAGMENT_LENGTH:
				return "max_fragment_length";
			case CLIENT_CERTIFICATE_URL:
				return "client_certificate-url";
			case TRUSTED_CA_KEYS:
				return "trusted_ca_keys";
			case TRUNCATED_HMAC:
				return "truncated_hmac";
			case STATUS_REQUEST:
				return "status_request";
			case SUPPORTED_GROUPS:
				return "supported_groups";
			case EC_POINT_FORMATS:
				return "ec_point_formats";
			case SIGNATURE_ALGORITHMS:
				return "signature_algorithms";
			case USE_SRTP:
				return "use_srtp";
			case HEARTBEAT:
				return "heartbeat";
			case APPLICATION_LAYER_PROTOCOL_NEGOTIATION:
				return "application_layer_protocol_negotiation";
			case SIGNED_CERTIFICATE_TIMESTAMP:
				return "signed_certificate_timestamp";
			case CLIENT_CERTIFICATE_TYPE:
				return "client_certificate_type";
			case SERVER_CERTIFICATE_TYPE:
				return "server_certificate_type";
			case PADDING:
				return "padding";
			case EXTENDED_MASTER_SECRET:
				return "extended_master_secret";
			case COMPRESS_CERTIFICATE:
				return "compress_certificate";
			case SESSION_TICKET:
				return "session_ticket";
			case PRE_SHARED_KEY:
				return "pre_shared_key";
			case EARLY_DATA:
				return "early_data";
			case SUPPORTED_VERSIONS:
				return "supported_versions";
			case COOKIE:
				return "cookie";
			case PSK_KEY_EXCHANGE_MODES:
				return "psk_key_exchange_modes";
			case CERTIFICATE_AUTHORITIES:
				return "certificate_authorities";
			case OID_FILTERS:
				return "oid_filters";
			case POST_HANDSHAKE_AUTH:
				return "post_handshake_auth";
			case SIGNATURE_ALGORITHMS_CERT:
				return "signature_algorithms_cert";
			case KEY_SHARE:
				return "key_share";
			case RENEGOTIATION_INFO:
				return "renegotiation_info";
			case APPLICATION_SETTINGS:
				return "application_settings";
			case ENCRYPTED_CLIENT_HELLO:
				return "encrypted_client_hello";
			case RESERVED1:
				return "reserved";
			case RESERVED2:
				return "reserved";
			default:
				return "unknown";
		}
	}
}