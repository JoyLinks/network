/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 缓存仓库,仅有一个实例的缓存对象,支持通配符*匹配,仅能单个通配符
 * 
 * <p>
 * 匹配有效降：全字符匹配 > 前缀匹配 > 后缀匹配 > 前缀和后缀匹配<br>
 * 全字符匹配："/abcd.html"<br>
 * 前缀匹配： "/abcd.*"<br>
 * 后缀匹配： "/*.html"<br>
 * 前缀和后缀匹配："/test/*.html"<br>
 * 
 * @author ZhangXi
 * @date 2020年10月29日
 */
public final class Wildcards<T> {

	/** 通配符 */
	public static final char ANY = '*';

	private Item<T>[] ITEMS;

	@SuppressWarnings("unchecked")
	public Wildcards() {
		ITEMS = (Item<T>[]) Array.newInstance(Item.class, 0);
	}

	public final T find(String key) {
		Item<T> item;
		for (int index = 0; index < ITEMS.length; index++) {
			item = ITEMS[index];
			if (item.match(key)) {
				return item.t;
			}
		}
		return null;
	}

	public final void bind(String text, T target) {
		ITEMS = Arrays.copyOf(ITEMS, ITEMS.length + 1);
		ITEMS[ITEMS.length - 1] = new Item<T>(target, text);
		Arrays.sort(ITEMS, Item.COMPARATOR);
	}

	/**
	 * 获取所有绑定项,修改返回的集合不会影响此对象实例内部绑定关系
	 */
	public final List<T> elements() {
		final List<T> items = new ArrayList<>(ITEMS.length);
		for (int index = 0; index < ITEMS.length; index++) {
			items.add(ITEMS[index].t);
		}
		return items;
	}

	static class Item<T> {
		final boolean wildcard;
		final String prefix;
		final String suffix;
		final T t;

		public Item(T t, String text) {
			this.t = t;

			int index = text.indexOf(ANY);
			if (index > 0) {
				wildcard = true;
				if (index + 1 == text.length()) {
					// /actions/*
					prefix = text.substring(0, index);
					suffix = null;
				} else {
					// /actions/*.do
					prefix = text.substring(0, index);
					suffix = text.substring(index + 1);
				}
			} else if (index == 0) {
				// *.html
				// *.do
				wildcard = true;
				prefix = null;
				suffix = text.substring(index + 1);
			} else {
				// /action.html
				wildcard = false;
				prefix = suffix = text;
			}
		}

		@Override
		public String toString() {
			if (wildcard) {
				if (suffix == null) {
					return prefix + ANY;
				}
				if (prefix == null) {
					return ANY + suffix;
				}
				return prefix + ANY + prefix;
			}
			return prefix;
		}

		final boolean match(String key) {
			if (wildcard) {
				if (suffix == null) {
					// 前缀匹配
					if (prefix == key) {
						return true;
					}
					if (prefix.length() <= key.length()) {
						for (int index = 0; index < prefix.length(); index++) {
							if (prefix.charAt(index) != key.charAt(index)) {
								return false;
							}
						}
						return true;
					} else {
						return false;
					}
				} else if (prefix == null) {
					// 后缀匹配
					if (suffix == key) {
						return true;
					}
					if (suffix.length() <= key.length()) {
						final int start = key.length() - suffix.length();
						for (int index = 0; index < suffix.length(); index++) {
							if (suffix.charAt(index) != key.charAt(start + index)) {
								return false;
							}
						}
						return true;
					} else {
						return false;
					}
				} else {
					// 前后缀匹配
					if (prefix.length() + suffix.length() <= key.length()) {
						for (int index = 0; index < prefix.length(); index++) {
							if (prefix.charAt(index) != key.charAt(index)) {
								return false;
							}
						}
						final int start = key.length() - suffix.length();
						for (int index = 0; index < suffix.length(); index++) {
							if (suffix.charAt(index) != key.charAt(start + index)) {
								return false;
							}
						}
						return true;
					} else {
						return false;
					}
				}
			} else {
				// 完全匹配
				if (prefix == key) {
					return true;
				}
				if (prefix.length() == key.length()) {
					for (int index = 0; index < prefix.length(); index++) {
						if (prefix.charAt(index) != key.charAt(index)) {
							return false;
						}
					}
					return true;
				} else {
					return false;
				}
			}
		}

		final static Comparator<Item<?>> COMPARATOR = new Comparator<>() {
			// 排序应为字符较多的优先于字符较少的
			// 否则"/*"和"/actions/*"后者永远无法匹配
			@Override
			public int compare(Item<?> a, Item<?> b) {
				if (a.wildcard) {
					if (b.wildcard) {
						final int length1 = (a.prefix == null ? 0 : a.prefix.length()) + (a.suffix == null ? 0 : a.suffix.length());
						final int length2 = (b.prefix == null ? 0 : b.prefix.length()) + (b.suffix == null ? 0 : b.suffix.length());
						if (length1 > length2) {
							return -1;
						} else if (length1 < length2) {
							return 1;
						}
						final String texta = (a.prefix == null ? "" : a.prefix) + (a.suffix == null ? "" : a.suffix);
						final String textb = (b.prefix == null ? "" : b.prefix) + (b.suffix == null ? "" : b.suffix);
						return texta.compareTo(textb);
					} else {
						return 1;
					}
				} else {
					if (b.wildcard) {
						return -1;
					} else {
						return a.prefix.compareTo(b.prefix);
					}
				}
			}
		};
	}
}
