/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;
import com.joyzl.network.web.MIMEType;

/**
 * Content-Type实体头部用于指示资源的MIME类型 media type。 在请求中(如POST或 PUT)，客户端告诉服务器实际发送的数据类型。
 * RFC7231 缺省为"application/octet-stream"
 * 已注册的类型https://www.iana.org/assignments/media-types/media-types.xhtml
 * 
 * <pre>
 * Content-Type: text/html; charset=utf-8
 * Content-Type: multipart/form-data; boundary=something
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年1月8日
 */
public final class ContentType extends Header {

	public final static String NAME = HTTP.Content_Type;

	final static String CHARSET = "charset";
	final static String BOUNDARY = "boundary";

	private String type;
	private String charset;
	private String boundary;

	public ContentType() {
	}

	public ContentType(String type) {
		setType(type);
	}

	public ContentType(String type, String charset) {
		setType(type);
		setCharset(charset);
	}

	@Override
	public String getHeaderName() {
		return HTTP.Content_Type;
	}

	@Override
	public String getHeaderValue() {
		if (type.startsWith(MIMEType.MULTIPART)) {
			if (Utility.isEmpty(boundary)) {
				// "multipart"必须设置boundary
				boundary = boundary();
			}
			return type + HTTPCoder.SEMI + HTTPCoder.SPACE + BOUNDARY + HTTPCoder.EQUAL + boundary;
		}
		if (Utility.noEmpty(charset)) {
			return type + HTTPCoder.SEMI + HTTPCoder.SPACE + CHARSET + HTTPCoder.EQUAL + charset;
		}
		return type;
	}

	@Override
	public void setHeaderValue(String value) {
		int start = value.indexOf(HTTPCoder.SEMI);
		if (start > 0) {
			type = value.substring(0, start);
			start = start + 1;
			while (start < value.length()) {
				if (Character.isWhitespace(value.charAt(start))) {
					start++;
				} else {
					break;
				}
			}
			int equal = value.indexOf(HTTPCoder.EQUAL, start);
			if (equal > 0) {
				if (Utility.same(CHARSET, value, start, equal)) {
					charset = value.substring(equal + 1);
				} else if (Utility.same(BOUNDARY, value, start, equal)) {
					boundary = value.substring(equal + 1);
				} else {
					// 忽略无法识别的附加参数
				}
			}
		} else {
			type = value;
		}
	}

	public final static ContentType parse(String value) {
		if (Utility.noEmpty(value)) {
			ContentType header = new ContentType();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	/**
	 * 获取当前时间戳生成的boundary
	 */
	public static String boundary() {
		return "JOYZL-HTTP-" + Long.toString(System.currentTimeMillis(), Character.MAX_RADIX);
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		type = value;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String value) {
		charset = value;
	}

	public String getBoundary() {
		return boundary;
	}

	public void setBoundary(String value) {
		boundary = value;
	}
}