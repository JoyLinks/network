package com.joyzl.network.ftp;

/**
 * 表示类型
 * 
 * @author ZhangXi 2024年7月8日
 */
public class TYPE extends FTPMessage {

	public final static String ASCII = "A";
	public final static String EBCDIC = "E";
	public final static String IMAGE = "I";
	public final static String LENGTH = "L";

	public final static String NOT_PRINT = "N";
	public final static String TELNET = "T";
	public final static String CARRIAGE_CONTROL = "C";

	private String format = ASCII, show = NOT_PRINT;

	@Override
	protected FTPCommand getCommand() {
		return FTPCommand.TYPE;
	}

	@Override
	protected String getParameter() {
		if (format == ASCII || format == EBCDIC) {
			return ASCII + FTP.SPACE + show;
		}
		if (format == IMAGE) {
			return IMAGE;
		}
		if (format == LENGTH) {
			return "L" + show;
		}
		return null;
	}

	@Override
	protected void setParameter(String value) {
		// A N
		if (value.startsWith(ASCII)) {
			setFormat(ASCII);
			if (value.length() == 3) {
				if (value.endsWith(NOT_PRINT)) {
					setShow(NOT_PRINT);
					return;
				}
				if (value.endsWith(TELNET)) {
					setShow(TELNET);
					return;
				}
				if (value.endsWith(CARRIAGE_CONTROL)) {
					setShow(CARRIAGE_CONTROL);
					return;
				}
			}
		} else //
		if (value.startsWith(EBCDIC)) {
			setFormat(EBCDIC);
			if (value.length() == 3) {
				if (value.endsWith(NOT_PRINT)) {
					setShow(NOT_PRINT);
					return;
				}
				if (value.endsWith(TELNET)) {
					setShow(TELNET);
					return;
				}
				if (value.endsWith(CARRIAGE_CONTROL)) {
					setShow(CARRIAGE_CONTROL);
					return;
				}
			}
		} else //
		if (value.startsWith(IMAGE)) {
			setFormat(IMAGE);
			setShow(null);
		} else //
		if (value.startsWith(LENGTH)) {
			setFormat(LENGTH);
			setShow(value.substring(1));
		}
		throw new IllegalArgumentException("参数格式无效");
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

	public String getFormat() {
		return format;
	}

	public void setFormat(String value) {
		format = value;
	}

	public String getShow() {
		return show;
	}

	public void setShow(String value) {
		show = value;
	}
}