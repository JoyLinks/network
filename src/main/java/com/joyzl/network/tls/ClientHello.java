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
public class ClientHello extends HandshakeExtensions {

	private short version = TLS.V12;
	private byte[] random = TLS.EMPTY_BYTES;
	private byte[] session_id = TLS.EMPTY_BYTES;
	private short[] cipher_suites = TLS.EMPTY_SHORTS;
	private byte[] compression_methods = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CLIENT_HELLO;
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
}