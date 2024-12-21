package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *     opaque certificate_request_context<0..2^8-1>;
 *     Extension extensions<2..2^16-1>;
 * } CertificateRequest;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class CertificateRequest extends HandshakeExtensions {

	private byte[] certificate_request_context;

	@Override
	public byte msgType() {
		return CERTIFICATE_REQUEST;
	}

	public byte[] getCertificateRequestContext() {
		return certificate_request_context;
	}

	public void setCertificateRequestContext(byte[] value) {
		this.certificate_request_context = value;
	}
}