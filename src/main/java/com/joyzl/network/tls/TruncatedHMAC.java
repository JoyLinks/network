package com.joyzl.network.tls;

/**
 * <pre>
 * RFC 6066
 * {EMPTY}
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class TruncatedHMAC extends Extension {

	public final static TruncatedHMAC INSTANCE = new TruncatedHMAC();

	@Override
	public short type() {
		return TRUNCATED_HMAC;
	}

	@Override
	public String toString() {
		return "truncated_hmac";
	}
}