package com.joyzl.network.ftp;

/**
 * 删除文件
 * 
 * @author ZhangXi 2024年7月10日
 */
public class DELE extends FTPMessage {

	private String path;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.DELE;
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
		// 450, 550
		// 500, 501, 502, 421, 530
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