package com.joyzl.network.tls;

/**
 * 应用数据（必须加密）
 * 
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
class ApplicationData extends Record {

	final static ApplicationData INSTANCE = new ApplicationData();

	private ApplicationData() {
		// 无须额外实例化此对象
		// 在记录层编码解码时包装或剥离
	}

	@Override
	public byte contentType() {
		return APPLICATION_DATA;
	}
}