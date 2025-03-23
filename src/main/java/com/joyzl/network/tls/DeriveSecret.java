package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

import com.joyzl.network.codec.Binary;

/**
 * TLS 1.3 密钥推导计划基础方法
 * 
 * @author ZhangXi 2025年1月9日
 */
class DeriveSecret extends PRF_HKDF {

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

	public DeriveSecret() {
	}

	public DeriveSecret(String digest, String hmac) throws Exception {
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

	////////////////////////////////////////////////////////////////////////////////

	/*-
	 * TLS 1.2 密钥导出
	 * 
	 * struct {
	 *       uint8 major;
	 *       uint8 minor;
	 * } ProtocolVersion;
	 * 
	 * struct {
	 *       uint32 gmt_unix_time;
	 *       opaque random_bytes[28];
	 * } Random;
	 * 
	 * struct {
	 *       ProtocolVersion client_version;
	 *       opaque random[46];
	 * } PreMasterSecret;
	 * 
	 * master_secret[0..47] = PRF(pre_master_secret, "master secret", ClientHello.random + ServerHello.random);
	 * master_secret[0..47] = PRF(pre_master_secret, "extended master secret", session_hash);
	 */

	/** TLS 1.2 preMasterSecret[48] */
	public static byte[] v12PreMasterSecret(short version) {
		final byte[] secret = new byte[48];
		TLS.RANDOM.nextBytes(secret);
		Binary.put(secret, 0, version);
		return secret;
	}

	/** TLS 1.2 ClientHello/ServerHello Random[32] */
	public static byte[] v12HelloRandom() {
		final byte[] random = new byte[32];
		TLS.RANDOM.nextBytes(random);
		Binary.put(random, 0, (int) System.currentTimeMillis());
		return random;
	}

	final static byte[] MASTER_SECRET = "master secret".getBytes(StandardCharsets.US_ASCII);
	final static byte[] EXTENDED_MASTER_SECRET = "extended master secret".getBytes(StandardCharsets.US_ASCII);

	/**
	 * TLS 1.2 RSA:preMasterSecret / Diffie-Hellman:PublicKey -> master_secret
	 */
	protected byte[] v12MasterSecret(byte[] pms, byte[] clientRandom, byte[] serverRandom) throws Exception {
		return v12PRF(pms, MASTER_SECRET, clientRandom, serverRandom, 48);
	}

	/**
	 * TLS 1.2 RSA:preMasterSecret / Diffie-Hellman:PublicKey -> master_secret
	 */
	protected byte[] v12MasterSecret(byte[] pms, byte[] hash) throws Exception {
		return v12PRF(pms, EXTENDED_MASTER_SECRET, hash, 48);
	}

	/*-
	 * enum { null(0), (255) } CompressionMethod;
	 * enum { server, client } ConnectionEnd;
	 * enum { tls_prf_sha256 } PRFAlgorithm;
	 * enum { null, rc4, 3des, aes } BulkCipherAlgorithm;
	 * enum { stream, block, aead } CipherType;
	 * enum { null, hmac_md5, hmac_sha1, hmac_sha256, hmac_sha384, hmac_sha512} MACAlgorithm;
	 * 
	 * struct {
	 *       ConnectionEnd          entity;
	 *       PRFAlgorithm           prf_algorithm;
	 *       BulkCipherAlgorithm    bulk_cipher_algorithm;
	 *       CipherType             cipher_type;
	 *       uint8                  enc_key_length;
	 *       uint8                  block_length;
	 *       uint8                  fixed_iv_length;
	 *       uint8                  record_iv_length;
	 *       MACAlgorithm           mac_algorithm;
	 *       uint8                  mac_length;
	 *       uint8                  mac_key_length;
	 *       CompressionMethod      compression_algorithm;
	 *       opaque                 master_secret[48];
	 *       opaque                 client_random[32];
	 *       opaque                 server_random[32];
	 * } SecurityParameters;
	 * 
	 * key_block = PRF(SecurityParameters.master_secret,"key expansion",
	 *                 SecurityParameters.server_random +
	 *                 SecurityParameters.client_random);
	 * 
	 * client_write_MAC_key [SecurityParameters.mac_key_length]
	 * server_write_MAC_key [SecurityParameters.mac_key_length]
	 * client_write_key     [SecurityParameters.enc_key_length]
	 * server_write_key     [SecurityParameters.enc_key_length]
	 * client_write_IV      [SecurityParameters.fixed_iv_length]
	 * server_write_IV      [SecurityParameters.fixed_iv_length]
	 */

	final static byte[] KEY_EXPANSION = "key expansion".getBytes(StandardCharsets.US_ASCII);

	/** TLS 1.2 master_secret -> key_block */
	protected byte[] v12KeyBlock(byte[] master, byte[] serverRandom, byte[] clientRandom, int length) throws Exception {
		return v12PRF(master, KEY_EXPANSION, serverRandom, clientRandom, length);
	}

	/** TLS 1.2 client_write_MAC_key */
	protected byte[] v12ClientWriteMACkey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, 0, key, 0, length);
		return key;
	}

	/** TLS 1.2 server_write_MAC_key */
	protected byte[] v12ServerWriteMACkey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, length, key, 0, length);
		return key;
	}

	/** TLS 1.2 client_write_key */
	protected byte[] v12ClientWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2, key, 0, length);
		return key;
	}

	/** TLS 1.2 server_write_key */
	protected byte[] v12ServerWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + length, key, 0, length);
		return key;
	}

	/** TLS 1.2 client_write_IV */
	protected byte[] v12ClientWriteIV(byte[] block, int macLength, int keyLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + keyLength * 2, key, 0, length);
		return key;
	}

	/** TLS 1.2 server_write_IV */
	protected byte[] v12ServerWriteIV(byte[] block, int macLength, int keyLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + keyLength * 2 + length, key, 0, length);
		return key;
	}

	/*-
	 * verify_data [0..verify_data_length-1] = PRF(master_secret, finished_label, Hash(handshake_messages));
	 * 
	 * CLIENT: Hash(ClientHello...) -> client Finished
	 * SERVER: Hash(ClientHello... client Finished) -> server Finished
	 */

	final static byte[] SERVER_FINISHED = "server finished".getBytes(StandardCharsets.US_ASCII);
	final static byte[] CLIENT_FINISHED = "client finished".getBytes(StandardCharsets.US_ASCII);

	/** TLS 1.2 master_secret -> verify_data */
	protected byte[] v12ServerFinished(byte[] master, byte[] hash) throws Exception {
		return v12PRF(master, SERVER_FINISHED, hash, hmacLength());
	}

	/** TLS 1.2 master_secret -> verify_data */
	protected byte[] v12ClientFinished(byte[] master, byte[] hash) throws Exception {
		return v12PRF(master, CLIENT_FINISHED, hash, hmacLength());
	}
}