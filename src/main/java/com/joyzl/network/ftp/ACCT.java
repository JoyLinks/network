package com.joyzl.network.ftp;

/**
 * 账户
 * 
 * @author ZhangXi 2024年7月8日
 */
public class ACCT extends FTPMessage {

	private String account;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.ACCT;
	}

	@Override
	protected String getParameter() {
		return account;
	}

	@Override
	protected void setParameter(String value) {
		account = value;
	}

	@Override
	protected boolean isSuccess() {
		// 230 用户成功登录，继续
		// 202 命令没有实现，对本站点冗余
		// 530
		// 500, 501, 503, 421
		return getCode() == 202 || getCode() == 230;
	}

	@Override
	protected void finish() {
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String value) {
		account = value;
	}
}