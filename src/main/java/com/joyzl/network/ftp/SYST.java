package com.joyzl.network.ftp;

/**
 * 系统
 * 
 * @author ZhangXi 2024年7月10日
 */
public class SYST extends FTPMessage {

	// RFC1700 Assigned Numbers
	// OPERATING SYSTEM NAMES

	private String system;

	@Override
	public FTPCommand getCommand() {
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

	@Override
	protected void setText(String value) {
		super.setText(value);
		// 215 UNIX emulated by FileZilla.
		// 状态代码后第一段字符为操作系统名称
		int index = value.indexOf(' ');
		if (index > 0) {
			setSystem(value.substring(0, index));
		} else {
			setSystem(null);
		}
	}

	public String getSystem() {
		return system;
	}

	protected void setSystem(String value) {
		system = value;
	}
}