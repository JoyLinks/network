package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *       ProtocolName supported_protocols<2..2^16-1>;
 * } ApplicationSettingsSupport;
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
class ApplicationSettings extends ApplicationLayerProtocolNegotiation {

	public ApplicationSettings() {
	}

	public ApplicationSettings(byte[]... value) {
		set(value);
	}

	@Override
	public short type() {
		return APPLICATION_SETTINGS;
	}
}