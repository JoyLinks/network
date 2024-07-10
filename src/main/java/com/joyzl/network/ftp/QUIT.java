package com.joyzl.network.ftp;

/**
 * 注销
 * 
 * @author ZhangXi 2024年7月8日
 */
public class QUIT extends FTPMessage {

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.QUIT;
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
		// 221 服务关闭控制连接
		// 500
		return getCode() == 221;
	}

	@Override
	protected void finish() {
	}
}