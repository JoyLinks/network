package com.joyzl.network.ftp;

/**
 * 文件名称列表
 * 
 * @author ZhangXi 2024年7月10日
 */
public class NLST extends FTPMessage {

	private String path;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.NLST;
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
		// 125 数据连接已打开，传输开始
		// 150 文件状态正常，将打开数据连接
		// 226 关闭数据连接
		// 250 请求文件动作完成
		// 425, 426, 451
		// 450
		// 500, 501, 502, 421, 530
		return false;
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