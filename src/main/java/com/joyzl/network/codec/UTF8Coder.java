package com.joyzl.network.codec;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 编码字符串为UTF8字节；或从UTF8字节解码为字符串。
 * 
 * @author ZhangXi 2025年6月12日
 */
public class UTF8Coder {

	/*-
	 * UTF-8
	 * 
	 * U+0000 ~ U+007F
	 * 0xxxxxxx (1Byte)
	 * 
	 * U+0080 ~ U+07FF
	 * 110xxxxx 10xxxxxx (2Byte)
	 * 
	 * U+0800 ~ U+FFFF
	 * 1110xxxx 10xxxxxx 10xxxxxx (3Byte)
	 * 
	 * U+10000 ~ U+10FFFF
	 * 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx (4Byte)
	 */

	/**
	 * 编码字符串为UTF8字节串
	 */
	public static DataBuffer encode(CharSequence chars) throws IOException {
		final DataBuffer buffer = DataBuffer.instance();
		encode(chars, buffer);
		return buffer;
	}

	/**
	 * 编码字符串为UTF8字节串
	 */
	public static void encode(CharSequence chars, DataBuffer buffer) throws IOException {
		int code;
		for (int index = 0; index < chars.length(); index++) {
			code = chars.charAt(index);

			// 处理高代理项(Surrogate High)
			if (code >= 0xD800 && code <= 0xDBFF) {
				// 检查是否存在低代理项(Surrogate Low)
				if (index + 1 < chars.length()) {
					char low = chars.charAt(index += 1);
					if (low >= 0xDC00 && low <= 0xDFFF) {
						// 组合为完整码点(U+10000 到 U+10FFFF)
						int codePoint = ((code - 0xD800) << 10) + (low - 0xDC00) + 0x10000;
						// 编码 4 字节 UTF-8(U+10000 到 U+10FFFF)
						buffer.write(0xF0 | (codePoint >> 18));
						buffer.write(0x80 | ((codePoint >> 12) & 0x3F));
						buffer.write(0x80 | ((codePoint >> 6) & 0x3F));
						buffer.write(0x80 | (codePoint & 0x3F));
						continue;
					}
				} else {
					// 不完整的代理对或非法代理项
					// 按普通字符处理（可能导致乱码）
				}
			}

			if (code <= 0x7F) {
				buffer.write(code);
			} else if (code <= 0x7FF) {
				buffer.write(0xC0 | (code >> 6));
				buffer.write(0x80 | (code & 0x3F));
			} else if (code <= 0xFFFF) {
				buffer.write(0xE0 | (code >> 12));
				buffer.write(0x80 | ((code >> 6) & 0x3F));
				buffer.write(0x80 | (code & 0x3F));
			} else if (code <= 0x10FFFF) {
				buffer.write(0xF0 | (code >> 18));
				buffer.write(0x80 | ((code >> 12) & 0x3F));
				buffer.write(0x80 | ((code >> 6) & 0x3F));
				buffer.write(0x80 | (code & 0x3F));
			} else {
				continue;
			}
		}
	}

	/**
	 * 解码UTF8字节串为字符串
	 */
	public static CharSequence decodeUTF8(DataBuffer buffer) {
		final StringBuilder chars = new StringBuilder();
		decodeUTF8(buffer, chars);
		return chars;
	}

	/**
	 * 解码UTF8字节串为字符串
	 * 
	 * @return false 需要更多输入字节
	 */
	public static boolean decodeUTF8(DataBuffer buffer, StringBuilder chars) {
		int code;
		while (buffer.readable() > 0) {
			code = buffer.get(0);
			if ((code & 0x80) == 0) {
				chars.append((char) buffer.readByte());
			} else if ((code & 0xE0) == 0xC0) {
				if (buffer.readable() > 0) {
					code = (buffer.readByte() & 0x1F) << 6;
					code |= (buffer.readByte() & 0x3F);
					chars.append((char) code);
				} else {
					break;
				}
			} else if ((code & 0xF0) == 0xE0) {
				if (buffer.readable() > 1) {
					code = (buffer.readByte() & 0x0F) << 12;
					code |= (buffer.readByte() & 0x3F) << 6;
					code |= (buffer.readByte() & 0x3F);
					chars.append((char) code);
				} else {
					break;
				}
			} else if ((code & 0xF8) == 0xF0) {
				if (buffer.readable() > 2) {
					code = (buffer.readByte() & 0x07) << 18;
					code |= (buffer.readByte() & 0x3F) << 12;
					code |= (buffer.readByte() & 0x3F) << 6;
					code |= (buffer.readByte() & 0x3F);
					if (code >= 0x010000 && code <= 0x10FFFF) {
						// 处理代理对
						int high = ((code - 0x010000) >> 10) + 0xD800;
						int low = ((code - 0x010000) & 0x3FF) + 0xDC00;
						chars.append((char) high);
						chars.append((char) low);
					} else {
						// 无效代理对
						chars.append('\uFFFD');
					}
				} else {
					break;
				}
			} else {
				// 非法字节
				chars.append('\uFFFD');
			}
		}
		return buffer.readable() > 0;
	}
}