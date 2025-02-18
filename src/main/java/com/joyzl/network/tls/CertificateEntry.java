package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

/**
 * @see CertificateTypes
 * @author ZhangXi 2024年12月21日
 */
public class CertificateEntry implements Extensions {

	public final static byte X509 = 0;
	public final static byte RAW_PUBLIC_KEY = 2;

	private List<Extension> extensions = new ArrayList<>();
	private final byte type;
	private byte[] data;

	public CertificateEntry(byte type) {
		this.type = type;
	}

	public byte type() {
		return type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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
}