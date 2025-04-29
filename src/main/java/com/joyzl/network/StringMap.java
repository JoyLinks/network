package com.joyzl.network;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 基于字符串键的值集合，键名不区分大小写
 * 
 * @author ZhangXi 2025年4月23日
 */
public class StringMap<V> implements Map<CharSequence, V> {

	// HTTP参数特性：键值对，值可多项，参数名不区分大小写
	// HTTP头特性：键值对，键值可多项，键名不区分大小写，代理转发保持值顺序

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V put(CharSequence key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends CharSequence, ? extends V> m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<CharSequence> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Map.Entry<CharSequence, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	static class Entry<V> implements Map.Entry<CharSequence, V> {
		private CharSequence key;
		private V value;

		@Override
		public CharSequence getKey() {
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