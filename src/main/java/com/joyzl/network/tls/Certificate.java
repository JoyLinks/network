package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * enum {
 *     X509(0),
 *     RawPublicKey(2),
 *     (255)
 * } CertificateType;
 * 
 * struct {
 *     select (certificate_type) {
 *           case RawPublicKey:
 *             / From RFC 7250 ASN.1_subjectPublicKeyInfo /
 *             opaque ASN1_subjectPublicKeyInfo<1..2^24-1>;
 *           case X509:
 *             opaque cert_data<1..2^24-1>;
 *     };
 *     Extension extensions<0..2^16-1>;
 * } CertificateEntry;
 * 
 * struct {
 *     opaque certificate_request_context<0..2^8-1>;
 *     CertificateEntry certificate_list<0..2^24-1>;
 * } Certificate;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class Certificate extends Handshake {

	private final List<Extension> extensions = new ArrayList<>();
	private CertificateEntry[] certificates;
	private byte[] context;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CERTIFICATE;
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

	public void addExtension(Extension value) {
		extensions.add(value);
	}
}