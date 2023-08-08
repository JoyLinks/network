/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.util.HashMap;
import java.util.Map;

/**
 * WWW-Authenticate
 * <p>
 * HTTP WWW-Authenticate 响应头定义了使用何种验证方式去获取对资源的连接。 WWW-Authenticate header通常会和一个401 Unauthorized 的响应一同被发送。
 * 
 * <pre>
 * WWW-Authenticate: <type> realm=<realm>
 * WWW-Authenticate: Basic
 * WWW-Authenticate: Basic realm="Access to the staging site"
 * WWW-Authenticate: Digest qop="auth", realm="DS-2CD2310FD-I", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d"
 * </pre>
 * <p>
 * 验证类型有如下方案：<br>
 * Basic (RFC 7617, base64编码凭证.),<br>
 * Bearer (RFC 6750, bearer 令牌通过OAuth 2.0保护资源),<br>
 * Digest (RFC 7616, MD5/SHA散列加密支持),<br>
 * HOBA (RFC 7486 (草案), HTTP Origin-Bound 认证, 基于数字签名),<br>
 * Mutual (draft-ietf-httpauth-mutual),<br>
 * AWS4-HMAC-SHA256 (AWS docs).<br>
 * 
 * @author ZhangXi
 * @date 2021年12月7日
 */
public final class WWWAuthenticate extends Header {

	public final static String NAME = "WWW-Authenticate";

	private String type;
	private final Map<String, String> arguments = new HashMap<>();

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		if (type == null || type.isEmpty()) {
			return null;
		}
		if (arguments.isEmpty()) {
			return type;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(type);
			sb.append(HTTPCoder.SPACE);
			for (Map.Entry<String, String> item : arguments.entrySet()) {
				if (isEmpty(item.getKey()) || isEmpty(item.getValue())) {
					continue;
				}
				if (sb.length() > type.length() + 1) {
					sb.append(HTTPCoder.COMMA);
					sb.append(HTTPCoder.SPACE);
				}
				sb.append(item.getKey());
				sb.append(HTTPCoder.EQUAL);
				sb.append(HTTPCoder.QUOTE);
				sb.append(item.getValue());
				sb.append(HTTPCoder.QUOTE);
			}
			return sb.toString();
		}
	}

	@Override
	public void setHeaderValue(String text) {
		String name = null;
		int start = 0, end = 0, index = 0;
		for (; index <= text.length(); index++) {
			if (index >= text.length()) {
				type = text.substring(start, end);
				return;
			} else if (text.charAt(index) == HTTPCoder.SPACE) {
				if (start < end) {
					type = text.substring(start, end);
					end = start = index + 1;
					break;
				} else {
					start = index + 1;
				}
			} else if (Character.isWhitespace(text.charAt(index))) {
				if (start >= end) {
					start = index + 1;
				}
			} else {
				end = index + 1;
			}
		}
		for (; index <= text.length(); index++) {
			if (index >= text.length() || text.charAt(index) == HTTPCoder.COMMA) {
				if (name == null) {
					break;
				} else {
					arguments.put(name, text.substring(start, end));
				}
				name = null;
				end = start = index + 1;
			} else if (text.charAt(index) == HTTPCoder.EQUAL) {
				name = text.substring(start, end);

				end = start = index + 1;
			} else if (text.charAt(index) == HTTPCoder.QUOTE || Character.isWhitespace(text.charAt(index))) {
				if (end <= start) {
					start = index + 1;
				}
			} else {
				end = index + 1;
			}
		}
	}

	public final static WWWAuthenticate parse(String value) {
		if (noEmpty(value)) {
			WWWAuthenticate header = new WWWAuthenticate();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		type = value;
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
