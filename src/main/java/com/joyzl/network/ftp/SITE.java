package com.joyzl.network.ftp;

/**
 * 站点参数
 * 
 * @author ZhangXi 2024年7月10日
 */
public class SITE extends FTPMessage {

	private String name;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.SITE;
	}

	@Override
	protected String getParameter() {
		return getName();
	}

	@Override
	protected void setParameter(String value) {
		setName(value);
	}

	@Override
	protected boolean isSuccess() {
		// 200 命令成功
		// 202 命令没有实现，对本站点冗余
		// 500, 501, 530
		return getCode() == 200 || getCode() == 202;
	}

	@Override
	protected void finish() {
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}
}