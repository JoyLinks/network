/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import com.joyzl.network.Utility;

/**
 * URL中的查询参数编码
 * 
 * @author ZhangXi 2024年11月20日
 */
public class QueryCoder extends HTTP1Coder {

	// 此类无法与 FormDataCoder 共用逻辑
	// 虽然编码格式相同，但 FormDataCoder 侧重于字节
	// QueryCoder 仅处理字符串

	// TODO URLDecode和URLEncode创建了中间字符串，应优化

	/**
	 * 构造查询字符串参数
	 * 
	 * @param uri "/test.html"
	 * @param parameters
	 * @return "/test.html?query=alibaba&name=value"
	 */
	@Deprecated
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
	@Deprecated
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

	/**
	 * 解析URL编码的字符串
	 * <p>
	 * 此方法使用了公用字符缓存不能与其它使用公共字符缓存的方法嵌套
	 * </p>
	 * 
	 * @param offset 开始解析的字符位置，应小于等于'?'位置
	 * @param length 应解析的字符长度，应小于等于字符结束长度
	 * @param request 解析后的参数载体
	 */
	public static void parse(String url, int offset, int length, Request request) {
		length += offset;
		// 确认问号位置
		while (offset < length) {
			if (url.charAt(offset++) == QUEST) {
				break;
			}
		}
		if (offset < length) {
			// '+'还原为空格
			// "%FF"还原为字符
			// 可能的情形 name1&name2=&name3=value
			// 解析的结果 name1="" name2="" name3=value name4=null
			char c;
			String name = null;
			final StringBuilder buffer = getStringBuilder();
			for (; offset < length; offset++) {
				c = url.charAt(offset);
				if (c == '%') {
					// %XX%XX
					// 如果%后两位不是有效16进制字符则视为非百分号编码
					// 百分号编码是个很糟糕的设计
					if (offset < length - 2) {
						c = url.charAt(++offset);
						if (Utility.isHEXChar(c)) {
							char d = url.charAt(++offset);
							if (Utility.isHEXChar(d)) {
								c = (char) Utility.hex(c, d);
								if (c > 0x7F) {
									if (offset < length - 3) {
										// 按字节序列继续解析
										ByteBuffer bytes = ByteBuffer.allocate(64);
										bytes.put((byte) c);
										do {
											c = url.charAt(++offset);
											if (c == '%') {
												c = url.charAt(++offset);
												if (Utility.isHEXChar(c)) {
													d = url.charAt(++offset);
													if (Utility.isHEXChar(d)) {
														if (!bytes.hasRemaining()) {
															// 扩充序列空间
															final ByteBuffer temp = ByteBuffer.allocate(bytes.capacity() + bytes.capacity() / 4);
															temp.put(bytes);
															bytes = temp;
														}
														bytes.put((byte) Utility.hex(c, d));
													} else {
														// 不是十六进制字符
														buffer.append(StandardCharsets.UTF_8.decode(bytes.flip()));
														buffer.append('%');
														buffer.append(c);
														bytes.clear();
														c = d;
														break;
													}
												} else {
													// 不是十六进制字符
													buffer.append(StandardCharsets.UTF_8.decode(bytes.flip()));
													buffer.append('%');
													bytes.clear();
													break;
												}
											} else {
												break;
											}
										} while (offset < length - 3);
										// DONE
										if (bytes.position() > 0) {
											buffer.append(StandardCharsets.UTF_8.decode(bytes.flip()));
											if (offset < length - 3) {
												// NEXT c
											} else {
												break;
											}
										}
									} else {
										// 只能丢弃c了哟
										continue;
									}
								} else {
									// 单个ASCII字符
									buffer.append(c);
									continue;
								}
							} else {
								// 不是十六进制字符
								buffer.append('%');
								buffer.append(c);
								c = d;
							}
						} else {
							// 不是十六进制字符
							buffer.append('%');
						}
					} else {
						// 不够长度
						buffer.append(c);
						continue;
					}
				}

				if (c == EQUAL) {
					name = buffer.toString();
					buffer.setLength(0);
				} else if (c == AND) {
					if (name == null) {
						request.addParameter(buffer.toString(), "");
						buffer.setLength(0);
					} else {
						request.addParameter(name, buffer.toString());
						buffer.setLength(0);
						name = null;
					}
				} else if (c == '+') {
					buffer.append(SPACE);
				} else {
					buffer.append(c);
				}
			}

			// 收尾
			if (name == null) {
				request.addParameter(buffer.toString(), "");
			} else {
				request.addParameter(name, buffer.toString());
			}
		}
	}
}