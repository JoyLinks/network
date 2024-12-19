package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * struct {
 *       Extension extensions<0..2^16-1>;
 * } EncryptedExtensions;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class EncryptedExtensions extends Handshake {

	private List<Extension> extensions = new ArrayList<>();

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.ENCRYPTED_EXTENSIONS;
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
}