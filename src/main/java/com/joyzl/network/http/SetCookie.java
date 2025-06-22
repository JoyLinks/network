/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.joyzl.network.Utility;

/**
 * HTTP Cookie
 * 
 * <p>
 * Set-Cookie 响应首部被用来由服务器端向客户端发送 cookie。
 * 
 * <pre>
 * Set-Cookie: <cookie-name>=<cookie-value>
 * Set-Cookie: <cookie-name>=<cookie-value>; Expires=<date>
 * Set-Cookie: <cookie-name>=<cookie-value>; Max-Age=<non-zero-digit>
 * Set-Cookie: <cookie-name>=<cookie-value>; Domain=<domain-value>
 * Set-Cookie: <cookie-name>=<cookie-value>; Path=<path-value>
 * Set-Cookie: <cookie-name>=<cookie-value>; Secure
 * Set-Cookie: <cookie-name>=<cookie-value>; HttpOnly
 * Set-Cookie: <cookie-name>=<cookie-value>; SameSite=Strict
 * Set-Cookie: <cookie-name>=<cookie-value>; SameSite=Lax
 * Set-Cookie: <cookie-name>=<cookie-value>; Domain=<domain-value>; Secure; HttpOnly
 * Set-Cookie: qwerty=219ffwef9w0f; Domain=somecompany.com; Path=/; Expires=Wed, 30 Aug 2019 00:00:00 GMT
 * </pre>
 * 
 * @author ZhangXi
 * @date 2020年9月11日
 */
public final class SetCookie extends Header {

	public final static String NAME = HTTP1.Set_Cookie;

	final static String EXPIRES = "Expires";
	final static String MAX_AGE = "Max-Age";
	final static String DOMAIN = "Domain";
	final static String PATH = "Path";
	final static String SECURE = "Secure";
	final static String HTTP_ONLY = "HttpOnly";
	final static String SAME_SITE = "SameSite";

	private String name;
	private String value;
	/** 最长有效时间 */
	private ZonedDateTime expires;
	/** 失效之前需要经过的秒数 */
	private Integer max_age;
	/** 送达的主机名 */
	private String domain;
	/** 指定一个 URL 路径，这个路径必须出现在要请求的资源的路径中才可以发送 Cookie首部。 */
	private String path;
	/** 跨站请求发送规则 */
	private String same_site;
	/** 请求使用SSL和HTTPS协议的时候才会被发送到服务器 */
	private boolean secure;
	/** 设置了HttpOnly属性的 cookie不能使用JavaScript访问 */
	private boolean http_only;

	@Override
	public String getHeaderName() {
		return HTTP1.Set_Cookie;
	}

	@Override
	public String getHeaderValue() {
		if (value == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(HTTP1Coder.EQUAL);
		sb.append(getValue());
		if (getExpires() != null) {
			sb.append(HTTP1Coder.SEMI);
			sb.append(HTTP1Coder.SPACE);
			sb.append(EXPIRES);
			sb.append(HTTP1Coder.EQUAL);
			sb.append(DateTimeFormatter.RFC_1123_DATE_TIME.format(getExpires()));
		}
		if (getMaxAge() != null) {
			sb.append(HTTP1Coder.SEMI);
			sb.append(HTTP1Coder.SPACE);
			sb.append(MAX_AGE);
			sb.append(HTTP1Coder.EQUAL);
			sb.append(getMaxAge());
		}
		if (Utility.noEmpty(getDomain())) {
			sb.append(HTTP1Coder.SEMI);
			sb.append(HTTP1Coder.SPACE);
			sb.append(DOMAIN);
			sb.append(HTTP1Coder.EQUAL);
			sb.append(getDomain());
		}
		if (Utility.noEmpty(getPath())) {
			sb.append(HTTP1Coder.SEMI);
			sb.append(HTTP1Coder.SPACE);
			sb.append(PATH);
			sb.append(HTTP1Coder.EQUAL);
			sb.append(getPath());
		}
		if (Utility.noEmpty(getSameSite())) {
			sb.append(HTTP1Coder.SEMI);
			sb.append(HTTP1Coder.SPACE);
			sb.append(SAME_SITE);
			sb.append(HTTP1Coder.EQUAL);
			sb.append(getSameSite());
		}
		if (isSecure()) {
			sb.append(HTTP1Coder.SEMI);
			sb.append(HTTP1Coder.SPACE);
			sb.append(SECURE);
		}
		if (isHttpOnly()) {
			sb.append(HTTP1Coder.SEMI);
			sb.append(HTTP1Coder.SPACE);
			sb.append(HTTP_ONLY);
		}
		return sb.toString();
	}

	@Override
	public void setHeaderValue(String value) {
		String name = null;
		for (int start = 0, end = 0, index = 0; index <= value.length(); index++) {
			if (index >= value.length() || value.charAt(index) == HTTP1Coder.SEMI) {
				if (name == null) {
					if (start < end) {
						name = value.substring(start, end);
						if (SECURE.equalsIgnoreCase(name)) {
							secure = true;
						} else if (HTTP_ONLY.equalsIgnoreCase(name)) {
							http_only = true;
						} else {
							// 忽略无效属性
						}
					}
				} else if (getName() == null) {
					// 一个 cookie 开始于一个名称/值对
					setName(name);
					setValue(value.substring(start, end));
				} else if (EXPIRES.equalsIgnoreCase(name)) {
					expires = ZonedDateTime.parse(value.substring(start, end), DateTimeFormatter.RFC_1123_DATE_TIME);
				} else if (MAX_AGE.equalsIgnoreCase(name)) {
					max_age = Integer.parseUnsignedInt(value, start, end, 10);
				} else if (DOMAIN.equalsIgnoreCase(name)) {
					domain = value.substring(start, end);
				} else if (PATH.equalsIgnoreCase(name)) {
					path = value.substring(start, end);
				} else if (SAME_SITE.equalsIgnoreCase(name)) {
					same_site = value.substring(start, end);
				} else {
					// 忽略无效属性
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

	public final static SetCookie parse(String value) {
		if (Utility.noEmpty(value)) {
			SetCookie header = new SetCookie();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public void setValue(String v) {
		value = v;
	}

	public String getValue() {
		return value;
	}

	public ZonedDateTime getExpires() {
		return expires;
	}

	public void setExpires(ZonedDateTime value) {
		expires = value;
	}

	public Integer getMaxAge() {
		return max_age;
	}

	public void setMaxAge(Integer value) {
		max_age = value;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String value) {
		domain = value;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String value) {
		path = value;
	}

	/**
	 * None浏览器会在同站请求、跨站请求下继续发送cookies，不区分大小写。 Strict浏览器将只在访问相同站点时发送cookie。
	 * Lax与Strict类似，但用户从外部站点导航至URL时（例如通过链接）除外。
	 * 
	 * @return None/Strict/Lax
	 */
	public String getSameSite() {
		return same_site;
	}

	/**
	 * None浏览器会在同站请求、跨站请求下继续发送cookies，不区分大小写。 Strict浏览器将只在访问相同站点时发送cookie。
	 * Lax与Strict类似，但用户从外部站点导航至URL时（例如通过链接）除外。
	 * 
	 * @param None/Strict/Lax
	 */
	public void setSameSite(String value) {
		same_site = value;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean value) {
		secure = value;
	}

	public boolean isHttpOnly() {
		return http_only;
	}

	public void setHttpOnly(boolean value) {
		http_only = value;
	}
}
