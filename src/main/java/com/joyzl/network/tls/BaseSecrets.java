package com.joyzl.network.tls;

/**
 * Server And Client same PART
 * 
 * @author ZhangXi 2024年12月30日
 */
public abstract class BaseSecrets extends DeriveSecret {

	private byte[] psk = null;
	private byte[] handshake = TLS.EMPTY_BYTES;
	private byte[] client_handshake_traffic = TLS.EMPTY_BYTES;
	private byte[] server_handshake_traffic = TLS.EMPTY_BYTES;
	private byte[] master = TLS.EMPTY_BYTES;
	private byte[] client_application_traffic = TLS.EMPTY_BYTES;
	private byte[] server_application_traffic = TLS.EMPTY_BYTES;
	private byte[] resumption_master = TLS.EMPTY_BYTES;
	private byte[] resumption = TLS.EMPTY_BYTES;

	public BaseSecrets(short code) throws Exception {
		super(code);
	}

	/**
	 * 获取共享密钥(PSK)
	 */
	public byte[] shared() {
		return psk;
	}

	public byte[] early() throws Exception {
		return early(psk);
	}

	public byte[] exporterBinderKey() throws Exception {
		return exporterBinderKey(early());
	}

	public byte[] resumptionBinderKey() throws Exception {
		return resumptionBinderKey(early());
	}

	public byte[] clientEarlyTraffic() throws Exception {
		return clientEarlyTraffic(early(), hash());
	}

	public byte[] earlyExporterMaster() throws Exception {
		return earlyExporterMaster(early(), hash());
	}

	public byte[] handshake(byte[] shared) throws Exception {
		if (handshake == TLS.EMPTY_BYTES) {
			handshake = handshake(early(), shared);
		}
		return handshake;
	}

	public byte[] handshake() throws Exception {
		if (handshake == TLS.EMPTY_BYTES) {
			// final byte[] derived = derive(early(psk));
			// handshake = extract(derived, shared());

			handshake = handshake(early(), psk);
		}
		return handshake;
	}

	/** ClientHello|ServerHello */
	public byte[] clientHandshakeTraffic() throws Exception {
		if (client_handshake_traffic == TLS.EMPTY_BYTES) {
			client_handshake_traffic = clientHandshakeTraffic(handshake(), hash());
		}
		return client_handshake_traffic;
	}

	/** ClientHello|ServerHello */
	public byte[] serverHandshakeTraffic() throws Exception {
		if (server_handshake_traffic == TLS.EMPTY_BYTES) {
			server_handshake_traffic = serverHandshakeTraffic(handshake(), hash());
		}
		return server_handshake_traffic;
	}

	public byte[] clientFinished() throws Exception {
		return finishedVerifyData(clientHandshakeTraffic(), hash());
	}

	public byte[] serverFinished() throws Exception {
		return finishedVerifyData(serverHandshakeTraffic(), hash());
	}

	public byte[] master() throws Exception {
		if (master == TLS.EMPTY_BYTES) {
			final byte[] derived = derive(handshake());
			master = extract(derived, new byte[hashLength()]);
		}
		return master;
	}

	/** ClientHello|ServerHello...Finished */
	public byte[] clientApplicationTraffic() throws Exception {
		if (client_application_traffic == TLS.EMPTY_BYTES) {
			client_application_traffic = clientApplicationTraffic(master(), hash());
		}
		return client_application_traffic;
	}

	/** ClientHello|ServerHello...Finished */
	public byte[] serverApplicationTraffic() throws Exception {
		if (server_application_traffic == TLS.EMPTY_BYTES) {
			server_application_traffic = serverApplicationTraffic(master(), hash());
		}
		return server_application_traffic;
	}

	public byte[] resumptionMaster() throws Exception {
		if (resumption_master == TLS.EMPTY_BYTES) {
			resumption_master = resumptionMaster(master(), hash());
		}
		return resumption_master;
	}

	public byte[] resumption(byte[] nonce) throws Exception {
		if (resumption == TLS.EMPTY_BYTES) {
			resumption = resumption(resumptionMaster(), nonce);
		}
		return resumption;
	}
}