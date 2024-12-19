package com.joyzl.network.tls;

public class ProtocolVersion {

	/** SSL 3.0 */
	public final static short SSL30 = 0x0300;
	/** TLS 1.0 / SSL 3.0 */
	public final static short TLS10 = 0x0301;
	/** TLS 1.2 */
	public final static short TLS12 = 0x0303;
	/** TLS 1.2 */
	public final static short TLS13 = 0x0304;

	private byte major;
	private byte minor;

	public ProtocolVersion(byte major, byte minor) {
		this.major = major;
		this.minor = minor;
	}

	public byte getMinor() {
		return minor;
	}

	public void setMinor(byte value) {
		minor = value;
	}

	public byte getMajor() {
		return major;
	}

	public void setMajor(byte value) {
		major = value;
	}
}