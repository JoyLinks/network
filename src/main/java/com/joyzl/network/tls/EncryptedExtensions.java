/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * <pre>
 * struct {
 *       Extension extensions<0..2^16-1>;
 * } EncryptedExtensions;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class EncryptedExtensions extends HandshakeExtensions {

	@Override
	public byte msgType() {
		return ENCRYPTED_EXTENSIONS;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(':');
		b.append(extensionSize());
		if (hasExtensions()) {
			for (Extension e : getExtensions()) {
				b.append('\n');
				b.append('\t');
				b.append(e.toString());
			}
		}
		return b.toString();
	}
}