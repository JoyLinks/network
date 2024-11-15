/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date<br>
 * 通用首部，其中包含了报文创建的日期和时间
 * 
 * @author ZhangXi
 * @date 2021年10月18日
 */
public final class Date extends Header {

	public final static ZoneId GMT = ZoneId.of("GMT");
	public final static String NAME = "Date";

	private ZonedDateTime value;

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		if (value == null) {
			return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(GMT));
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

	public void setValue(LocalDateTime value) {
		this.value = value.atZone(GMT);
	}

	public void setValue(long value) {
		this.value = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), Date.GMT);
	}

	public static String toText(ZonedDateTime value) {
		return DateTimeFormatter.RFC_1123_DATE_TIME.format(value);
	}

	public static String toText(LocalDateTime value) {
		return DateTimeFormatter.RFC_1123_DATE_TIME.format(value.atZone(GMT));
	}

	public static String toText(long value) {
		return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), GMT));
	}
}