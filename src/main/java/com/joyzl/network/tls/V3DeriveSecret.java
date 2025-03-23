package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

/**
 * TLS 1.3 密钥推导计划基础方法
 * 
 * @author ZhangXi 2025年1月9日
 */
class V3DeriveSecret extends HKDF {

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

	public V3DeriveSecret() {
	}

	public V3DeriveSecret(String digest, String hmac) throws Exception {
		digest(digest);
		hmac(hmac);
	}

	/**
	 * TLS 1.3 Derive Secret
	 */
	protected byte[] v13DeriveSecret(byte[] secret) throws Exception {
		return v13DeriveSecret(secret, DERIVED, hashEmpty());
	}

	/**
	 * TLS 1.3 Early Secret
	 */
	protected byte[] v13EarlySecret(byte[] PSK) throws Exception {
		return v13Extract(TLS.EMPTY_BYTES, PSK);
	}

	/**
	 * TLS 1.3 ext binder_key
	 */
	protected byte[] v13ExporterBinderKey(byte[] early) throws Exception {
		return v13DeriveSecret(early, EXT_BINDER, hashEmpty());
	}

	/**
	 * TLS 1.3 res binder_key
	 */
	protected byte[] v13ResumptionBinderKey(byte[] early) throws Exception {
		return v13DeriveSecret(early, RES_BINDER, hashEmpty());
	}

	/**
	 * TLS 1.3 client_early_traffic_secret
	 */
	protected byte[] v13ClientEarlyTrafficSecret(byte[] early, byte[] hash) throws Exception {
		return v13DeriveSecret(early, C_E_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 early_exporter_master_secret
	 */
	protected byte[] v13EarlyExporterMasterSecret(byte[] early, byte[] hash) throws Exception {
		return v13DeriveSecret(early, E_EXP_MASTER, hash);
	}

	/**
	 * TLS 1.3 Handshake Secret
	 */
	protected byte[] v13HandshakeSecret(byte[] early, byte[] shared) throws Exception {
		// Derive-Secret(., "derived", "")
		early = v13DeriveSecret(early, DERIVED, hashEmpty());
		// (EC)DHE -> HKDF-Extract = Handshake Secret
		return v13Extract(early, shared);
	}

	/**
	 * TLS 1.3 client_handshake_traffic_secret
	 */
	protected byte[] v13ClientHandshakeTrafficSecret(byte[] handshake, byte[] hash) throws Exception {
		return v13DeriveSecret(handshake, C_HS_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 server_handshake_traffic_secret
	 */
	protected byte[] v13ServerHandshakeTrafficSecret(byte[] handshake, byte[] hash) throws Exception {
		return v13DeriveSecret(handshake, S_HS_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 Master Secret
	 */
	protected byte[] v13MasterSecret(byte[] handshake) throws Exception {
		// Derive-Secret(., "derived", "")
		handshake = v13DeriveSecret(handshake, DERIVED, hashEmpty());
		// 0 -> HKDF-Extract = Master Secret
		return v13Extract(handshake, TLS.EMPTY_BYTES);
	}

	/**
	 * TLS 1.3 client_application_traffic_secret
	 */
	protected byte[] v13ClientApplicationTrafficSecret(byte[] master, byte[] hash) throws Exception {
		return v13DeriveSecret(master, C_AP_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 server_application_traffic_secret
	 */
	protected byte[] v13ServerApplicationTrafficSecret(byte[] master, byte[] hash) throws Exception {
		return v13DeriveSecret(master, S_AP_TRAFFIC, hash);
	}

	/**
	 * TLS 1.3 exporter_master_secret
	 */
	protected byte[] v13ExporterMasterSecret(byte[] master, byte[] hash) throws Exception {
		return v13DeriveSecret(master, EXP_MASTER, hash);
	}

	/**
	 * TLS 1.3 resumption_master_secret
	 */
	protected byte[] v13ResumptionMasterSecret(byte[] master, byte[] hash) throws Exception {
		return v13DeriveSecret(master, RES_MASTER, hash);
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
	protected byte[] v13WriteKey(byte[] traffic, int length) throws Exception {
		return v13ExpandLabel(traffic, KEY, TLS.EMPTY_BYTES, length);
	}

	/**
	 * TLS 1.3 *_traffic_secret -> [sender]_write_iv
	 */
	protected byte[] v13WriteIV(byte[] traffic, int length) throws Exception {
		return v13ExpandLabel(traffic, IV, TLS.EMPTY_BYTES, length);
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
	protected byte[] v13FinishedVerifyData(byte[] traffic, byte[] hash) throws Exception {
		// finished_key=HKDF-Expand-Label(BaseKey,"finished","",Hash.length)
		// verify_data=HMAC(finished_key,Transcript-Hash(Handshake-Context,Certificate*,CertificateVerify*))

		final byte[] finished_key = v13ExpandLabel(traffic, FINISHED, TLS.EMPTY_BYTES, hmacLength());
		return v13Extract(finished_key, hash);
	}

	final static byte[] EXPORTER = "exporter".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 <br>
	 * exporter_master_secret -> exporter<br>
	 * early_exporter_master_secret -> exporter<br>
	 */
	protected byte[] v13ExporterSecret(byte[] label, byte[] secret, byte[] hash, int length) throws Exception {
		// TLS-Exporter(label, context_value, key_length) =
		// HKDF-Expand-Label(Derive-Secret(Secret,label,""),"exporter",Hash(context_value),key_length)
		// RFC8446 RFC5705

		secret = v13DeriveSecret(secret, label, hashEmpty());
		return v13ExpandLabel(secret, EXPORTER, hash, length);
	}

	final static byte[] RESUMPTION = "resumption".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 resumption_master_secret -> resumption
	 */
	protected byte[] v13ResumptionSecret(byte[] secret, byte[] nonce) throws Exception {
		// HKDF-Expand-Label(resumption_master_secret,"resumption",ticket_nonce,Hash.length)

		return v13ExpandLabel(secret, RESUMPTION, nonce, hashLength());
	}

	/*-
	 * application_traffic_secret_N + 1 = HKDF-Expand-Label(application_traffic_secret_N,"traffic upd","",Hash.length)
	 */
	final static byte[] TRAFFIC_UPD = "traffic upd".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.3 application_traffic_secret_N + 1
	 */
	protected byte[] v13NextApplicationTrafficSecret(byte[] traffic) throws Exception {
		return v13ExpandLabel(traffic, TRAFFIC_UPD, TLS.EMPTY_BYTES, hashLength());
	}
}