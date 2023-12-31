/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

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
}