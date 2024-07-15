package com.joyzl.network.ftp;

/**
 * 重命名（新名称）
 * 
 * @author ZhangXi 2024年7月10日
 */
public class RNTO extends FTPMessage {

	private String path;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.RNTO;
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
		// 532, 553
		// 500, 501, 502, 503, 421, 530
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