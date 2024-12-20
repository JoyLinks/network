package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 2246 TLSv1.0
 * RFC 4336 TLSv1.1
 * RFC 5246 TLSv1.2
 * 
 * opaque ASN.1Cert<1..2^24-1>;
 * 
 * struct {
 *     ASN.1Cert certificate_list<0..2^24-1>;
 * } Certificate;
 * 
 * RFC 8446 TLSv1.3
 * 
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
public class Certificate extends HandshakeExtensions {

	private final static CertificateEntry[] EMPTY = new CertificateEntry[0];
	private CertificateEntry[] certificates = EMPTY;
	private byte[] context = null;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CERTIFICATE;
	}

	public byte[] getContext() {
		return context;
	}

	public void setContext(byte[] value) {
		context = value;
	}

	public CertificateEntry[] getCertificates() {
		return certificates;
	}

	public void setCertificates(CertificateEntry[] value) {
		if (value == null) {
			certificates = EMPTY;
		} else {
			certificates = value;
		}
	}
}