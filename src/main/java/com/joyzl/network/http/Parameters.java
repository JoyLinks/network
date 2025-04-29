/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 键值对集合，键不区分大小写
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

}