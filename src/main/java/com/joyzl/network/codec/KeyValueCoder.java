/*
 * 版权所有 重庆骄智科技有限公司 保留所有权利
 * Copyright © 2020-2025 All rights reserved. 
 * www.joyzl.com
 */
package com.joyzl.network.codec;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 
 * 键值对编码，值支持 UTF-8 字符
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2026年2月27日
 */
public class KeyValueCoder {

	// name=value,
	// = 转义 [0x10 =]
	// , 转义 [0x10 ,]

	final static char ESCAPE = 0x10;

	public static void encode(Map<?, ?> parameters, DataBuffer buffer) throws IOException {
		int size = 0;
		for (Entry<?, ?> entry : parameters.entrySet()) {
			if (size > 0) {
				buffer.writeASCII(',');
			}
			encode(entry.getKey().toString(), buffer);
			buffer.writeASCII('=');
			if (entry.getValue() != null) {
				encode(entry.getValue().toString(), buffer);
			}
			size++;
		}
	}

	private static void encode(CharSequence chars, DataBuffer buffer) throws IOException {
		char c;
		for (int i = 0; i < chars.length(); i++) {
			c = chars.charAt(i);
			if (c == '=') {
				buffer.writeASCII(ESCAPE);
				buffer.writeASCII('=');
			} else if (c == ',') {
				buffer.writeASCII(ESCAPE);
				buffer.writeASCII(',');
			} else {
				buffer.writeUTF8(c);
			}
		}
	}

	public static void decode(Map<String, String> parameters, DataBuffer buffer) throws IOException {
		int c;
		String name = null;
		final StringBuilder s = new StringBuilder();
		while (buffer.readable() > 0) {
			c = buffer.readUTF8();
			if (c == ESCAPE) {
				s.appendCodePoint(buffer.readUTF8());
			} else if (c == '=') {
				name = s.toString();
				s.setLength(0);
			} else if (c == ',') {
				parameters.put(name, s.toString());
				s.setLength(0);
				name = null;
			} else {
				s.appendCodePoint(c);
			}
		}
		if (name != null) {
			parameters.put(name, s.toString());
		}
	}
}