/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Content-Range响应首部显示的是一个数据片段在整个文件中的位置。
 * 
 * <pre>
 * Content-Range: <unit> <range-start>-<range-end>/<size>
 * Content-Range: <unit> <range-start>-<range-end>/＊
 * Content-Range: <unit> ＊/<size>
 * Content-Range: bytes 200-1000/67589
 * </pre>
 * 
 * <unit> 数据区间所采用的单位。通常是字节（byte）。 <br>
 * <range-start> 一个整数，表示在给定单位下，区间的起始值。<br>
 * <range-end> 一个整数，表示在给定单位下，区间的结束值。<br>
 * <size> 整个文件的大小（如果大小未知则用"*"表示）。
 * 
 * @author ZhangXi
 * @date 2021年1月13日
 */
public class ContentRange extends Header {

	public final static String NAME = HTTP.Content_Range;

	final static String UNIT = "bytes";

	private long length;
	private long start;
	private long end;

	public ContentRange() {
		this(0, 0, 0);
	}

	public ContentRange(long s, long e) {
		this(s, e, 0);
	}

	public ContentRange(long s, long e, long l) {
		length = l;
		start = s;
		end = e;
	}

	@Override
	public String getHeaderName() {
		return HTTP.Content_Range;
	}

	@Override
	public String getHeaderValue() {
		StringBuilder sb = new StringBuilder();
		sb.append(UNIT);
		sb.append(HTTPCoder.SPACE);
		sb.append(start);
		sb.append(HTTPCoder.MINUS);
		sb.append(end);
		sb.append(HTTPCoder.SLASH);
		if (length > 0) {
			sb.append(length);
		} else {
			sb.append('*');
		}
		return sb.toString();
	}

	@Override
	public void setHeaderValue(String value) {
		// bytes 200-1000/67589
		int a, b;
		if ((a = value.indexOf(UNIT)) >= 0) {
			a = value.indexOf(HTTPCoder.SPACE, a + UNIT.length());
			b = value.indexOf(HTTPCoder.MINUS, ++a);
			start = Long.parseUnsignedLong(value, a, b, 10);
			a = value.indexOf(HTTPCoder.SLASH, ++b);
			end = Long.parseUnsignedLong(value, b, a, 10);
			length = Long.parseUnsignedLong(value, ++a, value.length(), 10);
		} else {
			throw new UnsupportedOperationException("无法识别的数据单位:" + value);
		}
	}

	public final static ContentRange parse(String value) {
		if (Utility.noEmpty(value)) {
			ContentRange header = new ContentRange();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	/**
	 * 检查范围是否有效
	 * 
	 * @return true有效/false无效
	 */
	public boolean isValid() {
		if (start < 0 || end <= 0) {
			return false;
		}
		if (start >= end || start >= length || end >= length) {
			return false;
		}
		if (end - start > length) {
			return false;
		}

		return true;
	}

	/**
	 * 获取数据总长度
	 */
	public long getLength() {
		return length;
	}

	public void setLength(long value) {
		length = value;
	}

	/**
	 * 获取Start~End数据量
	 * 
	 * @return end - start + 1
	 */
	public long getSize() {
		// 0~100 = 101
		// 0~0 = 1
		return end - start + 1;
	}

	public void setStart(long value) {
		start = value;
	}

	public long getStart() {
		return start;
	}

	public void setEnd(long value) {
		end = value;
	}

	public long getEnd() {
		return end;
	}
}
