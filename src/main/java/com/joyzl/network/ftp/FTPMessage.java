package com.joyzl.network.ftp;

/**
 * FTP 控制命令
 * 
 * @author ZhangXi 2024年7月5日
 */
public abstract class FTPMessage {

	// 响应代码
	private int code;
	// 响应文本
	private String text;

	/** 获取控制命令 */
	protected abstract FTPCommand getCommand();

	/** 获取控制命令参数 */
	protected abstract String getParameter();

	/** 设置控制命令参数 */
	protected abstract void setParameter(String value);

	/** 获取响应代码 */
	public int getCode() {
		return code;
	}

	/** 设置响应代码 */
	public void setCode(int value) {
		code = value;
	}

	/** 获取响应代码文本 */
	public String getCodeText() {
		return FTP.codeText(code);
	}

	/** 设置响应文本 */
	protected void setText(String value) {
		text = value;
	}

	/** 添加响应文本 */
	protected void addText(String value) {
		text = value;
	}

	/** 获取响应文本 */
	public String getText() {
		return text;
	}

	/** 命令是否执行成功 */
	protected abstract boolean isSuccess();

	/** 命令完成后的动作 */
	protected abstract void finish();
}
