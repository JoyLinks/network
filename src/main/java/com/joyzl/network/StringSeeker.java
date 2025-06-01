package com.joyzl.network;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 字符串常量查找，不区分大小写；
 * <p>
 * 对于类似于HTTP-Header这样的字符串，可通过预定义建立Seeker对象，在解码时通过CharSequence获取字符串实例，以减少频繁创建字符串对象；
 * 如果字符串常量长度大量相同，那么使用此类不会获得额外优势。
 * </p>
 * 
 * @author ZhangXi 2024年11月22日
 */
public class StringSeeker implements Iterable<String> {

	/** 相同长度分组，每组按字符排序 */
	private final String[][] constants;

	public StringSeeker(String[] strings) {
		// 字符串排序（长度，字符）
		Arrays.sort(strings, LENGTH_ORDER);
		String[] group = new String[strings.length];
		String[][] constants = new String[strings.length][];
		int g = -1, c = 0;
		for (int s = 0; s < strings.length; s++) {
			if (strings[s].length() == 0) {
				throw new IllegalArgumentException("不能有空字符串");
			}
			if (g < 0) {
				group[++g] = strings[s];
			} else if (group[g].length() == strings[s].length()) {
				group[++g] = strings[s];
			} else {
				constants[c++] = Arrays.copyOfRange(group, 0, g + 1);
				group[g = 0] = strings[s];
			}
		}
		if (g >= 0) {
			constants[c++] = Arrays.copyOfRange(group, 0, g + 1);
		}
		this.constants = Arrays.copyOf(constants, c);
	}

	/**
	 * 获取匹配的字符串常量实例，如果没有匹配项将返回当前实例
	 */
	public String get(CharSequence chars) {
		final String[] constants = dichotomy(chars.length());
		if (constants == null) {
			return chars.toString();
		}
		// final String constant = dichotomy(constants, chars);
		final String constant = simple(constants, chars);
		if (constant != null) {
			return constant;
		}
		return chars.toString();
	}

	/**
	 * 获取匹配的字符串常量实例，如果没有匹配项将返回null
	 */
	public String take(CharSequence chars) {
		final String[] constants = dichotomy(chars.length());
		if (constants == null) {
			return null;
		}
		// return dichotomy(constants, chars);
		return simple(constants, chars);
	}

	/**
	 * 检查是否存在指定的字符串常量实例
	 */
	public boolean contains(CharSequence chars) {
		final String[] constants = dichotomy(chars.length());
		if (constants == null) {
			return false;
		}
		return simple(constants, chars) != null;
	}

	/**
	 * 查找指定长度的字符串常量组
	 */
	private String[] dichotomy(int length) {
		// 二分法查找指定长度的字符串组

		int l, index = 0, begin = 0, end = constants.length;
		while (begin < end) {
			index = begin + (end - begin) / 2;
			l = constants[index][0].length();
			if (l == length) {
				return constants[index];
			} else if (l < length) {
				begin = index + 1;
			} else {
				end = index;
			}
		}
		return null;
	}

	/**
	 * 二分法匹配字符串，分组后与顺序法优势已不明显，但是较复杂
	 * 
	 * @param constants
	 * @param chars
	 * @return
	 */
	@Deprecated
	String dichotomy(String[] constants, CharSequence chars) {
		int i = 0, c, t;
		int p = 0, ben = 0, end = constants.length;
		int _ben, _end;

		do {
			c = chars.charAt(i);
			// 1查找目标值
			while (ben < end) {
				p = ben + (end - ben) / 2;
				t = constants[p].charAt(i);
				if (c == t || (t ^ c) == 32) {
					// 缩小范围
					if (end - ben > 1) {
						_end = p;
						_ben = p;
						// ben ~ p
						while (ben < _end) {
							p = ben + (_end - ben) / 2;
							t = constants[p].charAt(i);
							if (c == t || (t ^ c) == 32) {
								_end = p;
							} else {
								ben = p + 1;
							}
						}
						// p ~ end
						while (_ben < end) {
							p = _ben + (end - _ben) / 2;
							t = constants[p].charAt(i);
							if (c == t || (t ^ c) == 32) {
								_ben = p + 1;
							} else {
								end = p;
							}
						}
					}
					break;
				}
				// A(65)~Z(90) < a(97)~z(122)
				// c = Character.toUpperCase(c);
				if (c >= 97 && c <= 122) {
					c -= 32;
				}
				// t = Character.toUpperCase(t);
				if (t >= 97 && t <= 122) {
					t -= 32;
				}
				if (c < t) {
					end = p;
				} else if (c > t) {
					ben = p + 1;
				}
			}
		} while (++i < chars.length());

		while (ben < end) {
			if (chars.length() == constants[ben].length()) {
				return constants[ben];
			}
			ben++;
		}

		return null;
	}

	/**
	 * 顺序查找匹配字符串
	 */
	private String simple(String[] constants, CharSequence chars) {
		int index = 0, c = 0, c1, c2;
		while (true) {
			c1 = chars.charAt(index);
			c2 = constants[c].charAt(index);
			if (c1 == c2 || (c1 ^ c2) == 32) {
				if (++index >= chars.length()) {
					return constants[c];
				}
			} else {
				if (++c >= constants.length) {
					return null;
				}
			}
		}
	}

	/**
	 * 获取常量字符串数量，此方法将遍历常量组计算总数
	 */
	public int size() {
		int size = 0;
		for (int index = 0; index < constants.length; index++) {
			size += constants[index].length;
		}
		return size;
	}

	/**
	 * 获取常量字符串最大长度
	 */
	public int maxLength() {
		if (constants.length > 0) {
			return constants[constants.length - 1][0].length();
		}
		return 0;
	}

	/**
	 * 获取常量字符串最小长度
	 */
	public int minLength() {
		if (constants.length > 0) {
			return constants[0][0].length();
		}
		return 0;
	}

	/**
	 * 字符串排序：长度，字符
	 */
	final static Comparator<String> LENGTH_ORDER = new Comparator<String>() {
		@Override
		public int compare(String a, String b) {
			if (a.length() == b.length()) {
				return String.CASE_INSENSITIVE_ORDER.compare(a, b);
			}
			return a.length() - b.length();
		}
	};

	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			private int d1 = 0, d2 = 0;

			@Override
			public boolean hasNext() {
				if (d1 < constants.length) {
					if (d2 < constants[d1].length) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String next() {
				final String value = constants[d1][d2++];
				if (d2 >= constants[d1].length) {
					d2 = 0;
					d1++;
				}
				return value;
			}
		};
	}
}