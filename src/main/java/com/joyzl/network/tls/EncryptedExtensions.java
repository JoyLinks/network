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
public class EncryptedExtensions extends HandshakeExtensions {

	@Override
	public byte msgType() {
		return ENCRYPTED_EXTENSIONS;
	}
}