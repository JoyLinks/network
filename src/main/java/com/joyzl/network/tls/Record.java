package com.joyzl.network.tls;

/**
 * <pre>
 * enum {
 *     invalid(0),
 *     change_cipher_spec(20),
 *     alert(21),
 *     handshake(22),
 *     application_data(23),
 *     (255)
 * } ContentType;
 * 
 * struct {
 *     ContentType type;
 *     ProtocolVersion legacy_record_version;
 *     uint16 length;
 *     opaque fragment[TLSPlaintext.length];
 * } TLSPlaintext;
 * </pre>
 * 
 * <pre>
 * struct {
 *     opaque content[TLSPlaintext.length];
 *     ContentType type;
 *     uint8 zeros[length_of_padding];
 * } TLSInnerPlaintext;
 * 
 * struct {
 *     ContentType opaque_type = application_data; / 23 /
 *     ProtocolVersion legacy_record_version = 0x0303; / TLS v1.2 /
 *     uint16 length;
 *     opaque encrypted_record[TLSCiphertext.length];
 * } TLSCiphertext;
 * 
 * encrypted_record:TLSInnerPlaintext
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public abstract class Record extends TLS {

	/** TLSPlaintext 16K (2^14) */
	final static int PLAINTEXT_MAX = 16384;
	/** TLSCiphertext */
	final static int CIPHERTEXT_MAX = PLAINTEXT_MAX + 256;

	// ContentType MAX(255)

	/** 1.3 */
	public final static byte INVALID = 0;
	/** 1.0 */
	public final static byte CHANGE_CIPHER_SPEC = 20;
	/** 1.0 */
	public final static byte ALERT = 21;
	/** 1.0 */
	public final static byte HANDSHAKE = 22;
	/** 1.0 */
	public final static byte APPLICATION_DATA = 23;
	/** 1.3 */
	public final static byte HEARTBEAT = 24;

	////////////////////////////////////////////////////////////////////////////////

	private short version = TLS.V12;

	public abstract byte contentType();

	public final short getProtocolVersion() {
		return version;
	}

	public final void setProtocolVersion(short value) {
		version = value;
	}

	@Override
	public String toString() {
		return name(contentType());
	}

	public static String name(byte code) {
		if (HANDSHAKE == code) {
			return "handshake";
		}
		if (CHANGE_CIPHER_SPEC == code) {
			return "change_cipher_spec";
		}
		if (APPLICATION_DATA == code) {
			return "application_data";
		}
		if (HEARTBEAT == code) {
			return "heartbeat";
		}
		if (INVALID == code) {
			return "invalid";
		}
		if (ALERT == code) {
			return "alert";
		}
		return "unknown";
	}
}