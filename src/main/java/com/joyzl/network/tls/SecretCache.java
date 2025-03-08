package com.joyzl.network.tls;

/**
 * 具有状态的密钥处理类
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
		reset(null);
	}

	public boolean hasKey() {
		return master != null;
	}

	public boolean handshaked() {
		return clientHandshakeTraffic != null || serverHandshakeTraffic != null;
	}

	public boolean application() {
		return clientApplicationTraffic != null || serverApplicationTraffic != null;
	}

	// public byte[] clientTraffic() {
	// return clientApplicationTraffic;
	// }
	//
	// public byte[] serverTraffic() {
	// return serverApplicationTraffic;
	// }

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
		early = early(psk);
	}

	/**
	 * 设置对端共享密钥
	 */
	public void sharedKey(byte[] key) throws Exception {
		handshake = handshake(early, key);
		master = master(handshake);
	}

	public byte[] clientHandshakeTraffic() throws Exception {
		if (clientHandshakeTraffic == null) {
			return clientHandshakeTraffic = clientHandshakeTraffic(handshake, hash());
		}
		return clientHandshakeTraffic;
	}

	public byte[] serverHandshakeTraffic() throws Exception {
		if (serverHandshakeTraffic == null) {
			return serverHandshakeTraffic = serverHandshakeTraffic(handshake, hash());
		}
		return serverHandshakeTraffic;
	}

	public byte[] clientFinished() throws Exception {
		return finishedVerifyData(clientHandshakeTraffic, hash());
	}

	public byte[] serverFinished() throws Exception {
		return finishedVerifyData(serverHandshakeTraffic, hash());
	}

	public byte[] clientApplicationTraffic() throws Exception {
		if (clientApplicationTraffic == null) {
			return clientApplicationTraffic = clientApplicationTraffic(master, hash());
		}
		return clientApplicationTraffic;
	}

	public byte[] serverApplicationTraffic() throws Exception {
		if (serverApplicationTraffic == null) {
			return serverApplicationTraffic = serverApplicationTraffic(master, hash());
		}
		return serverApplicationTraffic;
	}

	public byte[] exporterMaster() throws Exception {
		return exporterMaster(master, hash());
	}

	public byte[] resumptionMaster() throws Exception {
		return master = resumptionMaster(master, hash());
	}

	public byte[] resumption(byte[] ticket_nonce) throws Exception {
		ticket_nonce = resumption(master, ticket_nonce);
		return ticket_nonce;
	}

	public byte[] resumptionBinderKey() throws Exception {
		return finishedVerifyData(resumptionBinderKey(early), hash());
	}

	public byte[] clientEarlyTraffic() throws Exception {
		return clientEarlyTraffic(early, hash());
	}

	public byte[] earlyExporterMaster() throws Exception {
		return earlyExporterMaster(early, hash());
	}
}