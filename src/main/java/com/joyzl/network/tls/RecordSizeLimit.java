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