package com.joyzl.network.http;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * HTTP Header Collection
 * 
 * @author ZhangXi 2024年11月22日
 */
public class Dictionary<V> implements Map<String, V> {

	// 支持CharSequence查找，避免创建过多String对象
	// 不依赖 HashCode，String生成HashCode需要遍历每个字符
	// 键名固定为String类型，不区分大小写
	// HEAD 允许重复，参数允许重复且保持顺序

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
	public V put(String key, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<V> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Entry<String, V>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	class NameValue<T> {
		private final String name;
		private T value;

		NameValue(String name) {
			this.name = name;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public String getName() {
			return name;
		}
	}
}