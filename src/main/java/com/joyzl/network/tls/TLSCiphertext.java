package com.joyzl.network.tls;

/**
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
public abstract class TLSCiphertext extends Record {
	// TLS密文Record
}