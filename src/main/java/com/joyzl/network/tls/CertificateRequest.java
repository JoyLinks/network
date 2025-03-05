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
class CertificateRequest extends HandshakeExtensions {

	private byte[] context = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE_REQUEST;
	}

	public byte[] getContext() {
		return context;
	}

	public void setContext(byte[] value) {
		if (value == null) {
			context = TLS.EMPTY_BYTES;
		} else {
			context = value;
		}
	}
}