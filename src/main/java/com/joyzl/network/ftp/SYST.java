package com.joyzl.network.ftp;

/**
 * 系统
 * 
 * @author ZhangXi 2024年7月10日
 */
public class SYST extends FTPMessage {

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.SYST;
	}

	@Override
	protected String getParameter() {
		return null;
	}

	@Override
	protected void setParameter(String value) {
	}

	@Override
	protected boolean isSuccess() {
		// 215 系统类型名称
		// 500, 501, 502, 421
		return getCode() == 215;
	}

	@Override
	protected void finish() {
	}
}