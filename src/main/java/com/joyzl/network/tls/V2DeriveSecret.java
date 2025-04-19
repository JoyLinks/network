package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

import com.joyzl.network.codec.Binary;

/**
 * TLS 1.2 密钥推导计划基础方法
 * 
 * @author ZhangXi 2025年1月9日
 */
class V2DeriveSecret extends V2PRF {

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
	public static byte[] preMasterSecret(short version) {
		final byte[] secret = new byte[48];
		TLS.RANDOM.nextBytes(secret);
		Binary.put(secret, 0, version);
		return secret;
	}

	/** TLS 1.2 ClientHello/ServerHello Random[32] */
	public static byte[] helloRandom() {
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
	protected byte[] masterSecret(byte[] pms, byte[] clientRandom, byte[] serverRandom) throws Exception {
		return prf(pms, MASTER_SECRET, clientRandom, serverRandom, 48);
	}

	/**
	 * TLS 1.2 RSA:preMasterSecret / Diffie-Hellman:PublicKey -> master_secret
	 */
	protected byte[] masterSecret(byte[] pms, byte[] hash) throws Exception {
		return prf(pms, EXTENDED_MASTER_SECRET, hash, 48);
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
	protected byte[] keyBlock(byte[] master, byte[] serverRandom, byte[] clientRandom, int length) throws Exception {
		return prf(master, KEY_EXPANSION, serverRandom, clientRandom, length);
	}

	/** TLS 1.2 client_write_MAC_key */
	protected byte[] clientWriteMACKey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, 0, key, 0, length);
		return key;
	}

	/** TLS 1.2 server_write_MAC_key */
	protected byte[] serverWriteMACKey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, length, key, 0, length);
		return key;
	}

	/** TLS 1.2 client_write_key */
	protected byte[] clientWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2, key, 0, length);
		return key;
	}

	/** TLS 1.2 server_write_key */
	protected byte[] serverWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + length, key, 0, length);
		return key;
	}

	/** TLS 1.2 client_write_IV */
	protected byte[] clientWriteIV(byte[] block, int macLength, int keyLength, int length) {
		final byte[] iv = new byte[length];
		System.arraycopy(block, macLength * 2 + keyLength * 2, iv, 0, length);
		return iv;
	}

	/** TLS 1.2 server_write_IV */
	protected byte[] serverWriteIV(byte[] block, int macLength, int keyLength, int length) {
		final byte[] iv = new byte[length];
		System.arraycopy(block, macLength * 2 + keyLength * 2 + length, iv, 0, length);
		return iv;
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
	protected byte[] serverFinished(byte[] master, byte[] hash) throws Exception {
		return prf(master, SERVER_FINISHED, hash, 12);
	}

	/** TLS 1.2 master_secret -> verify_data */
	protected byte[] clientFinished(byte[] master, byte[] hash) throws Exception {
		return prf(master, CLIENT_FINISHED, hash, 12);
	}
}