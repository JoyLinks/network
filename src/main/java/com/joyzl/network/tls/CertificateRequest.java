package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

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
public class CertificateRequest extends Handshake {

	private byte[] certificate_request_context;
	private List<Extension> extensions = new ArrayList<>();

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CERTIFICATE_REQUEST;
	}

	public List<Extension> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<Extension> value) {
		if (value != extensions) {
			extensions.clear();
			extensions.addAll(value);
		}
	}

	public byte[] getCertificateRequestContext() {
		return certificate_request_context;
	}

	public void setCertificateRequestContext(byte[] value) {
		this.certificate_request_context = value;
	}
}