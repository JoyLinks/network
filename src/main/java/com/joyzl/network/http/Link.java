/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.util.HashMap;
import java.util.Map;

import com.joyzl.network.Utility;

/**
 * 序列化头部链接
 * 
 * <pre>
 * Link: <uri-reference>; param1=value1; param2="value2"
 * Link: <https://example.com>; rel="preload"
 * </pre>
 * 
 * @author ZhangXi
 * @date 2024年11月13日
 */
public final class Link extends Header {

	public final static String NAME = HTTP1.Link;

	private final Map<String, String> arguments = new HashMap<>();
	private String reference;

	@Override
	public String getHeaderName() {
		return HTTP1.Link;
	}

	@Override
	public String getHeaderValue() {
		if (getArguments() == null || getArguments().isEmpty()) {
			return '<' + getReference() + '>';
		} else {
			final StringBuilder builder = new StringBuilder();
			builder.append('<');
			builder.append(getReference());
			builder.append('>');
			for (Map.Entry<String, String> item : getArguments().entrySet()) {
				if (Utility.isEmpty(item.getKey()) || Utility.isEmpty(item.getValue())) {
					// 忽略名称或值为空的参数
					continue;
				}
				if (builder.length() > 0) {
					builder.append(HTTP1Coder.SEMI);
					builder.append(HTTP1Coder.SPACE);
				}
				builder.append(item.getKey());
				builder.append(HTTP1Coder.EQUAL);
				builder.append(item.getValue());
			}
			return builder.toString();
		}
	}

	@Override
	public void setHeaderValue(String value) {
		String name = null;
		for (int start = 0, end = 0, index = 0; index <= value.length(); index++) {
			if (index >= value.length() || value.charAt(index) == HTTP1Coder.SEMI) {
				if (name == null) {
					break;
				} else {
					setArgument(name, value.substring(start, end));
				}
				name = null;
				end = start = index + 1;
			} else if (value.charAt(index) == HTTP1Coder.EQUAL) {
				name = value.substring(start, end);
				end = start = index + 1;
			} else if (Character.isWhitespace(value.charAt(index))) {
				if (end <= start) {
					start = index + 1;
				}
			} else {
				end = index + 1;
			}
		}
	}

	public final static Link parse(String value) {
		if (Utility.noEmpty(value)) {
			Link header = new Link();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String value) {
		reference = value;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}

	public void setArgument(String name, String value) {
		arguments.put(name, value);
	}

	public String getArgument(String name) {
		return arguments.get(name);
	}
}