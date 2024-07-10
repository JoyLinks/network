package com.joyzl.network.ftp;

/**
 * 放弃
 * 
 * @author ZhangXi 2024年7月8日
 */
public class ABOR extends FTPMessage {

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.ABOR;
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
		// 225 数据连接打开，没有传输
		// 226 关闭数据连接
		// 500, 501, 502, 421
		return getCode() == 200;
	}

	@Override
	protected void finish() {
	}
}