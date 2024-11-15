package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 文件的部分的输入流
 * 
 * @author ZhangXi 2024年11月14日
 */
public class FilePartInputStream extends FileInputStream {

	private int length;

	public FilePartInputStream(File file, long offset, long length) throws IOException {
		super(file);

		if (offset + length > file.length()) {
			throw new IllegalArgumentException("长度超出范围" + (offset + length) + ">" + file.length());
		}

		super.skip(offset);
		this.length = (int) length;

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