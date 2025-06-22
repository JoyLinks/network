/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * <pre>
 * struct {} EndOfEarlyData;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class EndOfEarlyData extends Handshake {

	public final static EndOfEarlyData INSTANCE = new EndOfEarlyData();

	@Override
	public byte msgType() {
		return END_OF_EARLY_DATA;
	}
}