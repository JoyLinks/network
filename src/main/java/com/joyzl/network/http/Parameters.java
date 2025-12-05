/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 键值对集合，键区分大小写
 * 
 * @author ZhangXi
 * @date 2021年10月16日
 */
public final class Parameters extends HashMap<String, String[]> {

	private static final long serialVersionUID = 1L;

	public final static String[] add(String value, String[] values) {
		if (values == null) {
			return new String[] { value };
		} else {
			values = Arrays.copyOf(values, values.length + 1);
			values[values.length - 1] = value;
			return values;
		}
	}

	public void add(String name, String value) {
		String[] values = get(name);
		if (values == null) {
			put(name, new String[] { value });
		} else {
			values = Arrays.copyOf(values, values.length + 1);
			values[values.length - 1] = value;
			replace(name, values);
		}
	}

	public void set(String name, String value) {
		String[] values = get(name);
		if (values == null) {
			put(name, new String[] { value });
		} else if (values.length == 1) {
			values[0] = value;
			replace(name, values);
		} else {
			put(name, new String[] { value });
		}
	}

	public void set(String name, String[] values) {
		put(name, values);
	}
}