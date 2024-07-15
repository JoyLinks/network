package com.joyzl.network.ftp;

/**
 * 删除目录
 * 
 * @author ZhangXi 2024年7月10日
 */
public class RMD extends FTPMessage {

	private String path;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.RMD;
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