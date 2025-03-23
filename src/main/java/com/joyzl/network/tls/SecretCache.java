package com.joyzl.network.tls;

/**
 * TLS 1.3 具有状态的密钥处理类
 * 
 * @author ZhangXi 2024年12月30日
 */
class SecretCache extends DeriveSecret {

	/*-
	 * TLS 1.3 握手过程与加解密切换
	 * 
	 * Client-------------------------------------------Server
	 * -------------------------1-RTT-------------------------
	 * ClientHello             ------->             plain-text
	 *                                             ServerHello
	 *                                       encrypt-handshake
	 *                                   {EncryptedExtensions}
	 *                                   {CertificateRequest*}
	 *                                          {Certificate*}
	 *                                    {CertificateVerify*}
	 * decrypt-handshak       <-------              {Finished}
	 *                                     encrypt application
	 * decrypt-application    <-------     [Application Data*]
	 * encrypt-handshake
	 * {Certificate*}
	 * {CertificateVerify*}
	 * {Finished}              ------->      decrypt-handshake
	 * encrypt-application
	 * [Application Data]      ------->    decrypt-application
	 * 
	 * -------------------------RETRY-------------------------
	 * ClientHello             ------->             plain-text
	 * plain-text             <-------       HelloRetryRequest
	 * ClientHello             ------->             plain-text
	 *                                             ServerHello
	 *                                       encrypt-handshake
	 *                                   {EncryptedExtensions}
	 *                                   {CertificateRequest*}
	 *                                          {Certificate*}
	 *                                    {CertificateVerify*}
	 * decrypt-handshak       <-------              {Finished}
	 *                                     encrypt-application
	 * decrypt-application    <-------     [Application Data*]
	 * encrypt-handshake
	 * {Certificate*}
	 * {CertificateVerify*}
	 * {Finished}              ------->      decrypt-handshake
	 * encrypt-application
	 * [Application Data]      ------->    decrypt-application
	 * 
	 * -------------------------0-RTT-------------------------
	 * ClientHello
	 * (Application Data*)      ------->         decrypt-early
	 *                                             ServerHello
	 *                                       encrypt-handshake
	 *                                   {EncryptedExtensions}
	 * decrypt-handshak       <-------              {Finished}
	 *                                     encrypt-application
	 * decrypt-application    <-------     [Application Data*]
	 * encrypt-early
	 * (EndOfEarlyData)
	 * encrypt-handshake
	 * {Finished}              ------->      decrypt-handshake
	 * encrypt-application
	 * [Application Data]      ------->    decrypt-application
	 * 
	 * -----------------------KeyUpdate-----------------------
	 * encrypt-application
	 * [key_update]            ------->    decrypt-application
	 *                          ......
	 *                                     encrypt-application
	 * decrypt-application    <-------            [key_update]
	 * update-encrypt                           update-decrypt
	 * update-decrypt                           update-encrypt
	 */

	private byte[] early;
	private byte[] master;
	private byte[] handshake;
	private byte[] clientHandshakeTraffic, clientApplicationTraffic;
	private byte[] serverHandshakeTraffic, serverApplicationTraffic;

	public SecretCache() {
	}

	public SecretCache(String digest, String hmac) throws Exception {
		digest(digest);
		hmac(hmac);
	}

	@Override
	public void hmac(String name) throws Exception {
		super.hmac(name);
		v13Reset(null);
	}

	public boolean hasMaster() {
		return master != null;
	}

	public boolean v13IsHandshaked() {
		return clientHandshakeTraffic != null || serverHandshakeTraffic != null;
	}

	public boolean v13IsApplication() {
		return clientApplicationTraffic != null || serverApplicationTraffic != null;
	}

	/**
	 * 重置密钥处理类
	 */
	public void v13Reset(byte[] psk) throws Exception {
		hashReset();
		master = null;
		handshake = null;
		clientHandshakeTraffic = null;
		serverHandshakeTraffic = null;
		clientApplicationTraffic = null;
		serverApplicationTraffic = null;
		early = v13EarlySecret(psk);
	}

	/**
	 * 设置对端共享密钥
	 */
	public void v13SharedKey(byte[] key) throws Exception {
		handshake = v13HandshakeSecret(early, key);
		master = v13MasterSecret(handshake);
	}

	public byte[] v13ClientHandshakeTrafficSecret() throws Exception {
		if (clientHandshakeTraffic == null) {
			return clientHandshakeTraffic = v13ClientHandshakeTrafficSecret(handshake, hash());
		}
		return clientHandshakeTraffic;
	}

	public byte[] v13ServerHandshakeTrafficSecret() throws Exception {
		if (serverHandshakeTraffic == null) {
			return serverHandshakeTraffic = v13ServerHandshakeTrafficSecret(handshake, hash());
		}
		return serverHandshakeTraffic;
	}

	public byte[] v13ClientFinished() throws Exception {
		return v13FinishedVerifyData(clientHandshakeTraffic, hash());
	}

