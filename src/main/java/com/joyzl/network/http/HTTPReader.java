/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 超文本读取
 * 
 * @author ZhangXi
 * @date 2021年10月5日
 */
public class HTTPReader extends Reader {

	int c;
	private final DataBuffer buffer;
	private final StringBuilder builder;

	public HTTPReader(DataBuffer b) {
		builder = new StringBuilder();
		buffer = b;
	}

	@Override
	public int read() throws IOException {
		return buffer.readUnsignedByte();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int e = 0;
		while (len > 0 && (c = read()) >= 0) {
			cbuf[off + e] = (char) c;
			len--;
			e++;
		}
		return e;
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		buffer.mark();
	}

	public void mark() throws IOException {
		mark(0);
	}

	@Override
	public void reset() throws IOException {
		buffer.reset();
	}

	@Override
	public void close() throws IOException {
	}

	/**
	 * 最后读取的字符
	 */
	public char last() {
		return (char) c;
	}

	/**
	 * 最后读取的第二个字符
	 */
	public char previous() {
		return builder.charAt(builder.length() - 1);
	}

	/**
	 * 获取已读取字符的字符串对象
	 */
	public String string() {
		return builder.toString();
	}

	/**
	 * 获取已读取字符的字符序列对象
	 */
	public CharSequence sequence() {
		return builder;
	}

	public DataBuffer buffer() {
		return buffer;
	}

	/**
	 * 跳过空白字符
	 */
	public void skipWhitespace() {
		while (buffer.readable() > 0) {
			if (Character.isWhitespace(buffer.get(0))) {
				buffer.readByte();
			} else {
				break;
			}
		}
	}

	/**
	 * 读取任意字符直到结束字符
	 * 
	 * @param end 结束字符
	 * @return true 成功于结束字符/false 字符流结束
	 * @throws IOException
	 */
	public boolean readTo(final char end) throws IOException {
		builder.setLength(0);
		while (buffer.readable() > 0) {
			c = read();
			if (c == end) {
				return true;
			}
			builder.append((char) c);
		}
		return false;
	}

	/**
	 * 读取任意字符直到结束字符,期望end1,可能是end2
	 * 
	 * @param end1 结束字符1
	 * @param end2 结束字符2
	 * @return true 成功于结束字符/false 字符流结束
	 * @throws IOException
	 */
	public boolean readTo(final char end1, final char end2) throws IOException {
		builder.setLength(0);
		while (buffer.readable() > 0) {
			c = read();
			if (c == end1 || c == end2) {
				return true;
			}
			builder.append((char) c);
		}
		return false;
	}

	/**
	 * 读取任意字符直到结束字符,期望end1,可能是end2
	 * 
	 * @param end1 结束字符1
	 * @param end2 结束字符串2
	 * @return true 成功于结束字符/false 字符流结束
	 * @throws IOException
	 */
	public boolean readTo(final char end1, final CharSequence end2) throws IOException {
		int e = 0;
		builder.setLength(0);
		while (buffer.readable() > 0) {
			c = read();
			if (c == end1) {
				return true;
			} else if (c == end2.charAt(e)) {
				e++;
				if (e >= end2.length()) {
					return true;
				} else {
					continue;
				}
			} else if (e > 0) {
				builder.append(end2, 0, e);
				e = 0;
			}
			builder.append((char) c);
		}
		return false;
	}

	/**
	 * 读取字符直到结束字符串
	 * 
	 * @param end 结束字符串
	 * @return 成功于结束字符串/false 字符流结束
	 * @throws IOException
	 */
	public boolean readTo(final CharSequence end) throws IOException {
		int e = 0;
		builder.setLength(0);
		while (buffer.readable() > 0) {
			c = read();
			if (c == end.charAt(e)) {
				e++;
				if (e >= end.length()) {
					return true;
				} else {
					continue;
				}
			} else if (e > 0) {
				builder.append(end, 0, e);
				e = 0;
			}
			builder.append((char) c);
		}
		return false;
	}

