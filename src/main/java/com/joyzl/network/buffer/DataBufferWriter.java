/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.buffer;

import java.io.IOException;
import java.io.Writer;

/**
 * 包装DataBuffer为Writer，字符编码为UTF-8
 * <p>
 * 此字符读取类继承并实现 java.io.Writer ，同时取消了父类中所有同步锁和中间缓存（DataBuffer本身就是缓存）。
 * </p>
 * 
 * @author ZhangXi 2025年6月23日
 */
public class DataBufferWriter extends Writer {

	private final DataBuffer buffer;

	public DataBufferWriter() {
		this.buffer = DataBuffer.instance();
	}

	public DataBufferWriter(DataBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void write(char[] chars, int offset, int length) throws IOException {
		if (offset < 0 || offset >= chars.length) {
			throw new IndexOutOfBoundsException(offset);
		}
		if (length < 0 || (length += offset) > chars.length) {
			throw new IndexOutOfBoundsException(length);
		}
		char c;
		for (; offset < length; offset++) {
			c = chars[offset];
			if (Character.isSurrogate(c)) {
				if (++offset < length) {
					buffer.writeUTF8(Character.toCodePoint(c, chars[offset]));
				} else {
					throw new IOException("代理字符被截断");
				}
			} else {
				buffer.writeUTF8((int) c);
			}
		}
	}

	@Override
	public void write(String string, int offset, int length) throws IOException {
		if (offset < 0 || offset >= string.length()) {
			throw new IndexOutOfBoundsException(offset);
		}
		if (length < 0 || (length += offset) > string.length()) {
			throw new IndexOutOfBoundsException(length);
		}
		char c;
		for (; offset < length; offset++) {
			c = string.charAt(offset);
			if (Character.isSurrogate(c)) {
				if (++offset < length) {
					buffer.writeUTF8(Character.toCodePoint(c, string.charAt(offset)));
				} else {
					throw new IOException("代理字符被截断");
				}
			} else {
				buffer.writeUTF8((int) c);
			}
		}
	}

	@Override
	public void write(int c) throws IOException {
		buffer.writeUTF8((char) c);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		buffer.writeUTF8(csq);
		return this;
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		if (start < 0 || start >= csq.length()) {
			throw new IndexOutOfBoundsException(start);
		}
		if (end < 0 || end > csq.length()) {
			throw new IndexOutOfBoundsException(end);
		}
		char c;
		for (; start < end; start++) {
			c = csq.charAt(start);
			if (Character.isSurrogate(c)) {
				if (++start < end) {
					buffer.writeUTF8(Character.toCodePoint(c, csq.charAt(start)));
				} else {
					throw new IOException("代理字符被截断");
				}
			} else {
				buffer.writeUTF8((int) c);
			}
		}
		return this;
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	public DataBuffer buffer() {
		return buffer;
	}
}