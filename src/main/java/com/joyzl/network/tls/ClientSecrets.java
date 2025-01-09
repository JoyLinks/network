package com.joyzl.network.tls;

public class ClientSecrets extends BaseSecrets {

	private byte[] client_early_traffic = TLS.EMPTY_BYTES;

	public ClientSecrets(short code) throws Exception {
		super(code);
	}

	public byte[] handshakeTrafficReadKey() throws Exception {
		return key(serverHandshakeTraffic());
	}

	public byte[] handshakeTrafficReadIv() throws Exception {
		return iv(serverHandshakeTraffic());
	}

	public byte[] applicationTrafficReadKey() throws Exception {
		return key(serverApplicationTraffic());
	}

	public byte[] applicationTrafficReadIv() throws Exception {
		return iv(serverApplicationTraffic());
	}

	public byte[] applicationTrafficWriteKey() throws Exception {
		return key(clientApplicationTraffic());
	}

	public byte[] applicationTrafficWriteIv() throws Exception {
		return iv(clientApplicationTraffic());
	}
}