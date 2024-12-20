package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.Utility;

/**
 * <pre>
 * struct {
 *     NamedGroup named_group_list<2..2^16-1>;
 * } NamedGroupList;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class SupportedGroups extends Extension {

	private final static NamedGroup[] EMPTY = new NamedGroup[0];
	private NamedGroup[] items = EMPTY;

	public SupportedGroups() {
	}

	public SupportedGroups(NamedGroup... value) {
		set(value);
	}

	@Override
	public ExtensionType type() {
		return ExtensionType.SUPPORTED_GROUPS;
	}

	public NamedGroup[] get() {
		return items;
	}

	public NamedGroup get(int index) {
		return items[index];
	}

	public void set(NamedGroup... value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public void add(NamedGroup value) {
		if (items == EMPTY) {
			items = new NamedGroup[] { value };
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
		final StringBuilder builder = Utility.getStringBuilder();
		builder.append("supported_groups:");
		if (items != null && items.length > 0) {
			for (int index = 0; index < items.length; index++) {
				if (index > 0) {
					builder.append(',');
				}
				builder.append(items[index].toString());
			}
		}
		return builder.toString();
	}
}