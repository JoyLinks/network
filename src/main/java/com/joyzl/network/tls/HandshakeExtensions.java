package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

/**
 * 握手消息支持扩展字段
 * 
 * @author ZhangXi 2024年12月13日
 */
abstract class HandshakeExtensions extends Handshake implements Extensions {

	private List<Extension> extensions = new ArrayList<>();

	@Override
	public boolean isHelloRetryRequest() {
		return false;
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

	public Extension lastExtension() {
		if (extensions.size() > 0) {
			return extensions.get(extensions.size() - 1);
		}
		return null;
	}
}