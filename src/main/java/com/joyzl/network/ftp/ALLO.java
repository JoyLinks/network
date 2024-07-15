package com.joyzl.network.ftp;

/**
 * 分配
 * 
 * @author ZhangXi 2024年7月10日
 */
public class ALLO extends FTPMessage {

	private long size, occupy;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.ALLO;
	}

	@Override
	protected String getParameter() {
		// decimal-integer [SP R SP decimal-integer]
		if (getOccupy() > 0) {
			return getSize() + " R " + getOccupy();
		}
		return Long.toString(getSize());
	}

	@Override
	protected void setParameter(String value) {
		// ALLO SP decimal-integer [SP R SP decimal-integer] CRLF
		int r = value.indexOf(" R ");
		if (r > 0) {
			setSize(Long.parseLong(value, 0, r, 10));
			setOccupy(Long.parseLong(value, r + 3, value.length(), 10));
		} else {
			setSize(Long.parseLong(value));
			setOccupy(0);
		}
	}

	@Override
	protected boolean isSuccess() {
		// 200 命令成功
		// 202 命令没有实现，对本站点冗余
		// 500, 501, 504, 421, 530
		return getCode() == 200 || getCode() == 202;
	}

	@Override
	protected void finish() {
	}

	/** 获取文件大小 */
	public long getSize() {
		return size;
	}

	/** 设置文件大小 */
	public void setSize(long value) {
		size = value;
	}

	/** 获取占用空间大小 */
	public long getOccupy() {
		return occupy;
	}

	/** 设置占用空间大小 */
	public void setOccupy(long value) {
		occupy = value;
	}
}