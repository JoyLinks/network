package com.joyzl.network.ftp;

/**
 * 帮助
 * 
 * @author ZhangXi 2024年7月10日
 */
public class HELP extends FTPMessage {

	private String name;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.HELP;
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
		// 211 系统状态，或者系统帮助响应
		// 214 帮助信息
		// 500, 501, 502, 421
		return getCode() == 211 || getCode() == 214;
	}

	@Override
	protected void finish() {
	}

	/** 获取须帮助的命令 */
	public String getName() {
		return name;
	}

	/** 设置须帮助的命令 */
	public void setName(String value) {
		name = value;
	}
}