package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

/**
 * 握手消息支持扩展字段
 * 
 * @author ZhangXi 2024年12月13日
 */
public abstract class HandshakeExtensions extends Handshake implements Extensions {

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
}