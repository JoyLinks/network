package com.joyzl.network;

import java.util.Iterator;

import com.joyzl.network.IndexMap.Entry;

/**
 * 基于整数键的键值集合
 * <p>
 * 此集合与标准的Map行为类似，但功能更加简化，专用于消息对象按整数标识存取且避免整数键装箱；<br>
 * 由于消息对象的标识特性，此集合具有固定大小的基础表空间，并且不会扩展基础表空间；<br>
 * 不能使用0作为键，0值用于判定节点是否为空。
 * </p>
 * <p>
 * 集合不是多线程安全的，多线程情况下需要额外的锁操作。
 * </p>
 * 
 * @author ZhangXi 2025年4月22日
 * @param <V> 值类型
 */
public class IndexMap<V> implements Iterator<Entry<V>>, Iterable<Entry<V>> {

	// 基础表空间存储根节点
	// 根节点始终保留，衍生节点在移除键值时被移除
	// 根节点为空值时，不产生衍生节点

	private final Entry<V>[] table;
	private int size = 0;

	public IndexMap() {
		this(64);
	}

	@SuppressWarnings("unchecked")
	public IndexMap(int capacity) {
		table = (Entry<V>[]) new Entry<?>[capacity];
		for (int index = 0; index < table.length; index++) {
			table[index] = new Entry<>();
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int capacity() {
		return table.length;
	}

	public int size() {
		return size;
	}

	private int index(int key) {
		if (key == 0) {
			throw new IllegalArgumentException("KEY:0");
		}
		if (key < 0) {
			return (key % table.length) * -1;
		}
		return key % table.length;
	}

	public boolean contains(int key) {
		Entry<V> entry = table[index(key)];
		do {
			if (entry.key == key) {
				return true;
			}
			entry = entry.next;
		} while (entry != null);
		return false;
	}

	public void put(int key, V value) {
		Entry<V> entry = table[index(key)];
		while (entry.key != 0) {
			if (entry.key == key) {
				entry.value = value;
				return;
			}
			if (entry.next == null) {
				entry.next = new Entry<>();
				entry = entry.next;
				break;
			}
			entry = entry.next;
		}
		entry.value = value;
		entry.key = key;
		size++;
	}

	public V get(int key) {
		Entry<V> entry = table[index(key)];
		do {
			if (entry.key == key) {
				return entry.value;
			}
			entry = entry.next;
		} while (entry != null);
		return null;
	}

	public V remove(int key) {
		Entry<V> entry = table[index(key)];
		if (entry.key == key) {
			size--;
			if (entry.next != null) {
				// 移除并替换根节点
				table[index(key)] = entry.next;
				entry.next = null;
				return entry.value;
			} else {
				// 仅有根节点保留
				entry.key = 0;
				return entry.value;
			}
		} else {
			while (entry.next != null) {
				if (entry.next.key == key) {
					size--;
					// 移除匹配节点
					Entry<V> e = entry.next;
					entry.next = e.next;
					return e.value;
				}
				entry = entry.next;
			}
			return null;
		}
	}

	public void clear() {
		Entry<V> entry;
		for (int index = 0; index < table.length; index++) {
			entry = table[index];
			entry.value = null;
			entry.key = 0;
			while (entry.next != null) {
				entry = entry.next;
				entry.value = null;
				entry.key = 0;
			}
			// 丢弃其它节点
			entry = table[index];
			entry.next = null;
		}
		size = 0;
	}

	public static class Entry<V> {
		private Entry<V> next;
		private int key = 0;
		private V value;

		private Entry() {
		}

		public int key() {
			return key;
		}

		public V value() {
			return value;
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	Entry<V> parent, entry;
	int index;

	private void nextEntry() {
		if (entry != null) {
			while (entry.next != null) {
				parent = entry;
				entry = entry.next;
				return;
			}
		}
		while (index < table.length) {
			entry = table[index++];
			if (entry.key != 0) {
				parent = null;
				return;
			}
		}
		entry = null;
	}

	@Override
	public Iterator<Entry<V>> iterator() {
		index = 0;
		parent = entry = null;
		return this;
	}

	@Override
	public boolean hasNext() {
		nextEntry();
		return entry != null;
	}

	@Override
	public Entry<V> next() {
		return entry;
	}

	@Override
	public void remove() {
		entry.value = null;
		entry.key = 0;
		size--;
		if (parent == null) {
			if (entry.next != null) {
				table[index -= 1] = entry.next;
			}
			entry = null;
		} else {
			parent.next = entry.next;
			entry = entry.next;
		}
	}
}