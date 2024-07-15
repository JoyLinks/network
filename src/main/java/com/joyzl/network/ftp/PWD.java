package com.joyzl.network.ftp;

/**
 * 打印工作目录
 * 
 * @author ZhangXi 2024年7月8日
 */
public class PWD extends FTPMessage {

	private String path;

	@Override
	public FTPCommand getCommand() {
		return FTPCommand.PWD;
	}

	@Override
	protected String getParameter() {
		return null;
	}

	@Override
	protected void setParameter(String value) {
	}

	@Override
	protected void setText(String value) {
		super.setText(value);

		// 257 "/" is current directory.
		int index = 0;
		for (; index < value.length(); index++) {
			if (value.charAt(index) == '"') {
				// NEXT
				final StringBuilder builder = new StringBuilder();
				for (; index < value.length(); index++) {
					if (value.charAt(index) == '"') {
						if (index < value.length() && value.charAt(index + 1) == '"') {
							// 引号转义
							builder.append('"');
						} else {
							setPath(builder.toString());
							break;
						}
					}
					builder.append(value.charAt(index));
				}
				break;
			}
		}
	}

	@Override
	protected boolean isSuccess() {
		// 257 创建了目录
		// 500, 501, 502, 421, 550
		return getCode() == 257;
	}

	@Override
	protected void finish() {
	}

	/** 获取当前目录 */
	public String getPath() {
		return path;
	}

	/** 设置当前目录 */
	protected void setPath(String value) {
		path = value;
	}
}