package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

import com.joyzl.network.codec.Binary;

/**
 * TLS 1.2 1.1. 1.0 密钥导出
 * 
 * @author ZhangXi 2025年3月9日
 */
public class MasterSecret extends PRF {

	/*-
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

	/** preMasterSecret */
	public byte[] pre(short version) {
		final byte[] secret = new byte[48];
		TLS.RANDOM.nextBytes(secret);
		Binary.put(secret, 0, version);
		return secret;
	}

	/** ClientHello/ServerHello Random */
	public byte[] random() {
		final byte[] random = new byte[32];
		TLS.RANDOM.nextBytes(random);
		Binary.put(random, 0, (int) System.currentTimeMillis());
		return random;
	}

	final static byte[] MASTER_SECRET = "master secret".getBytes(StandardCharsets.US_ASCII);
	final static byte[] EXTENDED_MASTER_SECRET = "extended master secret".getBytes(StandardCharsets.US_ASCII);

	/** RSA:preMasterSecret / Diffie-Hellman:PublicKey -> master_secret */
	public byte[] master(byte[] pms, byte[] clientRandom, byte[] serverRandom) throws Exception {
		return expandLabel(pms, MASTER_SECRET, clientRandom, serverRandom, 48);
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

	/** master_secret -> key_block */
	public byte[] keyBlock(byte[] secret, byte[] serverRandom, byte[] clientRandom, int length) throws Exception {
		return expandLabel(secret, KEY_EXPANSION, serverRandom, clientRandom, length);
	}

	/** client_write_MAC_key */
	public byte[] clientWriteMACkey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, 0, key, 0, length);
		return key;
	}

	/** server_write_MAC_key */
	public byte[] serverWriteMACkey(byte[] block, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, length, key, 0, length);
		return key;
	}

	/** client_write_key */
	public byte[] clientWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2, key, 0, length);
		return key;
	}

	/** server_write_key */
	public byte[] serverWriteKey(byte[] block, int macLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + length, key, 0, length);
		return key;
	}

	/** client_write_IV */
	public byte[] clientWriteIV(byte[] block, int macLength, int keyLength, int length) {
		final byte[] key = new byte[length];
		System.arraycopy(block, macLength * 2 + keyLength * 2, key, 0, length);
		return key;
	}

	/** server_write_IV */
	public byte[] serverWriteIV(byte[] block, int macLength, int keyLength, int length) {
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

	/** master_secret -> verify_data */
	public byte[] serverFinished(byte[] secret, byte[] hash, int length) throws Exception {
		return expandLabel(secret, SERVER_FINISHED, hash, length);
	}

	/** master_secret -> verify_data */
	public byte[] clientFinished(byte[] secret, byte[] hash, int length) throws Exception {
		return expandLabel(secret, CLIENT_FINISHED, hash, length);
	}
}