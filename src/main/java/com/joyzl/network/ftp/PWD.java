package com.joyzl.network.ftp;

/**
 * 打印工作目录
 * 
 * @author ZhangXi 2024年7月8日
 */
public class PWD extends FTPMessage {

	private String path;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.PWD;
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
		// 257 创建了目录
		// 500, 501, 502, 421, 550
		return getCode() == 257;
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