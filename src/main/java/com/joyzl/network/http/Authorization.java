/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.util.HashMap;
import java.util.Map;

/**
 * Authorization
 * <p>
 * HTTP协议中的 Authorization 请求消息头含有服务器用于验证用户代理身份的凭证，通常会在服务器返回401 Unauthorized 状态码以及WWW-Authenticate消息头之后在后续请求中发送此消息头。
 * 
 * <pre>
 * Authorization: <type> <credentials>
 * Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l
 * Authorization: Digest username="admin", realm="DS-2CD2310FD-I", qop="auth", algorithm="MD5", uri="/onvif/device_service", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d", nc=00000001, cnonce="0EE3ED23BFD9A00B2AB542E3BAB85BDB", response="518fd6d1666f9f00a5c5097359188c4e"
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
public final class Authorization extends Header {

	public final static String NAME = "Authorization";

	private String type;
	private String credentials;
	private final Map<String, Object> arguments = new HashMap<>();

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
			if (credentials == null || credentials.isEmpty()) {
				return type;
			} else {
				return type + HTTPCoder.SPACE + credentials;
			}
		} else {
			if (credentials == null || credentials.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append(type);
				sb.append(HTTPCoder.SPACE);
				for (Map.Entry<String, Object> item : arguments.entrySet()) {
					if (isEmpty(item.getKey()) || item.getValue() == null) {
						continue;
					}
					if (sb.length() > type.length() + 1) {
						sb.append(HTTPCoder.COMMA);
						sb.append(HTTPCoder.SPACE);
					}
					sb.append(item.getKey());
					sb.append(HTTPCoder.EQUAL);
					if (item.getValue() instanceof Number) {
						sb.append(item.getValue());
					} else {
						sb.append(HTTPCoder.QUOTE);
						sb.append(item.getValue());
						sb.append(HTTPCoder.QUOTE);
					}
				}
				return sb.toString();
			} else {
				return type + HTTPCoder.SPACE + credentials;
			}
		}
	}

	@Override
	public void setHeaderValue(String text) {
		String name = null;
		int start = 0, end = 0, index = 0;
		for (; index <= text.length(); index++) {
			if (index >= text.length()) {
				type = text.substring(start, end);
				credentials = text.substring(end + 1);
				return;
			} else if (text.charAt(index) == HTTPCoder.SPACE) {
				if (start < end) {
					type = text.substring(start, end);
					credentials = text.substring(end + 1);
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

	public final static Authorization parse(String value) {
		if (noEmpty(value)) {
			Authorization header = new Authorization();
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

	public String getCredentials() {
		return credentials;
	}

	public void setCredentials(String value) {
		credentials = value;
	}

	public void setValue(String name, Object value) {
		arguments.put(name, value);
	}

	public Object getValue(String name) {
		return arguments.get(name);
	}

	public Map<String, Object> getArguments() {
		return arguments;
	}
}
