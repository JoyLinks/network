package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 6066
 * {EMPTY}
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class ClientCertificateUrl extends Extension {

	public final static ClientCertificateUrl INSTANCE = new ClientCertificateUrl();

	@Override
	public ExtensionType type() {
		return ExtensionType.CLIENT_CERTIFICATE_URL;
	}

	@Override
	public String toString() {
		return "client_certificate_url";
	}
}