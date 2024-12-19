package com.joyzl.network.tls;

/**
 * 密码套件
 * 
 * @author ZhangXi 2024年12月17日
 */
public interface CipherSuite {
	/*-
	 * CipherSuite TLS_AEAD_HASH = VALUE;
	 * +-----------+------------------------------------------------+
	 * | Component | Contents                                       |
	 * +-----------+------------------------------------------------+
	 * | TLS       | The string "TLS"                               |
	 * | AEAD      | The AEAD algorithm used for record protection  |
	 * | HASH      | The hash algorithm used with HKDF              |
	 * | VALUE     | The two-byte ID assigned for this cipher suite |
	 * +-----------+------------------------------------------------+
	 */

	short TLS_NULL_WITH_NULL_NULL = 0x0000;

	short TLS_RSA_WITH_NULL_MD5 = 0x0001;
	short TLS_RSA_WITH_NULL_SHA = 0x0002;
	short TLS_RSA_EXPORT_WITH_RC4_40_MD5 = 0x0003;
	short TLS_RSA_WITH_RC4_128_MD5 = 0x0004;
	short TLS_RSA_WITH_RC4_128_SHA = 0x0005;
	short TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = 0x0006;
	short TLS_RSA_WITH_IDEA_CBC_SHA = 0x0007;
	short TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0008;
	short TLS_RSA_WITH_DES_CBC_SHA = 0x0009;
	short TLS_RSA_WITH_3DES_EDE_CBC_SHA = 0x000A;

	short TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x000B;
	short TLS_DH_DSS_WITH_DES_CBC_SHA = 0x000C;
	short TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA = 0x000D;
	short TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x000E;
	short TLS_DH_RSA_WITH_DES_CBC_SHA = 0x000F;
	short TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA = 0x0010;
	short TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x0011;
	short TLS_DHE_DSS_WITH_DES_CBC_SHA = 0x0012;
	short TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = 0x0013;
	short TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0014;
	short TLS_DHE_RSA_WITH_DES_CBC_SHA = 0x0015;
	short TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 0x0016;

	short TLS_DH_ANON_EXPORT_WITH_RC4_40_MD5 = 0x0017;
	short TLS_DH_ANON_WITH_RC4_128_MD5 = 0x0018;
	short TLS_DH_ANON_EXPORT_WITH_DES40_CBC_SHA = 0x0019;
	short TLS_DH_ANON_WITH_DES_CBC_SHA = 0x001A;
	short TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA = 0x001B;

	// 1.3

	short TLS_AES_128_GCM_SHA256 = 0x1301;
	short TLS_AES_256_GCM_SHA384 = 0x1302;
	short TLS_CHACHA20_POLY1305_SHA256 = 0x1303;
	short TLS_AES_128_CCM_SHA256 = 0x1304;
	short TLS_AES_128_CCM_8_SHA256 = 0x1305;

	short[] V13 = new short[] { //
			TLS_AES_128_GCM_SHA256, //
			TLS_AES_256_GCM_SHA384, //
			TLS_CHACHA20_POLY1305_SHA256, //
			TLS_AES_128_CCM_SHA256, //
			TLS_AES_128_CCM_8_SHA256//
	};
}