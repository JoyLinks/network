/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network;

import java.io.File;
import java.io.IOException;

/**
 * 指定文件读取范围创建输入流
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年3月10日
 */
public final class SegmentInputStream extends java.io.FileInputStream {

	private int length;

	/**
	 * 指定文件的部分创建读取输入流
	 * 
	 * @param file 文件
	 * @param offset 偏移量
	 * @param length 数据量
	 * @throws IOException
	 */
	public SegmentInputStream(File file, long offset, long length) throws IOException {
		super(file);

		if (offset + length > file.length()) {
			throw new IllegalArgumentException("长度超出范围" + (offset + length) + ">" + file.length());
		}

		super.skip(offset);
		this.length = (int) length;

	}

	/**
	 * 指定文件的部分创建读取输入流
	 * 
	 * @param file 文件
	 * @param offset 偏移量
	 * @param length 数据量
	 * @throws IOException
	 */
	public SegmentInputStream(File file, int offset, int length) throws IOException {
		this(file, (long) offset, (long) length);
	}

	@Override
	public int read() throws IOException {
		if (length > 0) {
			length--;
			return super.read();
		}
		return -1;
	}

	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (length > 0) {
			len = super.read(b, off, len < length ? len : length);
			length -= len;
			return len;
		}
		return -1;
	}

	@Override
	public long skip(long n) throws IOException {
		if (length > 0) {
			n = super.skip(n < length ? n : length);
			length -= n;
			return n;
		}
		return 0;
	}

	@Override
	public int available() throws IOException {
		return length;
	}
}