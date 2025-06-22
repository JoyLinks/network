/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
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
class EncryptedClientHello extends Extension {

	@Override
	public short type() {
		return ENCRYPTED_CLIENT_HELLO;
	}
}