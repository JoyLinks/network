/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.chain;

/**
 * 链路类型
 */
public enum ChainType {

	NONE(0, "NONE"),

	TCP_CLIENT(1, "TCP CLIENT"),
	TCP_SERVER(2, "TCP SERVER"),
	TCP_SLAVE(3, "TCP SLAVE"),
	UDP_CLIENT(4, "UDP CLIENT"),
	UDP_SERVER(5, "UDP SERVER"),
	UDP_SLAVE(6, "UDP SLAVE"),
	SERIAL_PORT(7, "SERIAL PORT"),

	/** 基于TCP ODBS协议的客户端连接 */
	TCP_ODBS_CLIENT(11, "TCP ODBS CLIENT"),
	/** 基于TCP ODBS协议的服务端监听 */
	TCP_ODBS_SERVER(12, "TCP ODBS SERVER"),
	/** 由TCP ODBS服务端接收的连接 */
	TCP_ODBS_SLAVE(13, "TCP ODBS SLAVE"),

	/** 基于TCP HTTP协议的客户端连接 */
	TCP_HTTP_CLIENT(15, "TCP HTTP CLIENT"),
	/** 基于TCP HTTP协议的客户端连接 */
	TCP_HTTP_CLIENT_WEB_SOCKET(16, "TCP HTTP CLIENT WEB SOCKET"),
	/** 基于TCP HTTP协议的服务端监听 */
	TCP_HTTP_SERVER(17, "TCP HTTP SERVER"),
	/** 由TCP HTTP服务端接收的连接 */
	TCP_HTTP_SLAVE(18, "TCP HTTP SLAVE"),
	/** 由TCP HTTP服务端接收的连接 */
	TCP_HTTP_SLAVE_WEB_SOCKET(19, "TCP HTTP SLAVE WEB SOCKET"),

	TCP_SHORT(20, "TCP SHORT");

	// <tt:Uri>rtsp://192.168.1.108:554/type=0&amp;id=1</tt:Uri>
	// 1.将连接目标发送到伺服器 "192.168.1.108:554"
	// 2.伺服器创建一个通道 ChainType为 TCP_TRANSPARENT/UDP_TRANSPARENT
	// 2.将IP地址修改为1230端口

	private final int code;
	private final String text;

	private ChainType(int v, String t) {
		code = v;
		text = t;
	}

	public final int code() {
		return code;
	}

	public final String text() {
		return text;
	}

	@Override
	public final String toString() {
		return text();
	}

	/**
	 * 根据code值获取{@link ChainType}枚举实例
	 *
	 * @param code
	 * @return {@link ChainType} / null
	 */
	public final static ChainType fromCode(int code) {
		final ChainType[] types = ChainType.values();
		for (int index = 0; index < types.length; index++) {
			if (types[index].code() == code) {
				return types[index];
			}
		}
		return null;
	}

	/**
	 * 获取枚举定义的最大code值
	 *
	 * @return 最大code值
	 */
	public final static int getMaxCode() {
		int code = 0;
		final ChainType[] types = ChainType.values();
		for (int index = 0; index < types.length; index++) {
			if (types[index].code() > code) {
				code = types[index].code();
			}
		}
		return code;
	}
}
