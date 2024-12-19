package com.joyzl.network.tls;

/**
 * 握手消息类型
 * 
 * @author ZhangXi 2024年12月13日
 */
public enum HandshakeType {

	/** TLS 1.0 */
	HELLO_REQUEST(0),
	/** TLS 1.0 1.3 */
	CLIENT_HELLO(1),
	/** TLS 1.0 1.3 */
	SERVER_HELLO(2),
	/** TLS 1.3 */
	NEW_SESSION_TICKET(4),
	/** TLS 1.3 */
	END_OF_EARLY_DATA(5),
	/** TLS 1.3 */
	ENCRYPTED_EXTENSIONS(8),
	/** TLS 1.0 1.3 */
	CERTIFICATE(11),
	/** TLS 1.0 */
	SERVER_KEY_EXCHANGE(12),
	/** TLS 1.0 1.3 */
	CERTIFICATE_REQUEST(13),
	/** TLS 1.0 */
	SERVER_HELLO_DONE(14),
	/** TLS 1.0 1.3 */
	CERTIFICATE_VERIFY(15),
	/** TLS 1.0 */
	CLIENT_KEY_EXCHANGE(16),
	/** TLS 1.0 1.3 */
	FINISHED(20),
	CERTIFICATE_URL(21),
	CERTIFICATE_STATUS(22),
	/** TLS 1.3 */
	KEY_UPDATE(24),
	/** TLS 1.3 */
	MESSAGE_HASH(254);

	// MAX(255)

	private final int code;

	private HandshakeType(int code) {
		this.code = code;
	}

	public int code() {
		return code;
	}
}