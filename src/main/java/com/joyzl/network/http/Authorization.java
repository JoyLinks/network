/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Authorization
 * <p>
 * HTTP协议中的 Authorization 请求消息头含有服务器用于验证用户代理身份的凭证，通常会在服务器返回401 Unauthorized
 * 状态码以及WWW-Authenticate消息头之后在后续请求中发送此消息头。
 * 
 * <pre>
 * Authorization: <type> <credentials>
 * Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l
 * Authorization: Digest username="admin", realm="DS-2CD2310FD-I", qop="auth", algorithm="MD5", uri="/onvif/device_service", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d", nc=00000001, cnonce="0EE3ED23BFD9A00B2AB542E3BAB85BDB", response="518fd6d1666f9f00a5c5097359188c4e"
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年12月7日
 */
public final class Authorization extends Header {

	public final static String NAME = HTTP1.Authorization;

	private String value;

	public Authorization() {
	}

	public Authorization(String value) {
		this.value = value;
	}

	@Override
	public String getHeaderName() {
		return HTTP1.Authorization;
	}

	@Override
	public String getHeaderValue() {
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}

	/** Basic Digest */
	public boolean isType(String type) {
		if (value == null || value.length() < type.length()) {
			return false;
		}
		if (value.length() == type.length()) {
			return value.equals(type);
		}
		if (value.length() > type.length()) {
			if (value.startsWith(type)) {
				return value.charAt(type.length()) == HTTP1Coder.SPACE;
			}
		}
		return false;
	}

	/** Type name=value,name="value" */
	public String getValue(String name) {
		if (value == null) {
			return null;
		}
		if (value.length() > name.length()) {
			int begin = value.indexOf(name);
			while (begin > 0) {
				begin += name.length();
				if (begin < value.length()) {
					if (value.charAt(begin) == HTTP1Coder.EQUAL) {
						// qop=auth
						// qop="auth, auth-int"
						if (value.charAt(begin += 1) == HTTP1Coder.QUOTE) {
							int end = value.indexOf(HTTP1Coder.QUOTE, begin + 1);
							if (end < 0) {
								// 双引号未成对
								return null;
							}
							return value.substring(begin + 1, end);
						} else {
							int end = value.indexOf(',', begin + 1);
							if (end < 0) {
								end = value.length();
								while (Character.isWhitespace(value.charAt(end - 1))) {
									// 忽略尾部空白
									end--;
								}
							}
							return value.substring(begin, end);
						}
					}
				}
				// 多次查找名称防止单词包含
				begin = value.indexOf(name, begin);
			}
		}
		return null;
	}

	/** Type value */
	public String getValue() {
		if (value == null) {
			return null;
		}
		int index = value.indexOf(HTTP1Coder.SPACE);
		if (index > 0 && index < value.length() - 1) {
			return value.substring(index + 1);
		}
		return null;
	}

	public final static Authorization parse(String value) {
		if (Utility.noEmpty(value)) {
			Authorization header = new Authorization();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}