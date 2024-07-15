package com.joyzl.network.ftp;

import java.io.File;

/**
 * 与文件传输相关的命令 RETR STOR STOU APPE
 * 
 * @author ZhangXi 2024年7月14日
 */
public abstract class FileMessage extends FTPMessage {

	private File file;
	private String path;

	private long size, transferred;

	@Override
	protected String getParameter() {
		return getPath();
	}

	@Override
	protected void setParameter(String value) {
		setPath(value);
	}

	@Override
	protected void finish() {
	}

	/** 获取远程文件路径 */
	public String getPath() {
		return path;
	}

	/** 设置远程文件路径 */
	public void setPath(String value) {
		path = value;
	}

	/** 获取本地文件 */
	public File getFile() {
		return file;
	}

	/** 设置本地文件 */
	public void setFile(File value) {
		file = value;
	}

	/**
	 * 获取已传输的字节数
	 * 
	 * @return 如果命令未执行则返回0，如果已执行或正在执行则返回实际传输数量
	 */
	public long getTransferred() {
		return transferred;
	}

	protected void setTransferred(long value) {
		transferred = value;
	}

	/***
	 * 获取文件总字节数
	 */
	public long getSize() {
		return size;
	}

	protected void setSize(long value) {
		size = value;
	}
}