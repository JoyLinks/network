package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用于认证的证书和链中任何支持的证书
 * 
 * <pre>
 * RFC 8446 TLS 1.3
 * 
 * enum {
 *     X509(0), RawPublicKey(2), (255)
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
 * 
 * RFC 8879
 * 
 * struct {
 *       CertificateCompressionAlgorithm algorithm;
 *       uint24 uncompressed_length;
 *       opaque compressed_certificate_message<1..2^24-1>;
 * } CompressedCertificate;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class CertificateV3 extends Handshake {

	// CertificateType MAX(255)

	public final static byte X509 = 0;
	public final static byte RAW_PUBLIC_KEY = 2;

	////////////////////////////////////////////////////////////////////////////////

	private final static CertificateEntry[] EMPTY = new CertificateEntry[0];
	private CertificateEntry[] certificates = EMPTY;
	private byte[] context = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE;
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

	public void add(CertificateEntry value) {
		if (certificates == EMPTY) {
			certificates = new CertificateEntry[] { value };
		} else {
			certificates = Arrays.copyOf(certificates, certificates.length + 1);
			certificates[certificates.length - 1] = value;
		}
	}

	public CertificateEntry get(int index) {
		return certificates[index];
	}

	public CertificateEntry[] get() {
		return certificates;
	}

	public void set(CertificateEntry[] value) {
		if (value == null) {
			certificates = EMPTY;
		} else {
			certificates = value;
		}
	}

	public int size() {
		return certificates.length;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(':');
		b.append(size());
		b.append(",context=");
		b.append(context.length);
		b.append("byte");
		if (size() > 0) {
			for (int index = 0; index < size(); index++) {
				b.append('\n');
				b.append('\t');
				b.append(get(index));
			}
		}
		return b.toString();
	}

	/**
	 * DER编码的X.509证书
	 * 
	 * @see CertificateTypes
	 * @author ZhangXi 2024年12月21日
	 */
	static class CertificateEntry implements Extensions {

		/**
		 * 证书的扩展，可用：OCSP,SignedCertificateTimestamp
		 */
		private List<Extension> extensions = new ArrayList<>();
		private byte type = X509;
		private byte[] data = TLS.EMPTY_BYTES;

		public CertificateEntry() {
		}

		public CertificateEntry(byte type) {
			this.type = type;
		}

		public CertificateEntry(byte[] value) {
			data = value;
		}

		public byte type() {
			return type;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] value) {
			if (value == null) {
				data = TLS.EMPTY_BYTES;
			} else {
				data = value;
			}
		}

		@Override
		public boolean hasExtensions() {
			return !extensions.isEmpty();
		}

		@Override
		public int extensionSize() {
			return extensions.size();
		}

		@Override
		public void addExtension(Extension extension) {
			extensions.add(extension);
		}

		@Override
		public Extension getExtension(int index) {
			return extensions.get(index);
		}

		@Override
		public List<Extension> getExtensions() {
			return extensions;
		}

		@Override
		public void setExtensions(List<Extension> value) {
			if (value != extensions) {
				extensions.clear();
				extensions.addAll(value);
			}
		}

		@Override
		public byte msgType() {
			return Handshake.CERTIFICATE;
		}

		@Override
		public boolean isHelloRetryRequest() {
			return false;
		}

		@Override
		public String toString() {
			final StringBuilder b = new StringBuilder();
			if (type == X509) {
				b.append("X509(");
				b.append(data.length);
				b.append("byte)");
			} else if (type == RAW_PUBLIC_KEY) {
				b.append("RawPublicKey(");
				b.append(data.length);
				b.append("byte)");
			} else {
				b.append("UNKNOWN(");
				b.append(data.length);
				b.append("byte)");
			}
			if (hasExtensions()) {
				for (Extension e : getExtensions()) {
					b.append('\n');
					b.append('\t');
					b.append('\t');
					b.append(e.toString());
				}
			}
			return b.toString();
		}
	}
}