package com.joyzl.network.ftp;

/**
 * 改变工作目录
 * 
 * @author ZhangXi 2024年7月8日
 */
public class CWD extends FTPMessage {

	private String path;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.CWD;
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
		// 250 请求文件动作完成
		// 500, 501, 502, 421, 530, 550
		return getCode() == 250;
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