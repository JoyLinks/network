/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

/**
 * 实用方法集
 * 
 * @author ZhangXi
 * @date 2023年9月4日
 */
public class Utility {

	/**
	 * value == null || value.length == 0
	 */
	public static boolean isEmpty(CharSequence value) {
		return value == null || value.length() == 0;
	}

	/**
	 * value != null && value.length > 0
	 */
	public static boolean noEmpty(CharSequence value) {
		return value != null && value.length() > 0;
	}

	/**
	 * 比较字符串相同（不区分大小写），允许空(null)字符串
	 */
	public final static boolean same(CharSequence a, CharSequence b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		if (a == b || a.isEmpty() && b.isEmpty()) {
			return true;
		}
		if (a.length() != b.length()) {
			return false;
		}
		int ca, cb;
		for (int index = 0; index < a.length(); index++) {
			ca = a.charAt(index);
			cb = b.charAt(index);
			if (ca != cb && (ca ^ cb) != 32) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 比较字符串相等（不区分大小写），允许空(null)字符串
	 */
	public final static boolean same(CharSequence a, CharSequence b, int start, int done) {
		if (a == null || b == null) {
			return false;
		}
		if (done - start != a.length() || done > b.length()) {
			return false;
		}
		int ca, cb;
		for (int index = 0; index < a.length(); index++, start++) {
			ca = a.charAt(index);
			cb = b.charAt(start);
			if (ca != cb && (ca ^ cb) != 32) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 比较字符串相等（区分大小写），允许空(null)字符串
	 */
	public final static boolean equal(CharSequence a, CharSequence b) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		if (a == b || a.isEmpty() && b.isEmpty()) {
			return true;
		}
		if (a.length() != b.length()) {
			return false;
		}
		for (int index = 0; index < a.length(); index++) {
			if (a.charAt(index) != b.charAt(index)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 比较字符串相等（区分大小写），允许空(null)字符串
	 */
	public final static boolean equal(CharSequence a, CharSequence b, int start, int done) {
		if (a == null || b == null) {
			return false;
		}
		if (done - start != a.length() || done > b.length()) {
			return false;
		}
		for (int index = 0; index < a.length(); index++, start++) {
			if (a.charAt(index) != b.charAt(start)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 比较字符串是否指定结束串，允许空(null)字符串
	 * 
	 * @param value 原字符串
	 * @param end 结束字符串
	 * @param ignore_case 是否忽略大小写
	 * @return true / false
	 */
	public static boolean ends(CharSequence value, CharSequence end, boolean ignore_case) {
		if (value == null || end == null) {
			return false;
		}
		if (value.length() < end.length()) {
			return false;
		}
		for (int begin = value.length() - end.length(), index = 0; index < end.length(); index++) {
			if (value.charAt(begin + index) != end.charAt(index)) {
				if (ignore_case) {
					if (Character.toLowerCase(value.charAt(begin + index)) != Character.toLowerCase(end.charAt(index))) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * [0x0A,0x1A] -> "0a1a"
	 */
	public static String hex(byte[] data) {
		final StringBuilder b = new StringBuilder(data.length * 2);
		hex(data, b);
		return b.toString();
	}

	/**
	 * [0x0A,0x1A] -> prefix + "0a1a" + suffix
	 */
	public static String hex(CharSequence prefix, byte[] data, CharSequence suffix) {
		final StringBuilder b = new StringBuilder(prefix.length() + data.length * 2 + suffix.length());
		b.append(prefix);
		hex(data, b);
		b.append(suffix);
		return b.toString();
	}

	/**
	 * [0x0A,0x1A] -> "0a1a"
	 */
	public static void hex(byte[] data, StringBuilder builder) {
		for (int value, i = 0; i < data.length; i++) {
			value = data[i] & 0xFF;
			builder.append(Character.forDigit(value / 16, 16));
			builder.append(Character.forDigit(value % 16, 16));
		}
	}

	/**
	 * "0a1a" -> [0x0A,0x1A]
	 */
	public static byte[] hex(String data) {
		return hex(data, 0, data.length());
	}

	/**
	 * "0a1a" -> [0x0A,0x1A]
	 */
	public static byte[] hex(String data, int offset, int length) {
		final byte[] temp = new byte[(length - offset) / 2];
		int value;
		for (int i = 0; i < temp.length; i++) {
			value = Character.digit(data.charAt(offset++), 16) * 16;
			value += Character.digit(data.charAt(offset++), 16);
			temp[i] = (byte) value;
		}
		return temp;
	}
}