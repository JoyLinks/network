package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *     opaque content[TLSPlaintext.length];
 *     ContentType type;
 *     uint8 zeros[length_of_padding];
 * } TLSInnerPlaintext;

 * struct {
 *     ContentType opaque_type = application_data; / 23 /
 *     ProtocolVersion legacy_record_version = 0x0303; / TLS v1.2 /
 *     uint16 length;
 *     opaque encrypted_record[TLSCiphertext.length];
 * } TLSCiphertext;
 * </pre>
 * 
 * @author ZhangXi 2024年12月20日
 */
public class ApplicationData extends TLSCiphertext {

	@Override
	public ContentType contentType() {
		return ContentType.APPLICATION_DATA;
	}
}