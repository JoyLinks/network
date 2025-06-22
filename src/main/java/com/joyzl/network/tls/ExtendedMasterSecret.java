/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 扩展：使用增强型主密钥计算方式，用于TLS 1.2密钥交换，TLS 1.3也应明确设置此扩展。
 * 
 * <pre>
 * { EMPTY }
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
class ExtendedMasterSecret extends Extension {

	public final static ExtendedMasterSecret INSTANCE = new ExtendedMasterSecret();

	private ExtendedMasterSecret() {
	}

	@Override
	public short type() {
		return EXTENDED_MASTER_SECRET;
	}
}