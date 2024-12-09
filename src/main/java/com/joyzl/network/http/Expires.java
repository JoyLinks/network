package com.joyzl.network.http;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Expires: Wed, 21 Oct 2023 07:28:00 GMT
 * 
 * @author ZhangXi 2023年9月15日
 */
public class Expires extends Header {

	public final static String NAME = HTTP.Expires;

	private ZonedDateTime value;

	@Override
	public String getHeaderName() {
		return HTTP.Expires;
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