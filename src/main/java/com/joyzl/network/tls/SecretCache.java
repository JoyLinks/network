package com.joyzl.network.tls;

/**
 * 具有状态的密钥处理类
 * 
 * @author ZhangXi 2024年12月30日
 */
class SecretCache extends DeriveSecret {

	private byte[] early;
	private byte[] master;
	private byte[] handshake;
	private byte[] clientHTraffic, clientATraffic;
	private byte[] serverHTraffic, serverATraffic;

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
		return clientHTraffic != null || serverHTraffic != null;
	}

	public byte[] clientTraffic() {
		return clientATraffic;
	}

	public byte[] serverTraffic() {
		return serverATraffic;
	}

	/**
	 * 重置密钥处理类
	 */
	public void reset(byte[] psk) throws Exception {
		hashReset();
		master = null;
		handshake = null;
		clientHTraffic = null;
		serverHTraffic = null;
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
		return clientHTraffic = clientHandshakeTraffic(handshake, hash());
	}

	public byte[] serverHandshakeTraffic() throws Exception {
		return serverHTraffic = serverHandshakeTraffic(handshake, hash());
	}

	public byte[] clientFinished() throws Exception {
		return finishedVerifyData(clientHTraffic, hash());
	}

	public byte[] serverFinished() throws Exception {
		return finishedVerifyData(serverHTraffic, hash());
	}

	public byte[] clientApplicationTraffic() throws Exception {
		return clientATraffic = clientApplicationTraffic(master, hash());
	}

	public byte[] serverApplicationTraffic() throws Exception {
		return serverATraffic = serverApplicationTraffic(master, hash());
	}

	public byte[] exporterMaster() throws Exception {
		return exporterMaster(master, hash());
	}

	public byte[] resumptionMaster() throws Exception {
		return master = resumptionMaster(master, hash());
	}

	public byte[] resumption(byte[] ticket_nonce) throws Exception {
		// master = resumptionMaster(master, hash());
		ticket_nonce = resumption(master, ticket_nonce);
		// hashReset();
		// early = early(ticket_nonce);
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