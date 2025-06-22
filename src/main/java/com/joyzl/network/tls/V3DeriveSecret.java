/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

/**
 * TLS 1.3 密钥推导计划基础方法
 * 
 * @author ZhangXi 2025年1月9日
 */
class V3DeriveSecret extends V3HKDF {

	/*-
	 * TLS 1.3 密钥导出
	 * 
	 * 	         0
	 * 	         v
	 * 	PSK ->  HKDF-Extract = Early Secret
	 * 	         +-----> Derive-Secret(., "ext binder" | "res binder", "") = binder_key
	 * 	         +-----> Derive-Secret(., "c e traffic", ClientHello)      = client_early_traffic_secret
	 * 	         +-----> Derive-Secret(., "e exp master", ClientHello)     = early_exporter_master_secret
	 * 	         v
	 * 	   Derive-Secret(., "derived", "")
	 * 	         v
	 * 	(EC)DHE -> HKDF-Extract = Handshake Secret
	 * 	         +-----> Derive-Secret(., "c hs traffic", ClientHello...ServerHello) = client_handshake_traffic_secret
	 * 	         +-----> Derive-Secret(., "s hs traffic", ClientHello...ServerHello) = server_handshake_traffic_secret
	 * 	         v
	 * 	   Derive-Secret(., "derived", "")
	 * 	         v
	 * 	0 -> HKDF-Extract = Master Secret
	 * 	         +-----> Derive-Secret(., "c ap traffic", ClientHello...server Finished) = client_application_traffic_secret_0
	 * 	         +-----> Derive-Secret(., "s ap traffic", ClientHello...server Finished) = server_application_traffic_secret_0
	 * 	         +-----> Derive-Secret(., "exp master",   ClientHello...server Finished) = exporter_master_secret
	 * 	         +-----> Derive-Secret(., "res master",   ClientHello...client Finished) = resumption_master_secret
	 */
	final static byte[] DERIVED = "derived".getBytes(StandardCharsets.US_ASCII);
	final static byte[] EXT_BINDER = "ext binder".getBytes(StandardCharsets.US_ASCII);
	final static byte[] RES_BINDER = "res binder".getBytes(StandardCharsets.US_ASCII);
	final static byte[] C_E_TRAFFIC = "c e traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] E_EXP_MASTER = "e exp master".getBytes(StandardCharsets.US_ASCII);
	final static byte[] C_HS_TRAFFIC = "c hs traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] S_HS_TRAFFIC = "s hs traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] C_AP_TRAFFIC = "c ap traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] S_AP_TRAFFIC = "s ap traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] EXP_MASTER = "exp master".getBytes(StandardCharsets.US_ASCII);
	final static byte[] RES_MASTER = "res master".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 Derive Secret
	 */
	protected byte[] deriveSecret(byte[] secret) throws Exception {
		return deriveSecret(secret, DERIVED, hashEmpty());
	}

	/**
	 * TLS 1.3 Early Secret
	 */
	protected byte[] earlySecret(byte[] PSK) throws Exception {
		return extract(TLS.EMPTY_BYTES, PSK);
	}

	/**
	 * TLS 1.3 ext binder_key
	 */
	protected byte[] exporterBinderKey(byte[] early) throws Exception {
		return deriveSecret(early, EXT_BINDER, hashEmpty());
	}

	/**
	 * TLS 1.3 res binder_key
	 */
	protected byte[] resumptionBinderKey(byte[] early) throws Exception {
		return deriveSecret(early, RES_BINDER, hashEmpty());
	}

	/**
	 * TLS 1.3 client_early_traffic_secret
	 */
	protected byte[] clientEarlyTrafficSecret(byte[] early, byte[] hash) throws Exception {
		return deriveSecret(early, C_E_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 early_exporter_master_secret
	 */
	protected byte[] earlyExporterMasterSecret(byte[] early, byte[] hash) throws Exception {
		return deriveSecret(early, E_EXP_MASTER, hash);
	}

	/**
	 * TLS 1.3 Handshake Secret
	 */
	protected byte[] handshakeSecret(byte[] early, byte[] shared) throws Exception {
		// Derive-Secret(., "derived", "")
		early = deriveSecret(early, DERIVED, hashEmpty());
		// (EC)DHE -> HKDF-Extract = Handshake Secret
		return extract(early, shared);
	}

	/**
	 * TLS 1.3 client_handshake_traffic_secret
	 */
	protected byte[] clientHandshakeTrafficSecret(byte[] handshake, byte[] hash) throws Exception {
		return deriveSecret(handshake, C_HS_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 server_handshake_traffic_secret
	 */
	protected byte[] serverHandshakeTrafficSecret(byte[] handshake, byte[] hash) throws Exception {
		return deriveSecret(handshake, S_HS_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 Master Secret
	 */
	protected byte[] masterSecret(byte[] handshake) throws Exception {
		// Derive-Secret(., "derived", "")
		handshake = deriveSecret(handshake, DERIVED, hashEmpty());
		// 0 -> HKDF-Extract = Master Secret
		return extract(handshake, TLS.EMPTY_BYTES);
	}

	/**
	 * TLS 1.3 client_application_traffic_secret
	 */
	protected byte[] clientApplicationTrafficSecret(byte[] master, byte[] hash) throws Exception {
		return deriveSecret(master, C_AP_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 server_application_traffic_secret
	 */
	protected byte[] serverApplicationTrafficSecret(byte[] master, byte[] hash) throws Exception {
		return deriveSecret(master, S_AP_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 exporter_master_secret
	 */
	protected byte[] exporterMasterSecret(byte[] master, byte[] hash) throws Exception {
		return deriveSecret(master, EXP_MASTER, hash);
	}

	/**
	 * TLS 1.3 resumption_master_secret
	 */
	protected byte[] resumptionMasterSecret(byte[] master, byte[] hash) throws Exception {
		return deriveSecret(master, RES_MASTER, hash);
	}

	/*-
	 * [sender]_write_key = HKDF-Expand-Label(Secret, "key", "", key_length)
	 * [sender]_write_iv  = HKDF-Expand-Label(Secret, "iv", "", iv_length)
	 * +-------------------+---------------------------------------+
	 * | Record Type       | Secret                                |
	 * +-------------------+---------------------------------------+
	 * | 0-RTT Application | client_early_traffic_secret           |
	 * |                   |                                       |
	 * | Handshake         | [sender]_handshake_traffic_secret     |
	 * |                   |                                       |
	 * | Application Data  | [sender]_application_traffic_secret_N |
	 * +-------------------+---------------------------------------+
	 */
	final static byte[] KEY = "key".getBytes(StandardCharsets.US_ASCII);
	final static byte[] IV = "iv".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 *_traffic_secret -> [sender]_write_key
	 */
	protected byte[] writeKey(byte[] traffic, int length) throws Exception {
		return expandLabel(traffic, KEY, TLS.EMPTY_BYTES, length);
	}

	/**
	 * TLS 1.3 *_traffic_secret -> [sender]_write_iv
	 */
	protected byte[] writeIV(byte[] traffic, int length) throws Exception {
		return expandLabel(traffic, IV, TLS.EMPTY_BYTES, length);
	}

	/*-
	 * +-----------+-------------------------+-----------------------------+
	 * | Mode      | Handshake Context       | Base Key                    |
	 * +-----------+-------------------------+-----------------------------+
	 * | Server    | ClientHello ... later   | server_handshake_traffic_   |
	 * |           | of EncryptedExtensions/ | secret                      |
	 * |           | CertificateRequest      |                             |
	 * |           |                         |                             |
	 * | Client    | ClientHello ... later   | client_handshake_traffic_   |
	 * |           | of server               | secret                      |
	 * |           | Finished/EndOfEarlyData |                             |
	 * |           |                         |                             |
	 * | Post-     | ClientHello ... client  | client_application_traffic_ |
	 * | Handshake | Finished +              | secret_N                    |
	 * |           | CertificateRequest      |                             |
	 * +-----------+-------------------------+-----------------------------+
	 */
	final static byte[] FINISHED = "finished".getBytes(StandardCharsets.US_ASCII);

	/** TLS 1.3 */
	protected byte[] finishedVerifyData(byte[] traffic, byte[] hash) throws Exception {
		// finished_key=HKDF-Expand-Label(BaseKey,"finished","",Hash.length)
		// verify_data=HMAC(finished_key,Transcript-Hash(Handshake-Context,Certificate*,CertificateVerify*))

		final byte[] finished_key = expandLabel(traffic, FINISHED, TLS.EMPTY_BYTES, hmacLength());
		return extract(finished_key, hash);
	}

	final static byte[] EXPORTER = "exporter".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 <br>
	 * exporter_master_secret -> exporter<br>
	 * early_exporter_master_secret -> exporter<br>
	 */
	protected byte[] exporterSecret(byte[] label, byte[] secret, byte[] hash, int length) throws Exception {
		// TLS-Exporter(label, context_value, key_length) =
		// HKDF-Expand-Label(Derive-Secret(Secret,label,""),"exporter",Hash(context_value),key_length)
		// RFC8446 RFC5705

		secret = deriveSecret(secret, label, hashEmpty());
		return expandLabel(secret, EXPORTER, hash, length);
	}

	final static byte[] RESUMPTION = "resumption".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 resumption_master_secret -> resumption
	 */
	protected byte[] resumptionSecret(byte[] secret, byte[] nonce) throws Exception {
		// HKDF-Expand-Label(resumption_master_secret,"resumption",ticket_nonce,Hash.length)

		return expandLabel(secret, RESUMPTION, nonce, hashLength());
	}

	/*-
	 * application_traffic_secret_N + 1 = HKDF-Expand-Label(application_traffic_secret_N,"traffic upd","",Hash.length)
	 */
	final static byte[] TRAFFIC_UPD = "traffic upd".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 application_traffic_secret_N + 1
	 */
	protected byte[] nextApplicationTrafficSecret(byte[] traffic) throws Exception {
		return expandLabel(traffic, TRAFFIC_UPD, TLS.EMPTY_BYTES, hashLength());
	}
}