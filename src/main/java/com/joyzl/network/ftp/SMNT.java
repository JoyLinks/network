package com.joyzl.network.ftp;

/**
 * 结构装备
 * 
 * @author ZhangXi 2024年7月8日
 */
public class SMNT extends FTPMessage {

	private String path;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.SMNT;
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
		// 202 命令没有实现，对本站点冗余
		// 250 请求文件动作完成
		// 500, 501, 502, 421, 530, 550
		return getCode() == 230;
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