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
 * @author ZhangXi 2024年12月19日
 */
public abstract class Record {

	private ContentType type;
	private short version = TLS.V12;

	public final ContentType getType() {
		return type;
	}

	public final void setType(ContentType value) {
		type = value;
	}

	public final short getProtocolVersion() {
		return version;
	}

	public final void setProtocolVersion(short value) {
		version = value;
	}
}