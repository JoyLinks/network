package com.joyzl.network.tls;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * struct {
 *     ProtocolVersion legacy_version = 0x0303;    / TLS v1.2 /
 *     Random random[32];
 *     opaque legacy_session_id_echo<0..32>;
 *     CipherSuite cipher_suite;
 *     uint8 legacy_compression_method = 0;
 *     Extension extensions<6..2^16-1>;
 * } ServerHello;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class ServerHello extends Handshake {

	/** SHA-256("HelloRetryRequest") */
	final static byte[] HELLO_RETRY_REQUEST_RANDOM = new byte[] { (byte) 0xCF, 0x21, (byte) 0xAD, 0x74, (byte) 0xE5, (byte) 0x9A, 0x61, 0x11, (byte) 0xBE, 0x1D, (byte) 0x8C, 0x02, 0x1E, 0x65, (byte) 0xB8, (byte) 0x91, (byte) 0xC2, (byte) 0xA2, 0x11, 0x16, 0x7A, (byte) 0xBB, (byte) 0x8C, 0x5E, 0x07, (byte) 0x9E, 0x09, (byte) 0xE2, (byte) 0xC8, (byte) 0xA8, 0x33, (byte) 0x9C };
	/** 1.2 - 1.3 SERVER */
	final static byte[] V12_LAST8 = new byte[] { 0x44, 0x4F, 0x57, 0x4E, 0x47, 0x52, 0x44, 0x01 };
	/** 1.1 - 1.3 SERVER */
	final static byte[] V11_LAST8 = new byte[] { 0x44, 0x4F, 0x57, 0x4E, 0x47, 0x52, 0x44, 0x00 };

	private List<Extension> extensions = new ArrayList<>();

	private short version = TLS.V12;
	private byte[] random;
	private byte[] session_id;
	private short cipher_suite;
	private byte compression_method;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.SERVER_HELLO;
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