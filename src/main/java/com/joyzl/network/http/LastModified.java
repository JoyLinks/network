/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Last-Modified
 * 
 * @author ZhangXi 2023年9月15日
 */
public class LastModified extends Header {

	/*-
	 * Last-Modified: <day-name>, <day> <month> <year> <hour>:<minute>:<second> GMT
	 * Last-Modified是一个响应首部，其中包含源头服务器认定的资源做出修改的日期及时间。
	 * 它通常被用作一个验证器来判断接收到的或者存储的资源是否彼此一致。
	 * 由于精确度比 ETag 要低，所以这是一个备用机制。
	 * 包含有 If-Modified-Since 或 If-Unmodified-Since 首部的条件请求会使用这个字段。
	 */

	public final static String NAME = HTTP1.Last_Modified;

	private ZonedDateTime value;

	@Override
	public String getHeaderName() {
		return HTTP1.Last_Modified;
	}

	@Override
	public String getHeaderValue() {
		if (value == null) {
			return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(Date.GMT));
		} else {
			return DateTimeFormatter.RFC_1123_DATE_TIME.format(value);
		}
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
	}

	public ZonedDateTime getValue() {
		return value;
	}

	public void setValue(ZonedDateTime value) {
		this.value = value;
	}
}