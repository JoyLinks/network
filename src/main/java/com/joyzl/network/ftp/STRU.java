package com.joyzl.network.ftp;

/**
 * 文件结构
 * 
 * @author ZhangXi 2024年7月8日
 */
public class STRU extends FTPMessage {

	public final static String FILE = "F";
	public final static String RECORD = "R";
	public final static String PAGE = "P";

	private String structure = FILE;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.STRU;
	}

	@Override
	protected String getParameter() {
		return getStructure();
	}

	@Override
	protected void setParameter(String value) {
		setStructure(value);
	}

	@Override
	protected boolean isSuccess() {
		// 200
		// 500, 501, 504, 421, 530
		return getCode() == 200;
	}

	@Override
	protected void finish() {
	}

	public String getStructure() {
		return structure;
	}

	public void setStructure(String value) {
		structure = value;
	}
}