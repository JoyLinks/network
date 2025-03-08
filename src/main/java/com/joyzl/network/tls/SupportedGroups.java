package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：表明支持的密钥协商算法
 * 
 * <pre>
 * struct {
 *     NamedGroup named_group_list<2..2^16-1>;
 * } NamedGroupList;
 * </pre>
 * 
 * @see NamedGroup
 * @author ZhangXi 2024年12月19日
 */
class SupportedGroups extends Extension {

	private final static short[] EMPTY = new short[0];
	private short[] items = EMPTY;

	public SupportedGroups() {
	}

	public SupportedGroups(short... value) {
		set(value);
	}

	@Override
	public short type() {
		return SUPPORTED_GROUPS;
	}

	public short[] get() {
		return items;
	}

	public short get(int index) {
		return items[index];
	}

	public void set(short... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(short value) {
		if (items == EMPTY) {
			items = new short[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public int size() {
		return items.length;
	}

	@Override
	public String toString() {
		if (size() > 0) {
			final StringBuilder b = new StringBuilder();
			b.append(name());
			b.append(':');
			for (int i = 0; i < size(); i++) {
				if (i > 0) {
					b.append(',');
				}
				b.append(NamedGroup.named(get(i)));
			}
			return b.toString();
		} else {
			return name() + ":EMPTY";
		}
	}

	/**
	 * 检查
	 */
	public boolean check(short other) {
		return NamedGroup.check(other, items);
	}

	/**
	 * 匹配
	 */
	public short match(short[] others) {
		for (int i = 0; i < items.length; i++) {
			for (int s = 0; s < others.length; s++) {
				if (items[i] == others[s]) {
					return others[s];
				}
			}
		}
		return 0;
	}
}