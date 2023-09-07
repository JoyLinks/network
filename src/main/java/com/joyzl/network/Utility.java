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
public final class Utility {
	public final static String EMPTY_STRIN = "";

	public static boolean isEmpty(CharSequence value) {
		return value == null || value.length() == 0;
	}

	public static boolean noEmpty(CharSequence value) {
		return value != null && value.length() > 0;
	}

	/**
	 * 比较字符串相等，允许空(null)字符串
	 * 
	 * @param a 字符串序列
	 * @param b 字符串序列
	 * @param ignore_case 是否忽略大小写
	 * @return true / false
	 */
	public final static boolean equals(CharSequence a, CharSequence b, boolean ignore_case) {
		if (a == null) {
			return b == null;
		}
		if (b == null) {
			return false;
		}
		if (a == b) {
			return true;
		} else {
			if (a.length() == b.length()) {
				for (int index = 0; index < a.length(); index++) {
					if (a.charAt(index) != b.charAt(index)) {
						if (ignore_case) {
							if (Character.toLowerCase(a.charAt(index)) != Character.toLowerCase(b.charAt(index))) {
								return false;
							}
						} else {
							return false;
						}
					}
				}
				return true;
			} else {
				return false;
			}
		}
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
}