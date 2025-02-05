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
public class SupportedGroups extends Extension {

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
}