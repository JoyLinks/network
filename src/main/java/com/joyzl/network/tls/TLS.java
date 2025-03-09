package com.joyzl.network.tls;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

abstract class TLS {

	/** TLS 1.3 */
	public final static short V13 = 0x0304;
	/** TLS 1.2 */
	public final static short V12 = 0x0303;
	/** TLS 1.1 */
	public final static short V11 = 0x0302;
	/** TLS 1.0 */
	public final static short V10 = 0x0301;
	/** SSL 3.0 */
	public final static short SSL30 = 0x0300;
	/** SSL 2.0 */
	public final static short SSL20 = 0x0200;

	/** ALL VERSIONS */
	public final static short[] ALL_VERSIONS = new short[] { V13, V12, V11, V10 };

	/** RFC3749 enum { null(0),DEFLATE(1),(255) } CompressionMethod; */
	public final static byte COMPRESSION_METHOD_NULL = 0;
	public final static byte COMPRESSION_METHOD_DEFLATE = 1;
	/** TLS 1.3 不支持压缩 */
	public final static byte[] COMPRESSION_METHODS = new byte[] { COMPRESSION_METHOD_NULL };

	/** 防空常量 */
	final static byte[] EMPTY_BYTES = new byte[0];
	/** 防空常量 */
	final static short[] EMPTY_SHORTS = new short[0];
	/** 防空常量 */
	final static byte[][] EMPTY_STRINGS = new byte[0][];

	/** 随机数 */
	final static SecureRandom RANDOM;
	static {
		try {
			RANDOM = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String version(short value) {
		if (value == V13) {
			return "TLS 1.3";
		}
		if (value == V12) {
			return "TLS 1.2";
		}
		if (value == V11) {
			return "TLS 1.1";
		}
		if (value == V10) {
			return "TLS 1.0";
		}
		if (value == SSL30) {
			return "SSL 3.0";
		}
		return "UNKNOWN";
	}
}