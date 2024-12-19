package com.joyzl.network.http;

import java.util.Arrays;

/**
 * 字符串常量查找，不区分大小写
 * 
 * @deprecated {@link Seeker}
 * @author ZhangXi 2024年11月22日
 */
@Deprecated
public class Seeker1 {

	private final String[] constants;
	private final int min, max;

	public Seeker1(String[] constants) {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int index = 0; index < constants.length; index++) {
			if (constants[index] == null || constants[index].length() == 0) {
				throw new IllegalArgumentException("不能有空字符串");
			}
			if (constants[index].length() < min) {
				min = constants[index].length();
			}
			if (constants[index].length() > max) {
				max = constants[index].length();
			}
		}
		Arrays.sort(constants, String.CASE_INSENSITIVE_ORDER);
		// for (int index = 0; index < constants.length; index++) {
		// System.out.println(constants[index]);
		// }
		this.constants = constants;
		this.min = min;
		this.max = max;
	}

	public String get(CharSequence chars) {
		// 二分法
		if (chars.length() >= min() && chars.length() <= max()) {
			int i = 0, c, t;
			int p = 0, ben = 0, end = constants().length;
			int _ben, _end;

			do {
				c = chars.charAt(i);
				// 1查找目标值
				while (ben < end) {
					p = ben + (end - ben) / 2;
					if (i >= constants()[p].length()) {
						// 字符串长度不足时，视为空字符0
						ben = p + 1;
					} else {
						t = constants()[p].charAt(i);
						if (c == t || (t ^ c) == 32) {
							// 缩小范围
							if (end - ben > 1) {
								_end = p;
								_ben = p;
								// ben ~ p
								while (ben < _end) {
									p = ben + (_end - ben) / 2;
									if (i >= constants()[p].length()) {
										ben = p + 1;
									} else {
										t = constants()[p].charAt(i);
										if (c == t || (t ^ c) == 32) {
											_end = p;
										} else {
											ben = p + 1;
										}
									}
								}
								// p ~ end
								while (_ben < end) {
									p = _ben + (end - _ben) / 2;
									if (i >= constants()[p].length()) {
										throw new IllegalStateException("XXX");
									} else {
										t = constants()[p].charAt(i);
										if (c == t || (t ^ c) == 32) {
											_ben = p + 1;
										} else {
											end = p;
										}
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
				}
			} while (++i < chars.length());

			// System.out.println(chars);
			// System.out.println("size:" + size);

			while (ben < end) {
				if (chars.length() == constants()[ben].length()) {
					return constants()[ben];
				}
				ben++;
			}
		}
		return chars.toString();
	}

	public String take(CharSequence chars) {
		if (chars.length() >= min() && chars.length() <= max()) {
			int i = 0, c, t;
			int p = 0, ben = 0, end = constants().length;
			int _ben, _end;

			do {
				c = chars.charAt(i);
				// 1查找目标值
				while (ben < end) {
					p = ben + (end - ben) / 2;
					if (i >= constants()[p].length()) {
						// 字符串长度不足时，视为空字符0
						ben = p + 1;
					} else {
						t = constants()[p].charAt(i);
						if (c == t || (t ^ c) == 32) {
							// 缩小范围
							if (end - ben > 1) {
								_end = p;
								_ben = p;
								// ben ~ p
								while (ben < _end) {
									p = ben + (_end - ben) / 2;
									if (i >= constants()[p].length()) {
										ben = p + 1;
									} else {
										t = constants()[p].charAt(i);
										if (c == t || (t ^ c) == 32) {
											_end = p;
										} else {
											ben = p + 1;
										}
									}
								}
								// p ~ end
								while (_ben < end) {
									p = _ben + (end - _ben) / 2;
									if (i >= constants()[p].length()) {
										throw new IllegalStateException("XXX");
									} else {
										t = constants()[p].charAt(i);
										if (c == t || (t ^ c) == 32) {
											_ben = p + 1;
										} else {
											end = p;
										}
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
				}
			} while (++i < chars.length());
			while (ben < end) {
				if (chars.length() == constants()[ben].length()) {
					return constants()[ben];
				}
				ben++;
			}
		}
		return null;
	}

	public String get1(CharSequence s) {
		// 朴素法
		if (s.length() >= min() && s.length() <= max()) {
			int ben = 0, i = 0, c;
			int end = constants().length - 1;
			while (i < s.length()) {
				c = s.charAt(i);
				for (; ben <= end; ben++) {
					if (s.length() == constants()[ben].length()) {
						if (constants()[ben].charAt(i) == c || (constants()[ben].charAt(i) ^ c) == 32) {
							break;
						}
					}
				}
				for (; end > ben; end--) {
					if (s.length() == constants()[end].length()) {
						if (constants()[end].charAt(i) == c || (constants()[end].charAt(i) ^ c) == 32) {
							break;
						}
					}
				}
				i++;
			}
			if (ben <= end && i == constants()[ben].length()) {
				return constants()[ben];
			}
		}
		return s.toString();
	}

	public String[] constants() {
		return constants;
	}

	public int size() {
		return constants.length;
	}

	public int max() {
		return max;
	}

	public int min() {
		return min;
	}
}