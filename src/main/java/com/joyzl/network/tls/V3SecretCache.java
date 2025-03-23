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

	public V3SecretCache() {
	}

	public V3SecretCache(String digest, String hmac) throws Exception {
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
}