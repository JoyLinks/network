package com.joyzl.network.ftp;

/**
 * 状态
 * 
 * @author ZhangXi 2024年7月10日
 */
public class STAT extends FTPMessage {

	private String path;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.STAT;
	}

	@Override
	protected String getParameter() {
		return getPath();
	}

	@Override
	protected void setParameter(String value) {
		setPath(value);
	}

	@Override
	protected boolean isSuccess() {
		// 211 系统状态，或者系统帮助响应
		// 212 目录状态
		// 213 文件状态
		// 450
		// 500, 501, 502, 421, 530
		return getCode() == 211 || getCode() == 212 || getCode() == 213;
	}

	@Override
	protected void finish() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String value) {
		path = value;
	}
}