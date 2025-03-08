package com.joyzl.network.tls;

/**
 * 协商使用证书URL；协商成功后客户端可通过CertificateURL消息代替Certificate消息
 * 
 * <pre>
 * RFC 6066
 * {EMPTY}
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class ClientCertificateURL extends Extension {

	final static ClientCertificateURL INSTANCE = new ClientCertificateURL();

	@Override
	public short type() {
		return CLIENT_CERTIFICATE_URL;
	}

	@Override
	public String toString() {
		return "client_certificate_url";
	}
}