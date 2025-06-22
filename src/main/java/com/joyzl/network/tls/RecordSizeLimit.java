/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * RFC8449
 * 
 * @author ZhangXi 2025年2月10日
 */
class RecordSizeLimit extends Extension {

	private int value;

	@Override
	public short type() {
		return RECORD_SIZE_LIMIT;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}