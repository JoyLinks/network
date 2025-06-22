/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 扩展：协商使用截断为 80-bit 的 truncated HMAC 值；此扩展已禁止使用。
 * 
 * <pre>
 * RFC 6066
 * {EMPTY}
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class TruncatedHMAC extends Extension {

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