package com.joyzl.network.ftp;

/**
 * 用户
 * 
 * @author ZhangXi 2024年7月8日
 */
public class USER extends FTPMessage {

	private String username;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.USER;
	}

	@Override
	protected String getParameter() {
		return getUsername();
	}

	@Override
	protected void setParameter(String value) {
		setUsername(value);
	}

	@Override
	protected boolean isSuccess() {
		// 230 用户成功登录，继续
		// 530 未登录
		// 500, 501, 421
		// 331 用户名有效，需要密码
		// 332 需要帐户才能登录
		return getCode() == 230 || getCode() == 331 || getCode() == 332;
	}

	@Override
	protected void finish() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String value) {
		username = value;
	}
}