package com.joyzl.network.tls;

public enum KeyExchange {
	PSK,
	/** 1.0 1.2 */
	RSA,
	/** 1.0 */
	RSA_EXPORT,
	/** 1.0 1.2 */
	DHE_DSS,
	/** 1.0 */
	DHE_DSS_EXPORT,
	/** 1.0 1.2 */
	DHE_RSA,
	/** 1.0 */
	DHE_RSA_EXPORT,
	/** 1.0 1.2 */
	DH_DSS,
	/** 1.0 1.2 */
	DH_RSA,
	/** 1.0 */
	DH_ANON,
	/** 1.0 */
	DHE_PSK,
	PSK_DHE,
	/** 1.0 */
	KRB5,
	/** 1.2 */
	RSA_PSK,
	/** 1.2 */
	ECDHE_RSA,
	/** 1.2 */
	ECDH_ECDSA,
	/** 1.2 */
	ECDH_RSA,
	/** 1.2 */
	ECDHE_ECDSA,
	ECDH_ANON,
	ECDHE_PSK,
	SRP_SHA,
	SRP_SHA_RSA,
	SRP_SHA_DSS
}