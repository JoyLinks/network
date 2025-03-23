package com.joyzl.network.tls;

/**
 * <pre>
 * TLS 1.3
 * 
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
 * <pre>
 * TLS 1.2
 * 
 * enum {
 *     change_cipher_spec(20),
 *     alert(21),
 *     handshake(22),
 *     application_data(23),
 *     (255)
 * } ContentType;
 * 
 * struct {
 *     ContentType type;
 *     ProtocolVersion version;
 *     uint16 length;
 *     opaque fragment[TLSPlaintext.length];
 * } TLSPlaintext;
 * 
 * struct {
 *     ContentType type;       // same as TLSPlaintext.type
 *     ProtocolVersion version;// same as TLSPlaintext.version
 *     uint16 length;
 *     opaque fragment[TLSCompressed.length];
 * } TLSCompressed;
 * 
 * struct {
 *     ContentType type;
 *     ProtocolVersion version;
 *     uint16 length;
 *     select (SecurityParameters.cipher_type) {
 *         case stream: GenericStreamCipher;
 *         case block:  GenericBlockCipher;
 *         case aead:   GenericAEADCipher;
 *     } fragment;
 * } TLSCiphertext;
 * 
 * stream-ciphered struct {
 *     opaque content[TLSCompressed.length];
 *     opaque MAC[SecurityParameters.mac_length];
 * } GenericStreamCipher;
 * 
 * struct {
 *     opaque IV[SecurityParameters.record_iv_length];
 *     block-ciphered struct {
 *         opaque content[TLSCompressed.length];
 *         opaque MAC[SecurityParameters.mac_length];
 *         uint8 padding[GenericBlockCipher.padding_length];
 *         uint8 padding_length;
 *     };
 * } GenericBlockCipher;
 * 
 * struct {
 *     opaque nonce_explicit[SecurityParameters.record_iv_length];
 *     aead-ciphered struct {
 *         opaque content[TLSCompressed.length];
 *     };
 * } GenericAEADCipher;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
abstract class Record extends TLS {

	/** TLSPlaintext 16K (2^14) */
	final static short PLAINTEXT_MAX = 16384;
	/** TLSCiphertext 16.25K */
	final static short CIPHERTEXT_MAX = PLAINTEXT_MAX + 256;

	// ContentType MAX(255)

	/** 1.3 */
	public final static byte INVALID = 0;
	/** 1.3 1.2 1.1 1.0 */
	public final static byte CHANGE_CIPHER_SPEC = 20;
	/** 1.3 1.2 1.1 1.0 */
	public final static byte ALERT = 21;
	/** 1.3 1.2 1.1 1.0 */
	public final static byte HANDSHAKE = 22;
	/** 1.3 1.2 1.1 1.0 */
	public final static byte APPLICATION_DATA = 23;
	/** 1.3 */
	public final static byte HEARTBEAT = 24;

	////////////////////////////////////////////////////////////////////////////////

	public abstract byte contentType();

	private short version = TLS.V12;

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