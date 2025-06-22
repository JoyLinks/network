/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.joyzl.network.Utility;

/**
 * HTTP Cookie
 * 
 * <p>
 * Cookie是一个请求首部，其中含有先前由服务器通过 Set-Cookie 首部投放并存储到客户端的 HTTP cookies。
 * 
 * <pre>
 * Cookie: <cookie-list>
 * Cookie: name=value
 * Cookie: name=value; name2=value2; name3=value3
 * Cookie: PHPSESSID=298zf09hf012fh2; csrftoken=u32t4o3tb3gg43; _gat=1;
 * </pre>
 * 
 * <name>可以是除了控制字符CTLs、空格spaces或制表符tab之外的任何ASCII字符。同时不能包含以下分隔字符：()<>@,;:\"/[]?={}.
 * <value>支持除了控制字符CTLs、空格whitespace、双引号double-quotes、逗号comma、分号semicolon以及反斜线backslash之外的任意ASCII字符。
 * 名称/值对之间用分号和空格 ('; ')隔开。
 * 
 * @author ZhangXi
 * @date 2020年9月11日
 */
public class Cookie extends Header {

	public final static String NAME = HTTP1.Cookie;

	private final Map<String, String> arguments = new HashMap<>();

	@Override
	public final String getHeaderName() {
		return HTTP1.Cookie;
	}

	@Override
	public String getHeaderValue() {
		if (arguments == null || arguments.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> item : arguments.entrySet()) {
			if (Utility.isEmpty(item.getKey()) || Utility.isEmpty(item.getValue())) {
				// 忽略名称或值为空的Cookie
				continue;
			}
			if (sb.length() > 0) {
				sb.append(HTTP1Coder.SEMI);
				sb.append(HTTP1Coder.SPACE);
			}
			sb.append(item.getKey());
			sb.append(HTTP1Coder.EQUAL);
			sb.append(URLEncoder.encode(item.getValue(), StandardCharsets.UTF_8));
		}
		return sb.toString();
	}

	@Override
	public void setHeaderValue(String value) {
		String name = null;
		for (int start = 0, end = 0, index = 0; index <= value.length(); index++) {
			if (index >= value.length() || value.charAt(index) == HTTP1Coder.SEMI) {
				if (name == null) {
					break;
				} else {
					setValue(name, URLDecoder.decode(value.substring(start, end), StandardCharsets.UTF_8));
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

	public final static Cookie parse(String value) {
		if (Utility.noEmpty(value)) {
			Cookie header = new Cookie();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public void setValue(String name, String value) {
		arguments.put(name, value);
	}

	public String getValue(String name) {
		return arguments.get(name);
	}

	public Map<String, String> getArguments() {
		return arguments;
	}
}
