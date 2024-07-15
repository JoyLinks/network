package com.joyzl.network.ftp;

/**
 * 空
 * 
 * @author ZhangXi 2024年7月8日
 */
public class NOOP extends FTPMessage {

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.NOOP;
	}

	@Override
	protected String getParameter() {
		return null;
	}

	@Override
	protected void setParameter(String value) {
		// 无参数
	}

	@Override
	protected boolean isSuccess() {
		// 200 500 421
		return getCode() == 200;
	}

	@Override
	protected void finish() {
	}
}