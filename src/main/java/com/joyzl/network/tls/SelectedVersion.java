package com.joyzl.network.tls;

/**
 *
 * <pre>
 * struct {
 *     select (Handshake.msg_type) {
 *      case client_hello:
 *        ProtocolVersion versions<2..254>;
 * 
 *      case server_hello: (and HelloRetryRequest)
 *        ProtocolVersion selected_version;
 *     }
 * } SupportedVersions
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class SelectedVersion extends Extension {

	private short selected;

	public SelectedVersion() {
	}

	public SelectedVersion(short version) {
		selected = version;
	}

	@Override
	public short type() {
		return SUPPORTED_VERSIONS;
	}

	public short get() {
		return selected;
	}

	public void set(short value) {
		selected = value;
	}

	@Override
	public String toString() {
		return name() + ":" + version(selected);
	}
}