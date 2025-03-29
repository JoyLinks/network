package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

import com.joyzl.network.codec.Binary;

/**
 * TLS 1.0 密钥推导计划基础方法
 * 
 * @author ZhangXi 2025年1月9日
 */
class V0DeriveSecret extends V0PRF {

	/*-
	 * TLS 1.0 密钥导出
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

	/** preMasterSecret[48] */
	public static byte[] preMasterSecret(short version) {
		final byte[] secret = new byte[48];
		TLS.RANDOM.nextBytes(secret);
		Binary.put(secret, 0, version);
		return secret;
	}

	/** ClientHello/ServerHello Random[32] */
	public static byte[] helloRandom() {
		final byte[] random = new byte[32];
		TLS.RANDOM.nextBytes(random);
		Binary.put(random, 0, (int) System.currentTimeMillis());
		return random;
	}

	final static byte[] MASTER_SECRET = "master secret".getBytes(StandardCharsets.US_ASCII);
	final static byte[] EXTENDED_MASTER_SECRET = "extended master secret".getBytes(StandardCharsets.US_ASCII);

	/**
	 * RSA:preMasterSecret / Diffie-Hellman:PublicKey -> master_secret
	 */
	protected byte[] masterSecret(byte[] pms, byte[] clientRandom, byte[] serverRandom) throws Exception {
		return prf(pms, MASTER_SECRET, clientRandom, serverRandom, 48);
	}

	/**
	 * RSA:preMasterSecret / Diffie-Hellman:PublicKey -> master_secret
	 */
	protected byte[] masterSecret(byte[] pms, byte[] hash) throws Exception {
		return prf(pms, EXTENDED_MASTER_SECRET, hash, 48);
	}

	/*-
	 * enum { server, client } ConnectionEnd;
	 * enum { null, rc4, rc2, des, 3des, des40 } BulkCipherAlgorithm;
	 * enum { stream, block } CipherType;
	 * enum { true, false } IsExportable
	 * enum { null, md5, sha1} MACAlgorithm;
	 * enum { null(0), (255) } CompressionMethod;
	 * 
	 * struct {
	 *       ConnectionEnd          entity;
	 *       BulkCipherAlgorithm    bulk_cipher_algorithm;
	 *       CipherType             cipher_type;
	 *       uint8                  key_size;
	 *       uint8                  key_material_length;
	 *       IsExportable           is_exportable;
	 *       MACAlgorithm           mac_algorithm;
	 *       uint8                  hash_size;
	 *       CompressionMethod      compression_algorithm;
	 *       opaque                 master_secret[48];
	 *       opaque                 client_random[32];
	 *       opaque                 server_random[32];
	 * } SecurityParameters;
	 * 
	 * client write MAC secret
	 * server write MAC secret
	 * client write key
	 * server write key
	 * client write IV (for block ciphers only)
	 * server write IV (for block ciphers only)
	 * 
	 * key_block = PRF(SecurityParameters.master_secret, "key expansion",
	 *                 SecurityParameters.server_random +
	 *                 SecurityParameters.client_random);
	 * 
	 * client_write_MAC_secret[SecurityParameters.hash_size]
	 * server_write_MAC_secret[SecurityParameters.hash_size]
	 * client_write_key[SecurityParameters.key_material_length]
	 * server_write_key[SecurityParameters.key_material_length]
	 * client_write_IV[SecurityParameters.IV_size]
	 * server_write_IV[SecurityParameters.IV_size]
	 */

	final static byte[] KEY_EXPANSION = "key expansion".getBytes(StandardCharsets.US_ASCII);

	/** master_secret -> key_block */
	protected byte[] keyBlock(byte[] master, byte[] serverRandom, byte[] clientRandom, int length) throws Exception {
		return prf(master, KEY_EXPANSION, serverRandom, clientRandom, length);
	}

	/** client_write_MAC_secret */
	protected byte[] clientWriteMACKey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, 0, key, 0, length);
		return key;
	}

	/** server_write_MAC_secret */
	protected byte[] serverWriteMACKey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, length, key, 0, length);
		return key;
	}

	/** client_write_key */
	protected byte[] clientWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2, key, 0, length);
		return key;
	}

	/** server_write_key */
	protected byte[] serverWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + length, key, 0, length);
		return key;
	}

	/** client_write_IV */
	protected byte[] clientWriteIV(byte[] block, int macLength, int keyLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + keyLength * 2, key, 0, length);
		return key;
	}

	/** server_write_IV */
	protected byte[] serverWriteIV(byte[] block, int macLength, int keyLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + keyLength * 2 + length, key, 0, length);
		return key;
	}

	/*-
	 * Exportable encryption algorithms
	 * 
	 * final_client_write_key =
	 * PRF(SecurityParameters.client_write_key, "client write key",
	 *     SecurityParameters.client_random +
	 *     SecurityParameters.server_random);
	 * 
	 * final_server_write_key =
	 * PRF(SecurityParameters.server_write_key, "server write key",
	 *     SecurityParameters.client_random +
	 *     SecurityParameters.server_random);
	 * 
	 * iv_block = PRF("", "IV block", SecurityParameters.client_random +
	 *                                SecurityParameters.server_random);
	 * client_write_IV[SecurityParameters.IV_size]
	 * server_write_IV[SecurityParameters.IV_size]
	 */

	final static byte[] CLIENT_WRITE_KEY = "client write key".getBytes(StandardCharsets.US_ASCII);
	final static byte[] SERVER_WRITE_KEY = "server write key".getBytes(StandardCharsets.US_ASCII);
	final static byte[] IV_BLOCK = "IV block".getBytes(StandardCharsets.US_ASCII);

	protected byte[] finalClientWriteKey(byte[] client_write_key, byte[] clientRandom, byte[] serverRandom, int length) throws Exception {
		return prf(client_write_key, CLIENT_WRITE_KEY, clientRandom, serverRandom, length);
	}

	protected byte[] finalServerWriteKey(byte[] server_write_key, byte[] clientRandom, byte[] serverRandom, int length) throws Exception {
		return prf(server_write_key, SERVER_WRITE_KEY, clientRandom, serverRandom, length);
	}

	protected byte[] ivBlock(byte[] clientRandom, byte[] serverRandom, int length) throws Exception {
		return prf(TLS.EMPTY_BYTES, IV_BLOCK, clientRandom, serverRandom, length);
	}

	/*-
	 * PRF(master_secret, finished_label, MD5(handshake_messages) + SHA-1(handshake_messages)) [0..11];
	 * 
	 * CLIENT: Hash(ClientHello...) -> client Finished
	 * SERVER: Hash(ClientHello... client Finished) -> server Finished
	 */

	final static byte[] SERVER_FINISHED = "server finished".getBytes(StandardCharsets.US_ASCII);
	final static byte[] CLIENT_FINISHED = "client finished".getBytes(StandardCharsets.US_ASCII);

	/** master_secret -> verify_data */
	protected byte[] serverFinished(byte[] master, byte[] md5, byte[] sha) throws Exception {
		return prf(master, SERVER_FINISHED, md5, sha, 12);
	}

	/** master_secret -> verify_data */
	protected byte[] clientFinished(byte[] master, byte[] md5, byte[] sha) throws Exception {
		return prf(master, CLIENT_FINISHED, md5, sha, 12);
	}
}