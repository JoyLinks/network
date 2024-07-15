package com.joyzl.network.ftp;

/**
 * 上层目录
 * 
 * @author ZhangXi 2024年7月8日
 */
public class CDUP extends FTPMessage {

	private String path;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.CDUP;
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
	protected void setText(String value) {
		super.setText(value);
		// TODO 获取路径
	}

	@Override
	protected boolean isSuccess() {
		// 200 命令成功
		// 500, 501, 502, 421, 530, 550
		return getCode() == 200;
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