	/**
	 * 读取字符直到结束字符串,必须同时满足end1紧接着end2
	 * 
	 * @param end1 结束字符1
	 * @param end2 结束字符串2
	 * @return 成功于结束字符串/false 字符流结束
	 * @throws IOException
	 */
	public boolean readAt(final char end1, final CharSequence end2) throws IOException {
		int e = 0;
		builder.setLength(0);
		while (buffer.readable() > 0) {
			c = read();
			if (e >= 1) {
				if (c == end2.charAt(e - 1)) {
					e++;
					if (e >= 1 + end2.length()) {
						return true;
					} else {
						continue;
					}
				} else {
					builder.append(end1);
					builder.append(end2, 0, e - 1);
					e = 0;
				}
			}
			if (c == end1) {
				e = 1;
				continue;
			}
			builder.append((char) c);
		}
		return false;
	}

	/**
	 * 读取字符直到结束字符串,必须同时满足end1紧接着end2
	 * 
	 * @param end1 结束字符串1
	 * @param end2 结束字符串2
	 * @return true 成功于结束字符串/false 字符流结束
	 * @throws IOException
	 */
	public boolean readAt(final CharSequence end1, final CharSequence end2) throws IOException {
		int e = 0;
		builder.setLength(0);
		while (buffer.readable() > 0) {
			c = read();
			if (e >= end1.length()) {
				if (c == end2.charAt(e - end1.length())) {
					e++;
					if (e >= end1.length() + end2.length()) {
						return true;
					} else {
						continue;
					}
				} else {
					builder.append(end1);
					builder.append(end2, 0, e - end1.length());
					e = 0;
				}
			}
			if (c == end1.charAt(e)) {
				e++;
				continue;
			} else if (e > 0) {
				builder.append(end1, 0, e);
				e = 0;
			}
			builder.append((char) c);
		}
		return false;
	}

	/**
	 * 读取字节到输出流直到指定的结束字符串
	 * 
	 * @param output 输出流
	 * @param end 结束字符串，只能是单字节ASCII编码字符
	 * @return true 成功于结束字符串/false 字符流结束
	 * @throws IOException
	 */
	public boolean readBy(final OutputStream output, final CharSequence end) throws IOException {
		int value, e = 0;
		while (buffer.readable() > 0) {
			value = read();
			if (value == end.charAt(e)) {
				e++;
				if (e >= end.length()) {
					return true;
				} else {
					continue;
				}
			} else if (e > 0) {
				for (int index = 0; index < e; index++) {
					output.write(end.charAt(index));
				}
				e = 0;
			}
			output.write(value);
		}
		return false;
	}

	/**
	 * 读取字节到输出流直到指定的结束字符串,必须同时满足end1紧接着end2
	 * 
	 * @param output 输出流
	 * @param end1 结束字符串，只能是单字节ASCII编码字符
	 * @param end2 结束字符串，只能是单字节ASCII编码字符
	 * @return true 成功于结束字符串/false 字符流结束
	 * @throws IOException
	 */
	public boolean readBy(final OutputStream output, final CharSequence end1, final CharSequence end2) throws IOException {
		int value, e = 0;
		builder.setLength(0);
		while (buffer.readable() >= 0) {
			value = read();
			if (e >= end1.length()) {
				if (value == end2.charAt(e - end1.length())) {
					e++;
					if (e >= end1.length() + end2.length()) {
						return true;
					} else {
						continue;
					}
				} else {
					for (int index = 0; index < end1.length(); index++) {
						output.write(end1.charAt(index));
					}
					e -= end1.length();
					for (int index = 0; index < e; index++) {
						output.write(end1.charAt(index));
					}
					e = 0;
				}
			}
			if (value == end1.charAt(e)) {
				e++;
				continue;
			} else if (e > 0) {
				for (int index = 0; index < e; index++) {
					output.write(end1.charAt(index));
				}
				e = 0;
			}
			output.write(value);
		}
		return false;
	}

	@Override
	public String toString() {
		builder.setLength(0);
		buffer.mark();
		while (buffer.readable() > 0) {
			c = buffer.readByte() & 0xFF;
			if (c == HTTPCoder.CR) {
				builder.append("CR");
			} else if (c == HTTPCoder.LF) {
				builder.append("LF");
			}
			builder.append((char) c);
		}
		buffer.reset();
		return builder.toString();
	}
}