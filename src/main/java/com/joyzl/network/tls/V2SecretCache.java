/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 具有状态的密钥处理类
 * 
 * @author ZhangXi 2024年12月30日
 */
class V2SecretCache extends V2DeriveSecret {

	/*-
	 * TLS 1.2 握手与加解密切换
	 * 
	 * Client-----------------------------------------------Server
	 * ClientHello               -------->
	 *                                                 ServerHello
	 *                                                Certificate*
	 *                                          ServerKeyExchange*
	 *                                         CertificateRequest*
	 *                           <--------         ServerHelloDone
	 * Certificate*
	 * ClientKeyExchange
	 * CertificateVerify*
	 * [ChangeCipherSpec]
	 * Finished                  -------->
	 *                                          [ChangeCipherSpec]
	 *                           <--------                Finished
	 * Application Data          <------->        Application Data
	 * 
	 * -------------------------Session ID------------------------
	 * ClientHello                ------->
	 *                                                 ServerHello
	 *                                          [ChangeCipherSpec]
	 *                           <--------                Finished
	 * [ChangeCipherSpec]
	 * Finished                  -------->
	 * Application Data          <------->        Application Data
	 * -----------------------Session Ticket----------------------
	 * ClientHello
	 * (empty SessionTicket extension)-------->
	 *                                                 ServerHello
	 *                             (empty SessionTicket extension)
	 *                                                Certificate*
	 *                                          ServerKeyExchange*
	 *                                         CertificateRequest*
	 *                              <--------      ServerHelloDone
	 * Certificate*
	 * ClientKeyExchange
	 * CertificateVerify*
	 * [ChangeCipherSpec]
	 * Finished                     -------->
	 * 											  NewSessionTicket
	 *                                          [ChangeCipherSpec]
	 *                              <--------             Finished
	 * Application Data             <------->     Application Data
	 */

	private byte[] pms;
	private byte[] master;
	private byte[] block;
	private byte[] clientRandom, serverRandom;

	public byte[] pms() {
		return pms;
	}

	public void pms(byte[] value) {
		master = null;
		block = null;
		pms = value;
	}

	public byte[] master() {
		return master;
	}

	public void master(byte[] value) {
		master = value;
		block = null;
		pms = null;
	}

	public void initialize(CipherSuiteType type) throws Exception {
		// prf_tls12_sha256:默认是SHA256算法(这是能满足最低安全的算法)
		// prf_tls12_sha384:如果加密套件指定的HMAC算法安全级别高于SHA256，则采用加密基元SHA384算法

		// TranscriptHash
		// extended_master_secret:session_hash
		// 扩展密钥使用至少SHA256(RFC7627)

		// PRF:所有密码套件至少使用SHA256
		// PRF:如果密码套件指定的算法安全级别高于SHA256，则采用SHA384算法
		// SHA-1 160 位 20 字节
		// MD5 128 位 16 字节
		if (type.hash() < 32) {
			initialize("HmacSHA256", "SHA-256");
		} else {
			initialize(type.macAlgorithm(), type.digestAlgorithm());
		}
	}

	/** RSA(pms) / Diffie-Hellman(key) */
	public byte[] masterSecret() throws Exception {
		return master = super.masterSecret(pms, clientRandom, serverRandom);
	}

	/** RSA(pms) / Diffie-Hellman(key) */
	public byte[] extendedMasterSecret() throws Exception {
		return master = super.masterSecret(pms, hash());
	}

	public boolean hasBlock() {
		return block != null;
	}

	public byte[] block() {
		return block;
	}

	/** key_block */
	public byte[] keyBlock(CipherSuiteType type) throws Exception {
		return block = keyBlock(master, serverRandom, clientRandom, keyBlockLength(type));
	}

	public int keyBlockLength(CipherSuiteType type) {
		// client_write_MAC_key [SecurityParameters.mac_key_length]
		// server_write_MAC_key [SecurityParameters.mac_key_length]
		// client_write_key [SecurityParameters.enc_key_length]
		// server_write_key [SecurityParameters.enc_key_length]
		// client_write_IV [SecurityParameters.fixed_iv_length]
		// server_write_IV [SecurityParameters.fixed_iv_length]

		// AEAD
		// fixed_iv_length + record_iv_legnth:8
		if (type.isAEAD()) {
			return type.hash() * 2 + type.key() * 2 + (type.iv() - 8) * 2;
		}

		// Stream
		// CBC:Block
		// SecurityParameters.record_iv_length=SecurityParameters.block_size
		return type.hash() * 2 + type.key() * 2 + type.iv() * 2;
	}

	/** client_write_MAC_key */
	public byte[] clientWriteMACKey(CipherSuiteType type) {
		return clientWriteMACKey(block, type.hash());
	}

	/** server_write_MAC_key */
	public byte[] serverWriteMACKey(CipherSuiteType type) {
		return serverWriteMACKey(block, type.hash());
	}

	/** client_write_key */
	public byte[] clientWriteKey(CipherSuiteType type) {
		return clientWriteKey(block, type.hash(), type.key());
	}

	/** server_write_key */
	public byte[] serverWriteKey(CipherSuiteType type) {
		return serverWriteKey(block, type.hash(), type.key());
	}

	/** client_write_IV */
	public byte[] clientWriteIV(CipherSuiteType type) {
		if (type.isAEAD()) {
			// fixed_iv_length+record_iv_legnth:8
			// 导出的iv位于数组前部，后部分由加解密时填充序号
			final byte[] iv = new byte[type.iv()];
			System.arraycopy(block, type.hash() * 2 + type.key() * 2, iv, 0, type.iv() - 8);
			return iv;
		}
		return clientWriteIV(block, type.hash(), type.key(), type.iv());
	}

	/** server_write_IV */
	public byte[] serverWriteIV(CipherSuiteType type) {
		if (type.isAEAD()) {
			// fixed_iv_length+record_iv_legnth:8
			// 导出的iv位于数组前部，后部分由加解密时填充序号
			final byte[] iv = new byte[type.iv()];
			System.arraycopy(block, type.hash() * 2 + type.key() * 2 + type.iv() - 8, iv, 0, type.iv() - 8);
			return iv;
		}
		return serverWriteIV(block, type.hash(), type.key(), type.iv());
	}

	/** master_secret -> verify_data */
	protected byte[] serverFinished() throws Exception {
		return serverFinished(master, hash());
	}

	/** master_secret -> verify_data */
	protected byte[] clientFinished() throws Exception {
		return clientFinished(master, hash());
	}

	public void serverRandom(byte[] value) {
		serverRandom = value;
	}

	public void clientRandom(byte[] value) {
		clientRandom = value;
	}
}