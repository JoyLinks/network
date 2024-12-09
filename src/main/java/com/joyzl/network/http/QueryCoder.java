package com.joyzl.network.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 查询参数
 * 
 * @author ZhangXi 2024年11月20日
 */
public class QueryCoder extends HTTPCoder {

	// TODO URLDecode和URLEncode创建了中间字符串，应优化

	/**
	 * 构造查询字符串参数
	 * 
	 * @param uri "/test.html"
	 * @param parameters
	 * @return "/test.html?query=alibaba&name=value"
	 */
	public static String makeQuery(String uri, Map<String, String[]> parameters) {
		if (parameters.isEmpty()) {
			return uri;
		}
		StringBuilder builder = new StringBuilder(uri);
		builder.append(QUEST);
		boolean more = false;
		for (Entry<String, String[]> item : parameters.entrySet()) {
			for (int index = 0; index < item.getValue().length; index++) {
				if (more) {
					builder.append(AND);
				} else {
					more = true;
				}
				builder.append(item.getKey());
				builder.append(EQUAL);
				if (item.getValue()[index] != null) {
					builder.append(URLEncoder.encode(item.getValue()[index], StandardCharsets.UTF_8));
				}
			}
		}
		return builder.toString();
	}

	/**
	 * 解析查询字符串参数
	 * 
	 * @param uri "/test.html?query=alibaba&name=value"
	 * @param parameters
	 * @return "/test.html
	 */
	public static String parseQuery(String uri, Map<String, String[]> parameters) {
		int quest = uri.indexOf(QUEST);
		if (quest > 0) {
			String name = null, value = null;
			for (int index = quest + 1, start = index, end = index; index <= uri.length(); index++) {
				if (index >= uri.length() || uri.charAt(index) == AND) {
					if (name == null) {
						if (start < end) {
							name = uri.substring(start, end);
							parameters.put(name, Parameters.add(null, parameters.get(name)));
						}
					} else {
						value = uri.substring(start, end);
						parameters.put(name, Parameters.add(value, parameters.get(name)));
					}
					name = null;
					end = start = index + 1;
				} else if (uri.charAt(index) == EQUAL) {
					name = uri.substring(start, end);
					end = start = index + 1;
				} else if (Character.isWhitespace(uri.charAt(index))) {
					if (end <= start) {
						start = index + 1;
					}
				} else {
					end = index + 1;
				}
			}
			return uri.substring(0, quest);
		}
		return uri;
	}

	public static void parse(Request request) {
		if (request.getQueryIndex() > 0) {
			if (request.getAnchorIndex() > 0) {
				parse(request.getURL(), request.getQueryIndex(), request.getAnchorIndex() - request.getQueryIndex(), request);
			} else {
				parse(request.getURL(), request.getQueryIndex(), request.getURL().length() - request.getQueryIndex(), request);
			}
		}
	}

	public static void parse(String url, int offset, int length, Request request) {
		length += offset;
		while (offset < length) {
			if (url.charAt(offset++) == QUEST) {
				break;
			}
		}
		if (offset < length) {
			String name = null, value = null;
			for (int start = offset, end = offset; offset <= length; offset++) {
				if (offset >= url.length() || url.charAt(offset) == AND) {
					if (name == null) {
						if (start < end) {
							name = url.substring(start, end);
							request.addParameter(name, null);
						}
					} else {
						value = url.substring(start, end);
						request.addParameter(name, value);
					}
					name = null;
					end = start = offset + 1;
				} else if (url.charAt(offset) == EQUAL) {
					name = url.substring(start, end);
					end = start = offset + 1;
				} else if (Character.isWhitespace(url.charAt(offset))) {
					if (end <= start) {
						start = offset + 1;
					}
				} else {
					end = offset + 1;
				}
			}
		}
	}
}