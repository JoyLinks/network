package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *    ECHCipherSuite cipher_suite;
 *    opaque config_id<0..255>;
 *    opaque enc<1..2^16-1>;
 *    opaque payload<1..2^16-1>;
 * } ClientECH;
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
public class EncryptedClientHello extends Extension {

	@Override
	public short type() {
		return ENCRYPTED_CLIENT_HELLO;
	}
}