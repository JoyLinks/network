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
public class CertificateURL extends Handshake {

	private CertChainType type;
	private URLAndHash[] urls;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CERTIFICATE_URL;
	}

	public URLAndHash[] getUrls() {
		return urls;
	}

	public void setUrls(URLAndHash[] value) {
		urls = value;
	}

	public CertChainType getCertChainType() {
		return type;
	}

	public void setCertChainType(CertChainType value) {
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