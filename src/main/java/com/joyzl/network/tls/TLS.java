package com.joyzl.network.tls;

public class TLS {

	/** SSL 3.0 */
	public final static short SSL30 = 0x0300;
	/** TLS 1.0 / SSL 3.0 */
	public final static short V10 = 0x0301;
	/** TLS 1.1 */
	public final static short V11 = 0x0302;
	/** TLS 1.2 */
	public final static short V12 = 0x0303;
	/** TLS 1.3 */
	public final static short V13 = 0x0304;
	/** ALL VERSIONS */
	public final static short[] ALL_VERSIONS = new short[] { V10, V11, V12, V13 };

	/** 16K (2^14) */
	final static int CHUNK_MAX = 16384;
	final static byte[] EMPTY_BYTES = new byte[0];
}