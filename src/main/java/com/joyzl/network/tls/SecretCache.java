package com.joyzl.network.tls;

/**
 * 具有状态的密钥处理类
 * 
 * @author ZhangXi 2024年12月30日
 */
public class SecretCache extends DeriveSecret {

	private byte[] early;
	private byte[] master;
	private byte[] handshake;
	private byte[] clientTraffic;
	private byte[] serverTraffic;

	public SecretCache() throws Exception {
	}

	public SecretCache(String digest, String hmac) throws Exception {
		digest(digest);
		hmac(hmac);
	}

	@Override
	public void hmac(String name) throws Exception {
		super.hmac(name);
		early = early(null);
	}

	public boolean hasKey() {
		return master != null;
	}

	public boolean handshaked() {
		return clientTraffic != null || serverTraffic != null;
	}

	/**
	 * 设置共享密钥
	 */
	public void sharedKey(byte[] key) throws Exception {
		handshake = handshake(early, key);
		master = master(handshake);
	}

	public byte[] clientHandshakeTraffic() throws Exception {
		return clientTraffic = clientHandshakeTraffic(handshake, hash());
	}

	public byte[] serverHandshakeTraffic() throws Exception {
		return serverTraffic = serverHandshakeTraffic(handshake, hash());
	}

	public byte[] clientFinished() throws Exception {
		return finishedVerifyData(clientTraffic, hash());
	}

	public byte[] serverFinished() throws Exception {
		return finishedVerifyData(serverTraffic, hash());
	}

	public byte[] clientApplicationTraffic() throws Exception {
		return clientApplicationTraffic(master, hash());
	}

	public byte[] serverApplicationTraffic() throws Exception {
		return serverApplicationTraffic(master, hash());
	}

	public byte[] exporterMaster() throws Exception {
		return exporterMaster(master, hash());
	}

	public void done() {
		hashReset();
		handshake = null;
		clientTraffic = null;
		serverTraffic = null;
	}

	public byte[] resumption(byte[] nonce) throws Exception {
		master = resumptionMaster(master, hash());
		nonce = resumption(master, nonce);
		done();
		early = early(nonce);
		return nonce;
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