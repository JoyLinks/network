/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network;

/**
 * 实用方法集
 * 
 * @author ZhangXi
 * @date 2023年9月4日
 */
public class Utility {

	private static final ThreadLocal<StringBuilder> THREAD_STRING_BUILDER = new ThreadLocal<StringBuilder>() {
		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder(512);
		}
	};

	public static StringBuilder getStringBuilder() {
		final StringBuilder b = THREAD_STRING_BUILDER.get();
		b.setLength(0);
		return b;
	}

	public static boolean isEmpty(CharSequence value) {
		return value == null || value.length() == 0;
	}

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

	public static String hex(byte[] data) {
		return hex(data, "", "");
	}

	public static String hex(byte[] data, CharSequence prefix, CharSequence suffix) {
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append(prefix);
		for (int value, i = 0; i < data.length; i++) {
			value = data[i] & 0xFF;
			builder.append(Character.forDigit(value / 16, 16));
			builder.append(Character.forDigit(value % 16, 16));
		}
		builder.append(suffix);
		return builder.toString();
	}
}