/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.joyzl.network.http.Request;

/**
 * WEB HTTP Request
 * 
 * @author ZhangXi
 * @date 2021年10月12日
 */
public final class WEBRequest extends Request {

	private final Map<String, String[]> parameters = new HashMap<>();

	/**
	 * 添加参数
	 */
	public void addParameter(String name, String value) {
		String[] values = parameters.get(name);
		if (values == null) {
			parameters.put(name, new String[] { value });
		} else {
			values = Arrays.copyOf(values, values.length + 1);
			values[values.length - 1] = value;
			parameters.replace(name, values);
		}
	}

	public boolean hasParameter() {
		if (parameters == null || parameters.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean hasParameter(String name) {
		if (parameters == null || parameters.isEmpty()) {
			return false;
		}
		return parameters.containsKey(name);
	}

	/**
	 * 设置参数
	 */
	public void setParameter(String name, String value) {
		String[] values = parameters.get(name);
		if (values == null) {
			parameters.put(name, new String[] { value });
		} else if (values.length == 1) {
			values[0] = value;
			parameters.replace(name, values);
		} else {
			parameters.put(name, new String[] { value });
		}
	}

	/**
	 * 设置多个参数值
	 */
	public void setParameter(String name, String[] values) {
		parameters.put(name, values);
	}

	/**
	 * 获取参数值
	 */
	public String getParameter(String name) {
		String[] values = parameters.get(name);
		if (values == null || values.length == 0) {
			return null;
		} else {
			return values[0];
		}
	}

	/**
	 * 获取参数值
	 */
	public String[] getParameterValues(String name) {
		return parameters.get(name);
	}

	public Map<String, String[]> getParametersMap() {
		return parameters;
	}

}
