package com.joyzl.network.ftp;

/**
 * 重新初始化
 * 
 * @author ZhangXi 2024年7月8日
 */
public class REIN extends FTPMessage {

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.REIN;
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
		// 120 服务将在稍后准备完成
		// 220 接受新用户服务准备完成
		// 421 服务不可用，关闭控制连接
		// 500, 502
		return getCode() == 120 || getCode() == 220;
	}

	@Override
	protected void finish() {
	}
}