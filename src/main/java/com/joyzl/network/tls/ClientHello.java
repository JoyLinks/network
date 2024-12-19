package com.joyzl.network.tls;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * uint16 ProtocolVersion;
 * opaque Random[32];

 * uint8 CipherSuite[2];    / Cryptographic suite selector /

 * struct {
 *     ProtocolVersion legacy_version = 0x0303;    / TLS v1.2 /
 *     Random random;
 *     opaque legacy_session_id<0..32>;
 *     CipherSuite cipher_suites<2..2^16-2>;
 *     opaque legacy_compression_methods<1..2^8-1>;
 *     Extension extensions<8..2^16-1>;
 * } ClientHello;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class ClientHello extends Handshake {

	private List<Extension> extensions = new ArrayList<>();

	private short version;
	private byte[] random;
	private byte[] session_id;
	private short[] cipher_suites;
	private byte[] compression_methods;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.CLIENT_HELLO;
	}

	public byte[] getRandom() {
		return random;
	}

	public void setRandom(byte[] value) {
		random = value;
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short value) {
		version = value;
	}

	public byte[] getSessionId() {
		return session_id;
	}

	public void setSessionId(byte[] value) {
		session_id = value;
	}

	public short[] getCipherSuites() {
		return cipher_suites;
	}

	public void setCipherSuites(short[] value) {
		cipher_suites = value;
	}

	public byte[] getCompressionMethods() {
		return compression_methods;
	}

	public void setCompressionMethods(byte[] value) {
		compression_methods = value;
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

	public static ClientHello make(short version) {
		final ClientHello hello = new ClientHello();
		hello.setRandom(SecureRandom.getSeed(32));
		if (version == TLS.V13) {
			hello.setSessionId(TLS.EMPTY_BYTES);
			hello.setCipherSuites(CipherSuite.V13);
			hello.setCompressionMethods(TLS.ZERO_BYTES);
			hello.getExtensions().add(new SupportedVersions(TLS.V13, TLS.V12, TLS.V11, TLS.V10));

		}
		if (version == TLS.V12) {

		}
		if (version == TLS.V13) {

		}
		return hello;
	}
}