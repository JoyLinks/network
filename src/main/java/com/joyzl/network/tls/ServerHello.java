package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * TLS 1.3
 * 
 * struct {
 *       ProtocolVersion legacy_version = 0x0303;    / TLS v1.2 /
 *       Random random[32];
 *       opaque legacy_session_id_echo<0..32>;
 *       CipherSuite cipher_suite;
 *       uint8 legacy_compression_method = 0;
 *       Extension extensions<6..2^16-1>;
 * } ServerHello;
 * </pre>
 * 
 * <pre>
 * TLS 1.2
 * 
 * struct {
 *       ProtocolVersion server_version;
 *       Random random;
 *       SessionID session_id;
 *       CipherSuite cipher_suite;
 *       CompressionMethod compression_method;
 *       select (extensions_present) {
 *             case false:
 *                   struct {};
 *             case true:
 *                   Extension extensions<0..2^16-1>;
 *       };
 * } ServerHello;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class ServerHello extends HandshakeExtensions {

	/** SHA-256("HelloRetryRequest") */
	final static byte[] HELLO_RETRY_REQUEST_RANDOM = new byte[] { (byte) 0xCF, 0x21, (byte) 0xAD, 0x74, (byte) 0xE5, (byte) 0x9A, 0x61, 0x11, (byte) 0xBE, 0x1D, (byte) 0x8C, 0x02, 0x1E, 0x65, (byte) 0xB8, (byte) 0x91, (byte) 0xC2, (byte) 0xA2, 0x11, 0x16, 0x7A, (byte) 0xBB, (byte) 0x8C, 0x5E, 0x07, (byte) 0x9E, 0x09, (byte) 0xE2, (byte) 0xC8, (byte) 0xA8, 0x33, (byte) 0x9C };
	/** 1.2 - 1.3 "DOWNGRD"+0x01 */
	final static byte[] V12_LAST8 = new byte[] { 0x44, 0x4F, 0x57, 0x4E, 0x47, 0x52, 0x44, 0x01 };
	/** 1.1 - 1.0 "DOWNGRD"+0x00 */
	final static byte[] V11_LAST8 = new byte[] { 0x44, 0x4F, 0x57, 0x4E, 0x47, 0x52, 0x44, 0x00 };

	private short version = TLS.V12;
	private byte[] random = TLS.EMPTY_BYTES;
	private byte[] session_id = TLS.EMPTY_BYTES;
	private short cipher_suite = CipherSuite.TLS_NULL_WITH_NULL_NULL;
	private byte compression_method = COMPRESSION_METHOD_NULL;

	@Override
	public byte msgType() {
		return SERVER_HELLO;
	}

	@Override
	public boolean isHelloRetryRequest() {
		return Arrays.equals(random, HELLO_RETRY_REQUEST_RANDOM);
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short value) {
		version = value;
	}

	public boolean hasRandom() {
		return random.length > 0;
	}

	public byte[] getRandom() {
		return random;
	}

	public void setRandom(byte[] value) {
		if (value == null) {
			random = TLS.EMPTY_BYTES;
		} else {
			random = value;
		}
	}

	public void makeRandom(short version) {
		TLS.RANDOM.nextBytes(random = new byte[32]);

		// 根据指定版本注入降级标志
		if (version == TLS.V12) {
			for (int i = 0; i < V12_LAST8.length; i++) {
				random[i + 24] = V12_LAST8[i];
			}
		} else if (version == TLS.V11 || version == TLS.V10) {
			for (int i = 0; i < V11_LAST8.length; i++) {
				random[i + 24] = V11_LAST8[i];
			}
		}
	}

	public void makeHelloRetryRequest() {
		random = HELLO_RETRY_REQUEST_RANDOM;
	}

	public boolean hasSessionId() {
		return session_id.length > 0;
	}

	public byte[] getSessionId() {
		return session_id;
	}

	public void setSessionId(byte[] value) {
		session_id = value;
	}

	public short getCipherSuite() {
		return cipher_suite;
	}

	public void setCipherSuite(short value) {
		cipher_suite = value;
	}

	public byte getCompressionMethod() {
		return compression_method;
	}

	public void setCompressionMethod(byte value) {
		compression_method = value;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(':');
		b.append("version=");
		b.append(version(version));
		b.append(",random=");
		if (random.length == 32) {
			if (Arrays.equals(random, HELLO_RETRY_REQUEST_RANDOM)) {
				b.append("HelloRetryRequest");
			} else if (Arrays.equals(random, random.length - V12_LAST8.length, random.length, V12_LAST8, 0, V12_LAST8.length)) {
				b.append("DOWNGRD");
			} else if (Arrays.equals(random, random.length - V11_LAST8.length, random.length, V11_LAST8, 0, V11_LAST8.length)) {
				b.append("DOWNGRD");
			} else {
				b.append("32byte");
			}
		} else {
			b.append(random.length);
			b.append("byte");
		}
		b.append(",session_id=");
		b.append(session_id.length);
		b.append("byte");
		b.append(",cipher_suites=");
		b.append(CipherSuite.name(cipher_suite));
		b.append(",compression_methods=");
		b.append(compression_method);
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