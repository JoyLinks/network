package com.joyzl.network.tls;

/**
 * TLS 1.3 具有状态的密钥处理类
 * 
 * @author ZhangXi 2024年12月30日
 */
abstract class V2SecretCache extends V2DeriveSecret {

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
	public byte[] masterSecret(byte[] clientRandom, byte[] serverRandom) throws Exception {
		return master = super.masterSecret(pms, clientRandom, serverRandom);
	}

	/** RSA(pms) / Diffie-Hellman(key) */
	public byte[] masterSecret() throws Exception {
		return master = super.masterSecret(pms, hash());
	}

	public boolean hasBlock() {
		return block != null;
	}

	public byte[] block() {
		return block;
	}

	/** key_block */
	public byte[] keyBlock(byte[] serverRandom, byte[] clientRandom) throws Exception {
		return block = keyBlock(master, serverRandom, clientRandom, keyBlockLength());
	}

	public int keyBlockLength() {
		// client_write_MAC_key [SecurityParameters.mac_key_length]
		// server_write_MAC_key [SecurityParameters.mac_key_length]
		// client_write_key [SecurityParameters.enc_key_length]
		// server_write_key [SecurityParameters.enc_key_length]
		// client_write_IV [SecurityParameters.fixed_iv_length]
		// server_write_IV [SecurityParameters.fixed_iv_length]
		return macLength() * 2 + keyLength() * 2 + ivLength() * 2;
	}

	/** client_write_MAC_key */
	public byte[] clientWriteMACKey() {
		return clientWriteMACKey(block, macLength());
	}

	/** server_write_MAC_key */
	public byte[] serverWriteMACKey() {
		return serverWriteMACKey(block, macLength());
	}

	/** client_write_key */
	public byte[] clientWriteKey() {
		return clientWriteKey(block, macLength(), keyLength());
	}

	/** server_write_key */
	public byte[] serverWriteKey() {
		return serverWriteKey(block, macLength(), keyLength());
	}

	/** client_write_IV */
	public byte[] clientWriteIV() {
		return clientWriteIV(block, macLength(), keyLength(), ivLength());
	}

	/** server_write_IV */
	public byte[] serverWriteIV() {
		return serverWriteIV(block, macLength(), keyLength(), ivLength());
	}

	/** master_secret -> verify_data */
	protected byte[] serverFinished() throws Exception {
		return serverFinished(master, hash());
	}

	/** master_secret -> verify_data */
	protected byte[] clientFinished() throws Exception {
		return clientFinished(master, hash());
	}

	public abstract int macLength();

	public abstract int keyLength();

	public abstract int ivLength();
}