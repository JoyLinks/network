package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

/**
 * TLS密钥推导计划
 * 
 * @author ZhangXi 2025年1月9日
 */
public class Secrets extends HKDF {

	// RFC8448 Example Handshake Traces for TLS 1.3

	public Secrets(short code) throws Exception {
		super(code);
	}

	/*-
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
	 * 
	 * [sender]_write_key = HKDF-Expand-Label(Secret, "key", "", key_length)
	 * [sender]_write_iv  = HKDF-Expand-Label(Secret, "iv", "", iv_length)
	 * 
	 */

	final static byte[] LABEL_EXT_BINDER = "ext binder".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_RES_BINDER = "res binder".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_C_E_TRAFFIC = "c e traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_DERIVED = "derived".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_E_EXP_MASTER = "e exp master".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_C_HS_TRAFFIC = "c hs traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_S_HS_TRAFFIC = "s hs traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_C_AP_TRAFFIC = "c ap traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_S_AP_TRAFFIC = "s ap traffic".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_EXP_MASTER = "exp master".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_RES_MASTER = "res master".getBytes(StandardCharsets.US_ASCII);

	public byte[] derived(byte[] secret) throws Exception {
		return deriveSecret(secret, LABEL_DERIVED, hashEmpty());
	}

	public byte[] handshake(byte[] secret, byte[] shared) throws Exception {
		return extract(secret, shared);
	}

	public byte[] clientHandshakeTraffic(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_C_HS_TRAFFIC, hash);
	}

	public byte[] serverHandshakeTraffic(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_S_HS_TRAFFIC, hash);
	}

	final static byte[] LABEL_KEY = "key".getBytes(StandardCharsets.US_ASCII);
	final static byte[] LABEL_IV = "iv".getBytes(StandardCharsets.US_ASCII);
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

	public byte[] key(byte[] secret) throws Exception {
		return expandLabel(secret, LABEL_KEY, TLS.EMPTY_BYTES, keyLength());
	}

	public byte[] iv(byte[] secret) throws Exception {
		return expandLabel(secret, LABEL_IV, TLS.EMPTY_BYTES, ivLength());
	}

	public byte[] clientEarlyTraffic(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_C_E_TRAFFIC, hash);
	}

	public byte[] clientApplicationTraffic(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_C_AP_TRAFFIC, hash);
	}

	public byte[] serverApplicationTraffic(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_S_AP_TRAFFIC, hash);
	}

	public byte[] exporterMaster(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_EXP_MASTER, hash);
	}

	public byte[] earlyExporterMaster(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_E_EXP_MASTER, hash);
	}

	public byte[] resumptionMaster(byte[] secret, byte[] hash) throws Exception {
		return deriveSecret(secret, LABEL_RES_MASTER, hash);
	}

	final static byte[] LABEL_TRAFFIC_UPD = "traffic upd".getBytes(StandardCharsets.US_ASCII);
	/*-
	 * application_traffic_secret_N+1=HKDF-Expand-Label(application_traffic_secret_N,"traffic upd","",Hash.length)
	 */

	public byte[] nextApplicationTraffic(byte[] secret) throws Exception {
		return expandLabel(secret, LABEL_TRAFFIC_UPD, TLS.EMPTY_BYTES, hashLength());
	}

	final static byte[] LABEL_FINISHED = "finished".getBytes(StandardCharsets.US_ASCII);
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

	public byte[] finishedVerifyData(byte[] secret, byte[] hash) throws Exception {
		// finished_key=HKDF-Expand-Label(BaseKey,"finished","",Hash.length)
		// verify_data=HMAC(finished_key,Transcript-Hash(Handshake-Context,Certificate*,CertificateVerify*))

		final byte[] finished_key = expandLabel(secret, LABEL_FINISHED, TLS.EMPTY_BYTES, hashLength());
		return extract(finished_key, hash);
	}

	final static byte[] LABEL_RESUMPTION = "resumption".getBytes(StandardCharsets.US_ASCII);

	public byte[] resumption(byte[] secret, byte[] nonce) throws Exception {
		// HKDF-Expand-Label(resumption_master_secret,"resumption",ticket_nonce,Hash.length)
		return expandLabel(secret, LABEL_RESUMPTION, nonce, hashLength());
	}
}