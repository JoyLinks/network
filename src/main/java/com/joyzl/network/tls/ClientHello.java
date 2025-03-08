package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 2246 TLSv1.0
 * RFC 4346 TLSv1.1
 * 
 * struct {
 *    uint32 gmt_unix_time;
 *    opaque random_bytes[28];
 * } Random[32];
 * 
 * enum { null(0), (255) } CompressionMethod;
 * 
 * struct {
 *     ProtocolVersion client_version;
 *     Random random;
 *     SessionID session_id;
 *     CipherSuite cipher_suites<2..2^16-1>;
 *     CompressionMethod compression_methods<1..2^8-1>;
 * } ClientHello;
 * 
 * RFC 5246 TLSv1.2
 * struct {
 *     ProtocolVersion client_version;
 *     Random random;
 *     SessionID session_id;
 *     CipherSuite cipher_suites<2..2^16-2>;
 *     CompressionMethod compression_methods<1..2^8-1>;
 *     select (extensions_present) {
 *         case false:
 *             struct {};
 *         case true:
 *             Extension extensions<0..2^16-1>;
 *     };
 * } ClientHello;
 * 
 * RFC 8446 TLSv1.3
 * 
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
class ClientHello extends HandshakeExtensions {

	private short version = TLS.V12;
	private byte[] random = TLS.EMPTY_BYTES;
	private byte[] session_id = TLS.EMPTY_BYTES;
	private short[] cipher_suites = TLS.EMPTY_SHORTS;
	private byte[] compression_methods = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CLIENT_HELLO;
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short value) {
		version = value;
	}

	/**
	 * 匹配
	 */
	public short matchVersion(short[] others) {
		for (int s = 0; s < others.length; s++) {
			if (version == others[s]) {
				return others[s];
			}
		}
		return 0;
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

	public void makeRandom() {
		TLS.RANDOM.nextBytes(random = new byte[32]);
	}

	public boolean hasSessionId() {
		return session_id.length > 0;
	}

	public byte[] getSessionId() {
		return session_id;
	}

	public void setSessionId(byte[] value) {
		if (value == null) {
			session_id = TLS.EMPTY_BYTES;
		} else {
			session_id = value;
		}
	}

	public void makeSessionId() {
		TLS.RANDOM.nextBytes(session_id = new byte[32]);
	}

	public boolean hasCipherSuites() {
		return cipher_suites.length > 0;
	}

	public short[] getCipherSuites() {
		return cipher_suites;
	}

	public void setCipherSuites(short[] value) {
		if (value == null) {
			cipher_suites = TLS.EMPTY_SHORTS;
		} else {
			cipher_suites = value;
		}
	}

	public boolean hasCompressionMethods() {
		return compression_methods.length > 0;
	}

	public byte[] getCompressionMethods() {
		return compression_methods;
	}

	public void setCompressionMethods(byte[] value) {
		if (value == null) {
			compression_methods = TLS.EMPTY_BYTES;
		} else {
			compression_methods = value;
		}
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		b.append(name());
		b.append(':');
		b.append("version=");
		b.append(version(version));
		b.append(",random=");
		b.append(random.length);
		b.append("byte");
		b.append(",session_id=");
		b.append(session_id.length);
		b.append("byte");
		b.append(",cipher_suites=");
		for (int index = 0; index < cipher_suites.length; index++) {
			if (index > 0) {
				b.append(' ');
			}
			b.append(CipherSuite.named(cipher_suites[index]));
		}
		b.append(",compression_methods=");
		for (int index = 0; index < compression_methods.length; index++) {
			if (index > 0) {
				b.append(' ');
			}
			b.append(compression_methods[index]);
		}
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