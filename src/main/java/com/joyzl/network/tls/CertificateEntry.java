package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

/**
 * DER编码的X.509证书
 * 
 * @see CertificateTypes
 * @author ZhangXi 2024年12月21日
 */
public class CertificateEntry implements Extensions {

	public final static byte X509 = 0;
	public final static byte RAW_PUBLIC_KEY = 2;

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
		if (type == X509) {
			return "X509(" + data.length + ")";
		}
		if (type == RAW_PUBLIC_KEY) {
			return "RPK(" + data.length + ")";
		}
		return "UNKNOWN";
	}
}