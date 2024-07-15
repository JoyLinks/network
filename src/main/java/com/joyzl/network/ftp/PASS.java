package com.joyzl.network.ftp;

/**
 * 密码
 * 
 * @author ZhangXi 2024年7月8日
 */
public class PASS extends FTPMessage {

	private String password;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.PASS;
	}

	@Override
	protected String getParameter() {
		return password;
	}

	@Override
	protected void setParameter(String value) {
		password = value;
	}

	@Override
	protected boolean isSuccess() {
		// 230 用户成功登录，继续
		// 202 命令没有实现，对本站点冗余
		// 530
		// 500, 501, 503, 421
		// 332 需要帐户才能登录
		return getCode() == 202 || getCode() == 230 || getCode() == 332;
	}

	@Override
	protected void finish() {
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String value) {
		password = value;
	}
}