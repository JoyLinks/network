package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 6066
 * enum {
 *     individual_certs(0), pkipath(1), (255)
 * } CertChainType;
 * 
 * struct {
 *     CertChainType type;
 *     URLAndHash url_and_hash_list<1..2^16-1>;
 * } CertificateURL;
 * 
 * struct {
 *     opaque url<1..2^16-1>;
 *     unint8 padding(0x01);
 *     opaque SHA1Hash[20];
 * } URLAndHash;
 * </pre>
 */
class CertificateURL extends Handshake {

	// CertChainType MAX(255)

	/** DER-encoded X.509v3 certificate */
	public final static byte INDIVIDUAL_CERTS = 0;
	/** MIME:application/pkix-pkipath */
	public final static byte PKIPATH = 1;

	////////////////////////////////////////////////////////////////////////////////

	private byte type;
	private URLAndHash[] urls;

	@Override
	public byte msgType() {
		return CERTIFICATE_URL;
	}

	public URLAndHash[] getUrls() {
		return urls;
	}

	public void setUrls(URLAndHash[] value) {
		urls = value;
	}

	public byte getCertChainType() {
		return type;
	}

	public void setCertChainType(byte value) {
		type = value;
	}

	class URLAndHash {
		private String url;
		private byte[] hash;

		/***
		 * URL SHA1 hash
		 */
		public byte[] getHash() {
			return hash;
		}

		public void setHash(byte[] value) {
			hash = value;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String value) {
			url = value;
		}
	}
}