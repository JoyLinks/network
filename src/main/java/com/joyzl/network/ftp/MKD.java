package com.joyzl.network.ftp;

/**
 * 新建目录
 * 
 * @author ZhangXi 2024年7月10日
 */
public class MKD extends FTPMessage {

	private String path;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.MKD;
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
		// 500, 501, 502, 421, 530, 550
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