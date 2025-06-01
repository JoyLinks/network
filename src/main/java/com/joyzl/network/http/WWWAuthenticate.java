/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * WWW-Authenticate
 * <p>
 * HTTP WWW-Authenticate 响应头定义了使用何种验证方式去获取对资源的连接。 WWW-Authenticate
 * header通常会和一个401 Unauthorized 的响应一同被发送。
 * 
 * <pre>
 * WWW-Authenticate: <type> realm=<realm>
 * WWW-Authenticate: Basic
 * WWW-Authenticate: Basic realm="Access to the staging site"
 * WWW-Authenticate: Digest qop="auth", realm="DS-2CD2310FD-I", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d"
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年12月7日
 */
public final class WWWAuthenticate extends Header {

	public final static String NAME = HTTP1.WWW_Authenticate;

	private String value;

	@Override
	public String getHeaderName() {
		return HTTP1.WWW_Authenticate;
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

	public final static WWWAuthenticate parse(String value) {
		if (Utility.noEmpty(value)) {
			WWWAuthenticate header = new WWWAuthenticate();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}