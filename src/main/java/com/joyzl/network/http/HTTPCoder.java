/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import com.joyzl.common.Assist;

/**
 * HTTP(Hyper Text Transfer Protocol)
 * 
 * @author ZhangXi
 * @date 2021年10月7日
 */
public class HTTPCoder {

	public final static char CR = '\r';
	public final static char LF = '\n';
	public final static char SPACE = ' ';
	public final static char COLON = ':';
	public final static char COMMA = ',';
	public final static char SEMI = ';';
	public final static char EQUAL = '=';
	public final static char QUOTE = '"';
	public final static char QUEST = '?';
	public final static char AND = '&';
	public final static char MINUS = '-';
	public final static char SLASH = '/';
	public final static String CRLF = "\r\n";

	/**
	 * 请求帧结构 REQUEST
	 * 
	 * <pre>
	 * +--------+-------+-----+-------+---------+-------+------+
	 * | METHOD | SPACE | URL | SPACE | VERSION | ENTER | LINE |
	 * +--------+-------+-----+-------+---------+-------+------+
	 * +------+---+-------+-------+-------+------+
	 * | NAME | : | SPACE | VALUE | ENTER | LINE |
	 * +------+---+-------+-------+-------+------+
	 * +------+
	 * | .... |
	 * +------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * +---------+
	 * | CONTENT |
	 * +---------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * </pre>
	 * 
	 */
	/**
	 * 响应帧结构 RESPONSE
	 * 
	 * <pre>
	 * +---------+--------+-------------+-------+------+
	 * | VERSION | STATUS | STATUS-TEXT | ENTER | LINE |
	 * +---------+--------+-------------+-------+------+
	 * +------+---+-------+-------+------+
	 * | NAME | : | VALUE | ENTER | LINE |
	 * +------+---+-------+-------+------+
	 * +------+
	 * | .... |
	 * +------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * +------+
	 * | BODY |
	 * +------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * </pre>
	 */

	/**
	 * 解码超文本请求命令(请求第一行)
	 * 
	 * @param r HTReader
	 * @param request Request
	 * @return true 成功完成 / false 流提前结束
	 * @throws IOException
	 */
	public final static boolean readCommand(HTTPReader reader, Request request) throws IOException {
		reader.mark();
		if (reader.readTo(SPACE)) {
			request.setMethod(reader.string());
			if (reader.readTo(SPACE)) {
				request.setURI(reader.string());
				if (reader.readTo(CRLF)) {
					request.setVersion(reader.string());
					return true;
				} else {
					reader.reset();
				}
			} else {
				reader.reset();
			}
		} else {
			reader.reset();
		}
		return false;
	}

	/**
	 * 解码超文本响应命令(请求第一行)
	 * 
	 * @param r HTReader
	 * @param response Request
	 * @return true 成功完成 / false 流提前结束
	 * @throws IOException
	 */
	public final static boolean readCommand(HTTPReader reader, Response response) throws IOException {
		reader.mark();
		if (reader.readTo(SPACE)) {
			response.setVersion(reader.string());
			if (reader.readTo(SPACE)) {
				response.setStatus(Integer.parseUnsignedInt(reader.sequence(), 0, reader.sequence().length(), 10));
				if (reader.readTo(CRLF)) {
					response.setText(reader.string());
					return true;
				} else {
					reader.reset();
				}
			} else {
				reader.reset();
			}
		} else {
			reader.reset();
		}
		return false;
	}

	/**
	 * 解码超文本请求消息头
	 * 
	 * @param r HTReader
	 * @param request Request
	 * @return true 成功完成 / false 流提前结束
	 * @throws IOException
	 */
	public final static boolean readHeaders(HTTPReader reader, Message request) throws IOException {
		// Connection: keep-alive
		// Content-Length: 2
		String name;
		reader.mark();
		while (reader.readTo(COLON, CRLF)) {
			if (reader.last() == COLON) {
				name = reader.string();
				reader.skipWhitespace();
				if (reader.readTo(CRLF)) {
					request.getHeaders().put(name, reader.string());
					reader.mark();
				} else {
					break;
				}
			} else if (reader.last() == LF) {
				return true;
			} else {
				throw new IOException("意外字符:" + reader.last());
			}
		}
		reader.reset();
		return false;
	}

	/**
	 * 编码超文本请求命令行(响应第一行)
	 * 
	 * @param writer
	 * @param request
	 * @throws IOException
	 */
	public final static void writeCommand(HTTPWriter writer, Request request) throws IOException {
		writer.write(request.getMethod());
		writer.write(SPACE);
		writer.write(request.getURI());
		writer.write(SPACE);
		writer.write(request.getVersion());
		writer.write(CRLF);
	}

	/**
	 * 编码超文本响应命令行(响应第一行)
	 * 
	 * @param writer
	 * @param response
	 * @throws IOException
	 */
	public final static void writeCommand(HTTPWriter writer, Response response) throws IOException {
		writer.write(response.getVersion());
		writer.write(SPACE);
		writer.write(Integer.toUnsignedString(response.getStatus()));
		writer.write(SPACE);
		writer.write(response.getText());
		writer.write(CRLF);
	}

	/**
	 * 编码超文本响应消息头
	 * 
	 * @param writer
	 * @param message
	 * @throws IOException
	 */
	public final static void writeHeaders(HTTPWriter writer, Message message) throws IOException {
		for (Entry<String, String> header : message.getHeaders().entrySet()) {
			writer.write(header.getKey());
			writer.write(COLON);
			writer.write(SPACE);
			writer.write(header.getValue());
			writer.write(CRLF);
		}
		writer.write(CRLF);
	}

	/**
	 * 构造查询字符串参数
	 * 
	 * @param uri "/test.html"
	 * @param parameters
	 * @return "/test.html?query=alibaba&name=value"
	 */
	public final static String makeQuery(String uri, Map<String, String[]> parameters) {
		// RFC 3986
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
				builder.append(URLEncoder.encode(item.getValue()[index], Assist.DEFAULT_CHARSET));
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
	public final static String parseQuery(String uri, Map<String, String[]> parameters) {
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
}