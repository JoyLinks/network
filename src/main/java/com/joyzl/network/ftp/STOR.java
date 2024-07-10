package com.joyzl.network.ftp;

/**
 * 保存
 * 
 * @author ZhangXi 2024年7月10日
 */
public class STOR extends FTPMessage {

	private String path;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.STOR;
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
		// 110 重新开始标记响应
		// 226 关闭数据连接
		// 250 请求文件动作完成
		// 425, 426, 451, 551, 552
		// 532, 450, 452, 553
		// 500, 501, 421, 530
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