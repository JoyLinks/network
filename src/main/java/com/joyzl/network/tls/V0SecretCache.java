package com.joyzl.network.tls;

/**
 * TLS 1.3 具有状态的密钥处理类
 * 
 * @author ZhangXi 2024年12月30日
 */
class V0SecretCache extends V0DeriveSecret {

	/*-
	 * TLS 1.0 握手与加解密切换
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

	/** RSA(pms) / Diffie-Hellman(key) */
	public byte[] masterSecret() throws Exception {
		return master = super.masterSecret(pms, clientRandom, serverRandom);
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

	int keyBlockLength(CipherSuiteType type) {
		// client_write_MAC_secret[SecurityParameters.hash_size]
		// server_write_MAC_secret[SecurityParameters.hash_size]
		// client_write_key[SecurityParameters.key_material_length]
		// server_write_key[SecurityParameters.key_material_length]
		// client_write_IV[SecurityParameters.IV_size]
		// server_write_IV[SecurityParameters.IV_size]
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
	public byte[] clientWriteKey(CipherSuiteType type) throws Exception {
		if (type.exportable()) {
			return finalClientWriteKey(clientWriteKey(block, type.hash(), type.key()), clientRandom, serverRandom, type.key());
		}
		return clientWriteKey(block, type.hash(), type.key());
	}

	/** server_write_key */
	public byte[] serverWriteKey(CipherSuiteType type) throws Exception {
		if (type.exportable()) {
			return finalServerWriteKey(serverWriteKey(block, type.hash(), type.key()), clientRandom, serverRandom, type.key());
		}
		return serverWriteKey(block, type.hash(), type.key());
	}

	/** client_write_IV */
	public byte[] clientWriteIV(CipherSuiteType type) {
		return clientWriteIV(block, type.hash(), type.key(), type.iv());
	}

	/** server_write_IV */
	public byte[] serverWriteIV(CipherSuiteType type) {
		return serverWriteIV(block, type.hash(), type.key(), type.iv());
	}

	/** master_secret -> verify_data */
	protected byte[] serverFinished() throws Exception {
		return serverFinished(master, hashMD5(), hashSHA());
	}

	/** master_secret -> verify_data */
	protected byte[] clientFinished() throws Exception {
		return clientFinished(master, hashMD5(), hashSHA());
	}

	public void serverRandom(byte[] value) {
		serverRandom = value;
	}

	public void clientRandom(byte[] value) {
		clientRandom = value;
	}
}