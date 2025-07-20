/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.buffer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * 包装DataBuffer为Reader，字符编码为UTF-8
 * <p>
 * 此字符读取类继承并实现 java.io.Reader ，同时取消了父类中所有同步锁和中间缓存（DataBuffer本身就是缓存）且无阻塞。
 * </p>
 * 
 * @author ZhangXi 2025年6月23日
 */
public class DataBufferReader extends Reader {

	private final DataBuffer buffer;
	private final boolean release;

	public DataBufferReader(DataBuffer buffer) {
		this.buffer = buffer;
		release = false;
	}

	public DataBufferReader(DataBuffer buffer, boolean closeRelease) {
		this.buffer = buffer;
		release = closeRelease;
	}

	@Override
	public boolean ready() throws IOException {
		return buffer.readable() > 0;
	}

	@Override
	public int read(char[] chars, int offset, int length) throws IOException {
		if (offset < 0 || offset >= chars.length) {
			throw new IndexOutOfBoundsException(offset);
		}
		if (length < 0 || (length += offset) > chars.length) {
			throw new IndexOutOfBoundsException(length);
		}
		int size = offset;
		while (offset < length && buffer.readable() > 0) {
			offset += Character.toChars(buffer.readUTF8(), chars, offset);
		}
		return offset - size;
	}

	@Override
	public int read() throws IOException {
		return buffer.readUTF8();
	}

	@Override
	public long skip(long n) throws IOException {
		long size = 0;
		while (size < n && buffer.readable() > 0) {
			if (Character.isBmpCodePoint(buffer.readUTF8())) {
				size++;
			} else {
				size += 2;
			}
		}
		return size;
	}

	@Override
	public long transferTo(Writer out) throws IOException {
		int code;
		long size = 0;
		char[] chars = new char[2];
		while (buffer.readable() > 0) {
			code = buffer.readUTF8();
			code = Character.toChars(code, chars, 0);
			out.write(chars, 0, code);
			size += code;
		}
		return size;
	}

	@Override
	public void close() throws IOException {
		if (release) {
			buffer.release();
		}
	}

	public DataBuffer buffer() {
		return buffer;
	}
}