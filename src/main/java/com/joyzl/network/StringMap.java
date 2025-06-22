/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 基于字符串键的值集合，键名不区分大小写
 * 
 * @author ZhangXi 2025年4月23日
 */
public class StringMap<V> implements Map<String, V>, Iterable<Map.Entry<String, V>> {

	// HTTP参数特性：键值对，值可多项，参数名不区分大小写
	// HTTP头特性：键值对，键值可多项，键名不区分大小写，代理转发保持值顺序
	// 按字符串长度确定存储位置（如果大量字符串具有相同长度，将导致性能下降）

	private final Entry[] table;
	private int size = 0;

	public StringMap() {
		this(64);
	}

	@SuppressWarnings("unchecked")
	public StringMap(int capacity) {
		table = (StringMap<V>.Entry[]) new StringMap<?>.Entry[capacity];
	}

	@Override
	public int size() {
		return size;
	}

	public int capacity() {
		return table.length;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/** 键定位基础表索引 */
	private int index(String key) {
		return key.length() % table.length;
	}

	/** 字符串比较不区分大小写 */
	private boolean equal(String a, String b) {
		if (a == b) {
			return true;
		}
		if (a.length() != b.length()) {
			return false;
		}

		int c1 = a.charAt(0);
		int c2 = b.charAt(0);
		if (c1 != c2) {
			if ((c1 ^ c2) != 32) {
				return false;
			}
		}
		for (int i = 1; i < a.length(); i++) {
			c1 = a.charAt(i);
			c2 = b.charAt(i);
			if (c1 != c2) {
				if ((c1 ^ c2) != 32) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean containsKey(Object key) {
		if (key instanceof String k) {
			Entry entry = table[index(k)];
			if (entry != null) {
				do {
					if (equal(entry.key, k)) {
						return true;
					}
					entry = entry.next;
				} while (entry != null);
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		Entry entry;
		for (int index = 0; index < table.length; index++) {
			entry = table[index];
			if (entry != null) {
				do {
					if (entry.value.equals(value)) {
						return true;
					}
					entry = entry.next;
				} while (entry != null);
			}
		}
		return false;
	}

	@Override
	public V get(Object key) {
		if (key instanceof String k) {
			Entry entry = table[index(k)];
			if (entry != null) {
				do {
					if (equal(entry.key, k)) {
						return entry.value;
					}
					entry = entry.next;
				} while (entry != null);
			}
		}
		return null;
	}

	@Override
	public V put(String key, V value) {
		Entry entry = table[index(key)];
		if (entry == null) {
			table[index(key)] = entry = new Entry();
		} else {
			if (equal(entry.key, key)) {
				V v = entry.value;
				entry.value = value;
				return v;
			}
			while (entry.next != null) {
				entry = entry.next;
				if (equal(entry.key, key)) {
					V v = entry.value;
					entry.value = value;
					return v;
				}
			}
			entry = entry.next = new Entry();
		}
		entry.value = value;
		entry.key = key;
		size++;
		return null;
	}

	@Override
	public V remove(Object key) {
		if (key instanceof String k) {
			Entry entry = table[index(k)];
			if (entry != null) {
				if (equal(entry.key, k)) {
					table[index(k)] = entry.next;
					size--;
					return entry.value;
				}
				while (entry.next != null) {
					if (equal(entry.next.key, k)) {
						Entry e = entry.next;
						entry.next = e.next;
						size--;
						return e.value;
					}
					entry = entry.next;
				}
			}
		}
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		Entry entry;
		loop: //
		for (Map.Entry<? extends String, ? extends V> e : m.entrySet()) {
			entry = table[index(e.getKey())];
			if (entry == null) {
				table[index(e.getKey())] = entry = new Entry();
			} else {
				if (equal(entry.key, e.getKey())) {
					entry.value = e.getValue();
					continue;
				}
				while (entry.next != null) {
					entry = entry.next;
					if (equal(entry.key, e.getKey())) {
						entry.value = e.getValue();
						continue loop;
					}
				}
				entry = entry.next = new Entry();
			}
			entry.value = e.getValue();
			entry.key = e.getKey();
			size++;
		}
	}

	@Override
	public void clear() {
		for (int index = 0; index < table.length; index++) {
			table[index] = null;
		}
		size = 0;
	}

	@Override
	public Set<String> keySet() {
		return new AbstractSet<String>() {
			@Override
			public Iterator<String> iterator() {
				return new KeyIterator();
			}

			@Override
			public int size() {
				return StringMap.this.size();
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {
			@Override
			public Iterator<V> iterator() {
				return new ValueIterator();
			}

			@Override
			public int size() {
				return StringMap.this.size();
			}
		};
	}

	@Override
	public Set<Map.Entry<String, V>> entrySet() {
		return new AbstractSet<Map.Entry<String, V>>() {
			@Override
			public Iterator<java.util.Map.Entry<String, V>> iterator() {
				return new EntryIterator();
			}

			@Override
			public int size() {
				return StringMap.this.size();
			}
		};
	}

	@Override
	public Iterator<Map.Entry<String, V>> iterator() {
		return new EntryIterator();
	}

	class BaseIterator {
		private int index = 0;
		private Entry prev, next;

		BaseIterator() {
			while (index < table.length) {
				next = table[index++];
				if (next != null) {
					break;
				}
			}
		}

		public boolean hasNext() {
			return next != null;
		}

		public Map.Entry<String, V> nextEntry() {
			prev = next;

			next = next.next;
			if (next == null) {
				while (index < table.length) {
					next = table[index++];
					if (next != null) {
						break;
					}
				}
			}

			return prev;
		}

		public void remove() {
			if (prev == null) {
				throw new IllegalStateException("NEXT");
			}

			Entry e = table[index(prev.getKey())];
			if (prev == e) {
				table[index(prev.getKey())] = e.next;
			} else {
				while (e.next != prev) {
					e = e.next;
				}
				e.next = e.next.next;
			}

			prev = null;
			size--;
		}
	}

	class KeyIterator extends BaseIterator implements Iterator<String> {
		@Override
		public String next() {
			return nextEntry().getKey();
		}
	}

	class ValueIterator extends BaseIterator implements Iterator<V> {
		@Override
		public V next() {
			return nextEntry().getValue();
		}
	}

	class EntryIterator extends BaseIterator implements Iterator<Map.Entry<String, V>> {
		@Override
		public Map.Entry<String, V> next() {
			return nextEntry();
		}
	}

	class Entry implements Map.Entry<String, V> {
		private Entry next;
		private String key;
		private V value;

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V v = value;
			this.value = value;
			return v;
		}
	}
}