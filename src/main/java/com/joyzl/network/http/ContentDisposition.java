/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.joyzl.network.Utility;

/**
 * Content-Disposition
 * 
 * 在常规的HTTP应答中，Content-Disposition响应头指示回复的内容该以何种形式展示，是以内联的形式（即网页或者页面的一部分），还是以附件的形式下载并保存到本地。
 * 在multipart/form-data类型的应答消息体中，Content-Disposition消息头可以被用在
 * multipart消息体的子部分中，用来给出其对应字段的相关信息。 各个子部分由在Content-Type中定义的分隔符分隔。用在消息体自身则无实际意义。
 * 
 * <pre>
 * RESPONSE:
 * 
 * Content-Disposition: inline
 * Content-Disposition: attachment
 * Content-Disposition: attachment; filename="filename.jpg"
 * Content-Disposition: attachment; filename="cool.html"
 * </pre>
 * 
 * <pre>
 * REQUEST:
 * 
 * Content-Disposition: form-data
 * Content-Disposition: form-data; name="fieldName"
 * Content-Disposition: form-data; name="fieldName"; filename="filename.jpg"
 * 
 * --boundary
 * Content-Disposition: form-data; name="field1"
 * --boundary
 * Content-Disposition: form-data; name="field2"; filename="example.txt"
 * </pre>
 * 
 * RFC6266 Use of the Content-Disposition Header Field in the Hypertext Transfer
 * Protocol (HTTP)<br>
 * RFC7578 Returning Values from Forms: multipart/form-data<br>
 * 
 * @author ZhangXi
 * @date 2021年1月10日
 */
public final class ContentDisposition extends Header {

	public final static String NAME = "Content-Disposition";

	/** 内容直接在页面显示 */
	public final static String INLINE = "inline";
	/** 内容已附件形式下载 */
	public final static String ATTACHMENT = "attachment";
	/** 内容为表单数据 */
	public final static String FORM_DATA = "form-data";

	// 常量
	final static String FIELD = "name";
	final static String FILENAME = "filename";
	/**
	 * RFC5987 Character Set and Language Encoding for Hypertext Transfer
	 * Protocol (HTTP) Header Field Parameters
	 */
	final static String FILENAME_ = "filename*";
	final static String SEPARATER = "''";

	/** inline attachment form-data */
	private String disposition;
	/** name= */
	private String field;
	private String filename;

	public ContentDisposition() {
	}

	public ContentDisposition(String type) {
		disposition = type;
	}

	public ContentDisposition(String type, String name) {
		disposition = type;
		field = name;
	}

	public ContentDisposition(String type, String name, String file) {
		disposition = type;
		filename = file;
		field = name;
	}

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		if (ATTACHMENT.equalsIgnoreCase(disposition)) {
			if (Utility.noEmpty(filename)) {
				StringBuilder builder = new StringBuilder();
				builder.append(disposition);
				builder.append(HTTP1Coder.SEMI);
				builder.append(HTTP1Coder.SPACE);
				if (hasChinese(filename)) {
					builder.append(FILENAME_);
					builder.append(HTTP1Coder.EQUAL);
					builder.append(HTTP1Coder.QUOTE);
					builder.append("UTF-8");
					builder.append(SEPARATER);
					builder.append(URLEncoder.encode(filename, StandardCharsets.UTF_8));
					builder.append(HTTP1Coder.QUOTE);
				} else {
					builder.append(FILENAME);
					builder.append(HTTP1Coder.EQUAL);
					builder.append(HTTP1Coder.QUOTE);
					builder.append(filename);
					builder.append(HTTP1Coder.QUOTE);
				}
				return builder.toString();
			} else {
				return disposition;
			}
		}
		if (FORM_DATA.equalsIgnoreCase(disposition)) {
			if (Utility.noEmpty(field)) {
				StringBuilder builder = new StringBuilder();
				builder.append(disposition);
				builder.append(HTTP1Coder.SEMI);
				builder.append(HTTP1Coder.SPACE);
				builder.append(FIELD);
				builder.append(HTTP1Coder.EQUAL);
				builder.append(HTTP1Coder.QUOTE);
				builder.append(field);
				builder.append(HTTP1Coder.QUOTE);
				if (Utility.noEmpty(filename)) {
					builder.append(HTTP1Coder.SEMI);
					builder.append(HTTP1Coder.SPACE);
					if (hasChinese(filename)) {
						builder.append(FILENAME_);
						builder.append(HTTP1Coder.EQUAL);
						builder.append(HTTP1Coder.QUOTE);
						builder.append("UTF-8");
						builder.append(SEPARATER);
						builder.append(URLEncoder.encode(filename, StandardCharsets.UTF_8));
						builder.append(HTTP1Coder.QUOTE);
					} else {
						builder.append(FILENAME);
						builder.append(HTTP1Coder.EQUAL);
						builder.append(HTTP1Coder.QUOTE);
						builder.append(filename);
						builder.append(HTTP1Coder.QUOTE);
					}
				}
				return builder.toString();
			}
		}
		return disposition;
	}

	@Override
	public void setHeaderValue(String value) {
		int start;
		if ((start = value.indexOf(HTTP1Coder.SEMI)) > 0) {
			disposition = value.substring(0, start);

			String name = null;
			for (int end = 0, index = ++start; index <= value.length(); index++) {
				if (index >= value.length() || value.charAt(index) == HTTP1Coder.SEMI) {
					if (name == null) {
						break;
					} else if (FIELD.equalsIgnoreCase(name)) {
						field = value.substring(start, end);
					} else if (FILENAME.equalsIgnoreCase(name)) {
						filename = value.substring(start, end);
					} else if (FILENAME_.equalsIgnoreCase(name)) {
						// filename*="UTF-8''%E4%B8%AD%E5%90%8E%E7%AB%AF%E6%A1%86%E6%9E%B6.txt"
						filename = value.substring(start, end);
						end = filename.indexOf(SEPARATER);
						try {
							filename = URLDecoder.decode(filename.substring(end + SEPARATER.length()), filename.substring(0, end));
						} catch (UnsupportedEncodingException e) {
							throw new UnsupportedOperationException(e);
						}
					}
					name = null;
					end = start = index + 1;
				} else if (value.charAt(index) == HTTP1Coder.EQUAL) {
					name = value.substring(start, end);
					end = start = index + 1;
				} else if (value.charAt(index) == HTTP1Coder.QUOTE || Character.isWhitespace(value.charAt(index))) {
					if (end <= start) {
						start = index + 1;
					}
				} else {
					end = index + 1;
				}
			}
		} else {
			disposition = value;
		}
	}

	public final static ContentDisposition parse(String value) {
		if (Utility.noEmpty(value)) {
			ContentDisposition header = new ContentDisposition();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public String getField() {
		return field;
	}

	public void setField(String value) {
		field = value;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String value) {
		filename = value;
	}

	public String getDisposition() {
		return disposition;
	}

	public void setDisposition(String value) {
		disposition = value;
	}

	/**
	 * 检查字符串中是否包含双字节字符(ASCII之外的字符)
	 *
	 * @param value
	 * @return true/false
	 */
	static boolean hasChinese(String value) {
		for (int index = 0; index < value.length(); index++) {
			if (value.charAt(index) < 0 || value.charAt(index) > 127) {
				return true;
			}
		}
		return false;
	}
}
