package com.joyzl.network.ftp;

/**
 * 传输模式
 * 
 * @author ZhangXi 2024年7月8日
 */
public class MODE extends FTPMessage {

	public final static String STREAM = "S";
	public final static String BLOCK = "B";
	public final static String COMPRESS = "C";

	private String mode = STREAM;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.TYPE;
	}

	@Override
	protected String getParameter() {
		return getMode();
	}

	@Override
	protected void setParameter(String value) {
		setMode(value);
	}

	@Override
	protected boolean isSuccess() {
		// 200 命令成功
		// 500, 501, 504, 421, 530
		return getCode() == 200;
	}

	@Override
	protected void finish() {
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String value) {
		mode = value;
	}
}