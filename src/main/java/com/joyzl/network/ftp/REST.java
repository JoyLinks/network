package com.joyzl.network.ftp;

/**
 * 重新传输指定部分
 * 
 * @author ZhangXi 2024年7月10日
 */
public class REST extends FTPMessage {

	private long marker;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.REST;
	}

	@Override
	protected String getParameter() {
		return Long.toString(getMarker());
	}

	@Override
	protected void setParameter(String value) {
		setMarker(Long.parseLong(value));
	}

	@Override
	protected boolean isSuccess() {
		// 500, 501, 502, 421, 530
		// 350 请求文件动作需要进一步的信息
		return getCode() == 350;
	}

	@Override
	protected void finish() {
	}

	/** 标记位置之前的数据重新传输 */
	public long getMarker() {
		return marker;
	}

	/** 标记位置之前的数据重新传输 */
	public void setMarker(long value) {
		marker = value;
	}
}