	public byte[] v13ClientFinished2() throws Exception {
		return v13FinishedVerifyData(clientApplicationTraffic, hash());
	}

	public byte[] v13ServerFinished() throws Exception {
		return v13FinishedVerifyData(serverHandshakeTraffic, hash());
	}

	public byte[] v13ClientApplicationTrafficSecret() throws Exception {
		if (clientApplicationTraffic == null) {
			return clientApplicationTraffic = v13ClientApplicationTrafficSecret(master, hash());
		}
		return clientApplicationTraffic;
	}

	public byte[] v13ServerApplicationTrafficSecret() throws Exception {
		if (serverApplicationTraffic == null) {
			return serverApplicationTraffic = v13ServerApplicationTrafficSecret(master, hash());
		}
		return serverApplicationTraffic;
	}

	public void v13NextApplicationTrafficSecret() throws Exception {
		clientApplicationTraffic = v13NextApplicationTrafficSecret(clientApplicationTraffic);
		serverApplicationTraffic = v13NextApplicationTrafficSecret(serverApplicationTraffic);
	}

	public byte[] v13ExporterMasterSecret() throws Exception {
		return v13ExporterMasterSecret(master, hash());
	}

	public byte[] v13ResumptionMasterSecret() throws Exception {
		return master = v13ResumptionMasterSecret(master, hash());
	}

	public byte[] v13ResumptionSecret(byte[] ticket_nonce) throws Exception {
		ticket_nonce = v13ResumptionSecret(master, ticket_nonce);
		return ticket_nonce;
	}

	public byte[] v13ResumptionBinderKey() throws Exception {
		return v13FinishedVerifyData(v13ResumptionBinderKey(early), hash());
	}

	public byte[] v13ClientEarlyTrafficSecret() throws Exception {
		return v13ClientEarlyTrafficSecret(early, hash());
	}

	public byte[] v13EarlyExporterMasterSecret() throws Exception {
		return v13EarlyExporterMasterSecret(early, hash());
	}

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
	 */

	private byte[] pms;
	// private byte[] master;
	private byte[] block;

	public byte[] v12PMS() {
		return pms;
	}

	public void v12PMS(byte[] value) {
		pms = value;
	}

	public byte[] v12Master() {
		return master;
	}

	/** RSA(pms) / Diffie-Hellman(key) */
	public byte[] v12MasterSecret(byte[] clientRandom, byte[] serverRandom) throws Exception {
		return master = super.v12MasterSecret(pms, clientRandom, serverRandom);
	}

	/** RSA(pms) / Diffie-Hellman(key) */
	public byte[] v12MasterSecret() throws Exception {
		return master = super.v12MasterSecret(pms, hash());
	}

	public boolean v12HasBlock() {
		return block != null;
	}

	/** key_block */
	public byte[] v12KeyBlock(byte[] clientRandom, byte[] serverRandom) throws Exception {
		return block = v12KeyBlock(master, serverRandom, clientRandom, v12KeyBlockLength());
	}

	public int v12KeyBlockLength() {
		// client_write_MAC_key [SecurityParameters.mac_key_length]
		// server_write_MAC_key [SecurityParameters.mac_key_length]
		// client_write_key [SecurityParameters.enc_key_length]
		// server_write_key [SecurityParameters.enc_key_length]
		// client_write_IV [SecurityParameters.fixed_iv_length]
		// server_write_IV [SecurityParameters.fixed_iv_length]
		return keyLength() * 2 + ivLength() * 2 + hmacLength() * 2;
	}

	/** client_write_MAC_key */
	public byte[] v12ClientWriteMACkey() {
		return v12ClientWriteMACkey(block, hmacLength());
	}

	/** server_write_MAC_key */
	public byte[] v12ServerWriteMACkey() {
		return v12ServerWriteMACkey(block, hmacLength());
	}

	/** client_write_key */
	public byte[] v12ClientWriteKey() {
		return v12ClientWriteKey(block, hmacLength(), keyLength());
	}

	/** server_write_key */
	public byte[] v12ServerWriteKey() {
		return v12ServerWriteKey(block, hmacLength(), keyLength());
	}

	/** client_write_IV */
	public byte[] v12ClientWriteIV() {
		return v12ClientWriteIV(block, hmacLength(), keyLength(), ivLength());
	}

	/** server_write_IV */
	public byte[] v12ServerWriteIV() {
		return v12ServerWriteIV(block, hmacLength(), keyLength(), ivLength());
	}

	/** master_secret -> verify_data */
	protected byte[] v12ServerFinished() throws Exception {
		return v12ServerFinished(master, hash());
	}

	/** master_secret -> verify_data */
	protected byte[] v12ClientFinished() throws Exception {
		return v12ClientFinished(master, hash());
	}

	public int blockLength() {
		return 0;
	}

	public int tagLength() {
		return 0;
	}

	public int keyLength() {
		return 0;
	}

	public int ivLength() {
		return 0;
	}
}