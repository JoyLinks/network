/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * 具有多个权重值的消息头
 * 
 * @author ZhangXi
 * @date 2021年10月20日
 */
public abstract class QualityValueHeader extends Header {

	final static QualityValue[] EMPTY = new QualityValue[0];
	private QualityValue[] values = EMPTY;

	@Override
	public String getHeaderValue() {
		if (values == EMPTY) {
			return Utility.EMPTY_STRIN;
		}
		if (values.length == 1) {
			return getValue();
		}
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < values.length; index++) {
			if (index > 0) {
				builder.append(HTTPCoder.COMMA);
				builder.append(HTTPCoder.SPACE);
			}
			builder.append(values[index].getValue());
			if (values[index].getQuality() < 1) {
				builder.append(HTTPCoder.SEMI);
				builder.append("q=");
				builder.append(Float.toString(values[index].getQuality()));
			}
		}
		return builder.toString();
	}

	// text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8

	@Override
	public void setHeaderValue(String value) {
		char c;
		for (int begin = 0, end = 0, semi = 0, equal = 0, index = 0; index <= value.length(); index++) {
			if (index >= value.length() || (c = value.charAt(index)) == HTTPCoder.COMMA) {
				if (begin < semi && semi < equal && equal < end) {
					addValue(value.substring(begin, semi), Float.parseFloat(value.substring(equal + 1, end + 1)));
				} else {
					addValue(value.substring(begin, end + 1));
				}
				begin = end = index + 1/* 跳过逗号 */;
				semi = equal = index;
			} else if (c == HTTPCoder.SEMI) {
				semi = index;
			} else if (c == HTTPCoder.EQUAL) {
				equal = index;
			} else if (Character.isWhitespace(c)) {
				if (begin >= end) {
					begin = index + 1;
				}
			} else {
				end = index;
			}
		}
	}

	public final void addValue(String v) {
		addValue(v, 1);
	}

	public final void addValue(String v, float q) {
		if (values == EMPTY) {
			values = new QualityValue[] { new QualityValue(v, q) };
		} else {
			int position = 0;
			for (; position < values.length; position++) {
				if (values[position].getValue().equals(v)) {
					values[position].setQuality(q);
					return;
				}
				if (values[position].getQuality() < q) {
					break;
				}
			}
			final QualityValue[] news = new QualityValue[values.length + 1];
			for (int index = 0; index < news.length; index++) {
				if (index == position) {
					news[index] = new QualityValue(v, q);
				} else if (index < position) {
					news[index] = values[index];
				} else if (index > position) {
					news[index] = values[index - 1];
				}
			}
			values = news;
		}
	}

	public String getValue() {
		if (values == EMPTY) {
			return null;
		}
		return values[0].getValue();
	}

	public String getValue(int index) {
		return values[index].getValue();
	}

	public QualityValue[] getValues() {
		return values;
	}

	public int size() {
		return values.length;
	}
}
