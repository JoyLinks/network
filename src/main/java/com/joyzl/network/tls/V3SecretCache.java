package com.joyzl.network.tls;

/**
 * TLS 1.3 具有状态的密钥处理类
 * 
 * @author ZhangXi 2024年12月30日
 */
class V3SecretCache extends V3DeriveSecret {

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
	private byte[] clientEarlyTraffic;

	public void initialize(CipherSuiteType type) throws Exception {
		initialize(type.macAlgorithm(), type.digestAlgorithm());
		reset(null);
	}

	public void initialize(String mac, String digest) throws Exception {
		super.initialize(mac, digest);
		reset(null);
	}

	public boolean hasMaster() {
		return master != null;
	}

	public boolean isHandshaked() {
		return clientHandshakeTraffic != null || serverHandshakeTraffic != null;
	}

	public boolean isApplication() {
		return clientApplicationTraffic != null || serverApplicationTraffic != null;
	}

	/**
	 * 重置密钥处理类
	 */
	public void reset(byte[] psk) throws Exception {
		hashReset();
		master = null;
		handshake = null;
		clientHandshakeTraffic = null;
		serverHandshakeTraffic = null;
		clientApplicationTraffic = null;
		serverApplicationTraffic = null;
		early = earlySecret(psk);
	}

	/**
	 * 设置对端共享密钥
	 */
	public void sharedKey(byte[] key) throws Exception {
		handshake = handshakeSecret(early, key);
		master = masterSecret(handshake);
	}

	public byte[] clientHandshakeTrafficSecret() throws Exception {
		if (clientHandshakeTraffic == null) {
			return clientHandshakeTraffic = clientHandshakeTrafficSecret(handshake, hash());
		}
		return clientHandshakeTraffic;
	}

	public byte[] serverHandshakeTrafficSecret() throws Exception {
		if (serverHandshakeTraffic == null) {
			return serverHandshakeTraffic = serverHandshakeTrafficSecret(handshake, hash());
		}
		return serverHandshakeTraffic;
	}

	public byte[] clientHandshakeWriteKey(CipherSuiteType type) throws Exception {
		return writeKey(clientHandshakeTraffic, type.key());
	}

	public byte[] clientHandshakeWriteIV(CipherSuiteType type) throws Exception {
		return writeIV(clientHandshakeTraffic, type.iv());
	}

	public byte[] serverHandshakeWriteKey(CipherSuiteType type) throws Exception {
		return writeKey(serverHandshakeTraffic, type.key());
	}

	public byte[] serverHandshakeWriteIV(CipherSuiteType type) throws Exception {
		return writeIV(serverHandshakeTraffic, type.iv());
	}

	public byte[] clientFinished() throws Exception {
		return finishedVerifyData(clientHandshakeTraffic, hash());
	}

	public byte[] clientFinished2() throws Exception {
		return finishedVerifyData(clientApplicationTraffic, hash());
	}

	public byte[] serverFinished() throws Exception {
		return finishedVerifyData(serverHandshakeTraffic, hash());
	}

	public byte[] clientApplicationTrafficSecret() throws Exception {
		if (clientApplicationTraffic == null) {
			return clientApplicationTraffic = clientApplicationTrafficSecret(master, hash());
		}
		return clientApplicationTraffic;
	}

	public byte[] serverApplicationTrafficSecret() throws Exception {
		if (serverApplicationTraffic == null) {
			return serverApplicationTraffic = serverApplicationTrafficSecret(master, hash());
		}
		return serverApplicationTraffic;
	}

	public void nextApplicationTrafficSecret() throws Exception {
		clientApplicationTraffic = nextApplicationTrafficSecret(clientApplicationTraffic);
		serverApplicationTraffic = nextApplicationTrafficSecret(serverApplicationTraffic);
	}

	public byte[] clientApplicationWriteKey(CipherSuiteType type) throws Exception {
		return writeKey(clientApplicationTraffic, type.key());
	}

	public byte[] clientApplicationWriteIV(CipherSuiteType type) throws Exception {
		return writeIV(clientApplicationTraffic, type.iv());
	}

	public byte[] serverApplicationWriteKey(CipherSuiteType type) throws Exception {
		return writeKey(serverApplicationTraffic, type.key());
	}

	public byte[] serverApplicationWriteIV(CipherSuiteType type) throws Exception {
		return writeIV(serverApplicationTraffic, type.iv());
	}

	public byte[] exporterMasterSecret() throws Exception {
		return exporterMasterSecret(master, hash());
	}

	public byte[] resumptionMasterSecret() throws Exception {
		return master = resumptionMasterSecret(master, hash());
	}

	public byte[] resumptionSecret(byte[] ticket_nonce) throws Exception {
		ticket_nonce = resumptionSecret(master, ticket_nonce);
		return ticket_nonce;
	}

	public byte[] resumptionBinderKey() throws Exception {
		return finishedVerifyData(resumptionBinderKey(early), hash());
	}

	public byte[] clientEarlyTrafficSecret() throws Exception {
		return clientEarlyTraffic = clientEarlyTrafficSecret(early, hash());
	}

	public byte[] clientEarlyWriteKey(CipherSuiteType type) throws Exception {
		return writeKey(clientEarlyTraffic, type.key());
	}

	public byte[] clientEarlyWriteIV(CipherSuiteType type) throws Exception {
		return writeIV(clientEarlyTraffic, type.iv());
	}

	public byte[] earlyExporterMasterSecret() throws Exception {
		return earlyExporterMasterSecret(early, hash());
	}
}