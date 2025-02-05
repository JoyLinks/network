package com.joyzl.network.tls;

/**
 * 密码套件，支持的AEAD算法和HKDF哈希对，用于密钥导出计划和消息加密与解密
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
	 * 
	 * CipherSuite                      Is       Key          Cipher      Hash
	 *                              Exportable Exchange
	 * 
	 * TLS_NULL_WITH_NULL_NULL               * NULL           NULL        NULL
	 * TLS_RSA_WITH_NULL_MD5                 * RSA            NULL         MD5
	 * TLS_RSA_WITH_NULL_SHA                 * RSA            NULL         SHA
	 * TLS_RSA_EXPORT_WITH_RC4_40_MD5        * RSA_EXPORT     RC4_40       MD5
	 * TLS_RSA_WITH_RC4_128_MD5                RSA            RC4_128      MD5
	 * TLS_RSA_WITH_RC4_128_SHA                RSA            RC4_128      SHA
	 * TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5    * RSA_EXPORT     RC2_CBC_40   MD5
	 * TLS_RSA_WITH_IDEA_CBC_SHA               RSA            IDEA_CBC     SHA
	 * TLS_RSA_EXPORT_WITH_DES40_CBC_SHA     * RSA_EXPORT     DES40_CBC    SHA
	 * TLS_RSA_WITH_DES_CBC_SHA                RSA            DES_CBC      SHA
	 * TLS_RSA_WITH_3DES_EDE_CBC_SHA           RSA            3DES_EDE_CBC SHA
	 * TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA  * DH_DSS_EXPORT  DES40_CBC    SHA
	 * TLS_DH_DSS_WITH_DES_CBC_SHA             DH_DSS         DES_CBC      SHA
	 * TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA        DH_DSS         3DES_EDE_CBC SHA
	 * TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA  * DH_RSA_EXPORT  DES40_CBC    SHA
	 * TLS_DH_RSA_WITH_DES_CBC_SHA             DH_RSA         DES_CBC      SHA
	 * TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA        DH_RSA         3DES_EDE_CBC SHA
	 * TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA * DHE_DSS_EXPORT DES40_CBC    SHA
	 * TLS_DHE_DSS_WITH_DES_CBC_SHA            DHE_DSS        DES_CBC      SHA
	 * TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA       DHE_DSS        3DES_EDE_CBC SHA
	 * TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA * DHE_RSA_EXPORT DES40_CBC    SHA
	 * TLS_DHE_RSA_WITH_DES_CBC_SHA            DHE_RSA        DES_CBC      SHA
	 * TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA       DHE_RSA        3DES_EDE_CBC SHA
	 * TLS_DH_anon_EXPORT_WITH_RC4_40_MD5    * DH_anon_EXPORT RC4_40       MD5
	 * TLS_DH_anon_WITH_RC4_128_MD5            DH_anon        RC4_128      MD5
	 * TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA   DH_anon        DES40_CBC    SHA
	 * TLS_DH_anon_WITH_DES_CBC_SHA            DH_anon        DES_CBC      SHA
	 * TLS_DH_anon_WITH_3DES_EDE_CBC_SHA       DH_anon        3DES_EDE_CBC SHA
	 */

	/** v1.0 Key Exchange:NULL,Cipher:NULL,Hash:NULL */
	short TLS_NULL_WITH_NULL_NULL = 0x0000;

	/** v1.0 Key Exchange:RSA,Cipher:NULL,Hash:MD5 */
	short TLS_RSA_WITH_NULL_MD5 = 0x0001;
	/** v1.0 Key Exchange:RSA,Cipher:NULL,Hash:SHA */
	short TLS_RSA_WITH_NULL_SHA = 0x0002;
	/** v1.0 Key Exchange:RSA_EXPORT,Cipher:RC4_40,Hash:MD5 */
	short TLS_RSA_EXPORT_WITH_RC4_40_MD5 = 0x0003;
	/** v1.0 Key:RSA,Cipher:RC4_128,Hash:MD5 */
	short TLS_RSA_WITH_RC4_128_MD5 = 0x0004;
	/** v1.0 Key:RSA,Cipher:RC4_128,Hash:SHA */
	short TLS_RSA_WITH_RC4_128_SHA = 0x0005;
	/** v1.0 Key Exchange:RSA_EXPORT,Cipher:RC2_CBC_40,Hash:MD5 */
	short TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = 0x0006;
	/** v1.0 Key:RSA,Cipher:IDEA_CBC,Hash:SHA */
	short TLS_RSA_WITH_IDEA_CBC_SHA = 0x0007;
	/** v1.0 Key Exchange:RSA_EXPORT,Cipher:DES40_CBC,Hash:SHA */
	short TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0008;
	/** v1.0 Key Exchange:RSA,Cipher:DES_CBC,Hash:SHA */
	short TLS_RSA_WITH_DES_CBC_SHA = 0x0009;
	/** v1.0 Key Exchange:RSA,Cipher:3DES_EDE_CBC,Hash:SHA */
	short TLS_RSA_WITH_3DES_EDE_CBC_SHA = 0x000A;

	/** v1.0 Key Exchange:DH_DSS_EXPORT,Cipher:DES40_CBC,Hash:SHA */
	short TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x000B;
	/** v1.0 Key Exchange:DH_DSS,Cipher:DES_CBC,Hash:SHA */
	short TLS_DH_DSS_WITH_DES_CBC_SHA = 0x000C;
	/** v1.0 Key Exchange:DH_DSS,Cipher:3DES_EDE_CBC,Hash:SHA */
	short TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA = 0x000D;
	/** v1.0 Key Exchange:DH_RSA_EXPORT,Cipher:DES40_CBC,Hash:SHA */
	short TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x000E;
	/** v1.0 Key Exchange:DH_RSA,Cipher:DES_CBC,Hash:SHA */
	short TLS_DH_RSA_WITH_DES_CBC_SHA = 0x000F;
	/** v1.0 Key Exchange:DH_RSA,Cipher:3DES_EDE_CBC,Hash:SHA */
	short TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA = 0x0010;
	/** v1.0 Key Exchange:DHE_DSS_EXPORT,Cipher:DES40_CBC,Hash:SHA */
	short TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x0011;
	/** v1.0 Key Exchange:DHE_DSS,Cipher:DES_CBC,Hash:SHA */
	short TLS_DHE_DSS_WITH_DES_CBC_SHA = 0x0012;
	/** v1.0 Key Exchange:DHE_DSS,Cipher:3DES_EDE_CBC,Hash:SHA */
	short TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = 0x0013;
	/** v1.0 Key Exchange:DHE_RSA_EXPORT,Cipher:DES40_CBC,Hash:SHA */
	short TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0014;
	/** v1.0 Key Exchange:DHE_RSA,Cipher:DES_CBC,Hash:SHA */
	short TLS_DHE_RSA_WITH_DES_CBC_SHA = 0x0015;
	/** v1.0 Key Exchange:DHE_RSA,Cipher:3DES_EDE_CBC,Hash:SHA */
	short TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 0x0016;

	/** v1.0 Key Exchange:DH_ANON_EXPORT,Cipher:RC4_40,Hash:MD5 */
	short TLS_DH_ANON_EXPORT_WITH_RC4_40_MD5 = 0x0017;
	/** v1.0 Key Exchange:DH_ANON,Cipher:RC4_128,Hash:MD5 */
	short TLS_DH_ANON_WITH_RC4_128_MD5 = 0x0018;
	/** v1.0 Key Exchange:DH_ANON_EXPORT,Cipher:DES40_CBC,Hash:SHA */
	short TLS_DH_ANON_EXPORT_WITH_DES40_CBC_SHA = 0x0019;
	/** v1.0 Key Exchange:DH_ANON,Cipher:DES_CBC,Hash:SHA */
	short TLS_DH_ANON_WITH_DES_CBC_SHA = 0x001A;
	/** v1.0 Key Exchange:DH_ANON,Cipher:3DES_EDE_CBC,Hash:SHA */
	short TLS_DH_ANON_WITH_3DES_EDE_CBC_SHA = 0x001B;

	// v1.3

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