package com.joyzl.network.ftp;

/**
 * 重命名（原名称）
 * 
 * @author ZhangXi 2024年7月10日
 */
public class RNFR extends FTPMessage {

	private String path;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.RNFR;
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
		// 450, 550
		// 500, 501, 502, 421, 530
		// 350 请求文件动作需要进一步的信息
		return getCode() == 350;
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