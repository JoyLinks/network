package com.joyzl.network.tls;

public class Random {
	/** GMT seconds */
	private int gmt_unix_time;
	/** [28]Bytes */
	private byte[] random_bytes;

	public byte[] getRandomBytes() {
		return random_bytes;
	}

	public void setRandomBytes(byte[] value) {
		random_bytes = value;
	}

	public int getGMTUnixTime() {
		return gmt_unix_time;
	}

	public void setGMTUnixTime(int value) {
		gmt_unix_time = value;
	}

}