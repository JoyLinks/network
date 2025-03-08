package com.joyzl.network.tls;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 发送客户端的URL证书
 * 
 * <pre>
 * RFC 6066
 * 
 * enum {
 *     individual_certs(0), // DER-encoded X.509v3 certificate
 *     pkipath(1),          // DER-encoded certificate chain
 *     (255)
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
 * 
 * 
 * RFC 3986 Uniform Resource Identifier (URI): Generic Syntax
 * 
 * Content-Type: application/pkix-cert
 * Content-Type: application/pkix-pkipath
 * </pre>
 */
class CertificateURL extends Handshake {

	// CertChainType MAX(255)

	/** DER-encoded X.509v3 certificate */
	public final static byte INDIVIDUAL_CERTS = 0;
	/** MIME:application/pkix-pkipath */
	public final static byte PKIPATH = 1;

	////////////////////////////////////////////////////////////////////////////////

	private final static URLAndHash[] EMPTY_URL_AND_HASH = new URLAndHash[0];
	private byte type;
	private URLAndHash[] urls = EMPTY_URL_AND_HASH;

	@Override
	public byte msgType() {
		return CERTIFICATE_URL;
	}

	public byte getCertChainType() {
		return type;
	}

	public void setCertChainType(byte value) {
		type = value;
	}

	public boolean hasURLs() {
		return urls.length > 0;
	}

	public URLAndHash[] getURLs() {
		return urls;
	}

	public URLAndHash getURL(int index) {
		return urls[index];
	}

	public void addURL(URLAndHash value) {
		if (urls == EMPTY_URL_AND_HASH) {
			urls = new URLAndHash[] { value };
		} else {
			urls = Arrays.copyOf(urls, urls.length + 1);
			urls[urls.length - 1] = value;
		}
	}

	public void setURLs(URLAndHash[] value) {
		if (value == null) {
			value = EMPTY_URL_AND_HASH;
		} else {
			urls = value;
		}
	}

	public int size() {
		return urls.length;
	}

	static class URLAndHash {
		private byte[] url = TLS.EMPTY_BYTES;
		private byte[] hash = TLS.EMPTY_BYTES;

		/***
		 * URL SHA1 hash
		 */
		public byte[] getHash() {
			return hash;
		}

		public void setHash(byte[] value) {
			if (value == null) {
				hash = TLS.EMPTY_BYTES;
			} else {
				hash = value;
			}
		}

		public byte[] getURL() {
			return url;
		}

		public String getURLString() {
			final String value = URLDecoder.decode(new String(url, StandardCharsets.US_ASCII), StandardCharsets.UTF_8);
			return value;
		}

		public void setURL(String value) {
			value = URLEncoder.encode(value, StandardCharsets.UTF_8);
			url = value.getBytes(StandardCharsets.US_ASCII);
		}

		public void setURL(byte[] value) {
			if (value == null) {
				url = TLS.EMPTY_BYTES;
			} else {
				url = value;
			}
		}
	}
}