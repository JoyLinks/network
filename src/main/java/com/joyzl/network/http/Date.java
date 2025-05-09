/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Date<br>
 * 通用首部，其中包含了报文创建的日期和时间
 * 
 * @author ZhangXi
 * @date 2021年10月18日
 */
public final class Date extends Header {

	public final static ZoneId GMT = ZoneId.of("GMT");
	public final static String NAME = HTTP1.Date;

	private ZonedDateTime value;

	@Override
	public String getHeaderName() {
		return HTTP1.Date;
	}

	@Override
	public String getHeaderValue() {
		if (value == null) {
			return FORMATTER.format(ZonedDateTime.now(GMT));
		} else {
			return FORMATTER.format(value);
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
		return FORMATTER.format(value);
	}

	public static String toText(LocalDateTime value) {
		return FORMATTER.format(value.atZone(GMT));
	}

	public static String toText(long value) {
		return FORMATTER.format(ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), GMT));
	}

	// RFC_1123_DATE_TIME
	// Date: Mon, 2 Dec 2024 00:05:33 GMT
	// RFC 53122 day 2位数值
	// Date: Sun, 06 Nov 1994 08:49:37 GMT
	public static final DateTimeFormatter FORMATTER;
	static {
		final Map<Long, String> dow = new HashMap<>();
		dow.put(1L, "Mon");
		dow.put(2L, "Tue");
		dow.put(3L, "Wed");
		dow.put(4L, "Thu");
		dow.put(5L, "Fri");
		dow.put(6L, "Sat");
		dow.put(7L, "Sun");
		final Map<Long, String> moy = new HashMap<>();
		moy.put(1L, "Jan");
		moy.put(2L, "Feb");
		moy.put(3L, "Mar");
		moy.put(4L, "Apr");
		moy.put(5L, "May");
		moy.put(6L, "Jun");
		moy.put(7L, "Jul");
		moy.put(8L, "Aug");
		moy.put(9L, "Sep");
		moy.put(10L, "Oct");
		moy.put(11L, "Nov");
		moy.put(12L, "Dec");

		FORMATTER = new DateTimeFormatterBuilder()//
			.parseCaseInsensitive()//
			.parseLenient()//
			.optionalStart()//
			.appendText(DAY_OF_WEEK, dow)//
			.appendLiteral(", ")//
			.optionalEnd()//
			.appendValue(DAY_OF_MONTH, 2, 2, SignStyle.NOT_NEGATIVE)//
			.appendLiteral(' ')//
			.appendText(MONTH_OF_YEAR, moy)//
			.appendLiteral(' ')//
			.appendValue(YEAR, 4)//
			.appendLiteral(' ')//
			.appendValue(HOUR_OF_DAY, 2)//
			.appendLiteral(':')//
			.appendValue(MINUTE_OF_HOUR, 2)//
			.optionalStart()//
			.appendLiteral(':')//
			.appendValue(SECOND_OF_MINUTE, 2)//
			.optionalEnd()//
			.appendLiteral(' ')//
			.appendOffset("+HHMM", "GMT") //
			// should handle UT/Z/EST/EDT/CST/CDT/MST/MDT/PST/MDT
			// .toFormatter(ResolverStyle.SMART, IsoChronology.INSTANCE);
			.toFormatter(Locale.getDefault(Locale.Category.FORMAT));
	}
}