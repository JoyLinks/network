package com.joyzl.network.tls;

public class ServerSecrets extends BaseSecrets {

	private byte[] exporter_master = TLS.EMPTY_BYTES;
	private byte[] early_exporter_master;

	public ServerSecrets(short code) throws Exception {
		super(code);
	}

	public byte[] handshakeTrafficWriteKey() throws Exception {
		return key(serverHandshakeTraffic());
	}

	public byte[] handshakeTrafficWriteIv() throws Exception {
		return iv(serverHandshakeTraffic());
	}

	public byte[] handshakeTrafficReadKey() throws Exception {
		return key(clientHandshakeTraffic());
	}

	public byte[] handshakeTrafficReadIv() throws Exception {
		return iv(clientHandshakeTraffic());
	}

	public byte[] applicationTrafficReadKey() throws Exception {
		return key(clientApplicationTraffic());
	}

	public byte[] applicationTrafficReadIv() throws Exception {
		return iv(clientApplicationTraffic());
	}

	public byte[] applicationTrafficWriteKey() throws Exception {
		return key(serverApplicationTraffic());
	}

	public byte[] applicationTrafficWriteIv() throws Exception {
		return iv(serverApplicationTraffic());
	}

	public byte[] exporterMaster() throws Exception {
		if (exporter_master == TLS.EMPTY_BYTES) {
			exporter_master = exporterMaster(master(), hash());
		}
		return exporter_master;
	}
}