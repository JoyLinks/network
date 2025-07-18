/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.joyzl.network.Utility;

/**
 * Range是一个请求首部，告知服务器返回文件的哪一部分。在一个Range首部中，可以一次性请求多个部分，服务器会以 multipart
 * 文件的形式将其返回。 如果服务器返回的是范围响应，需要使用 206 Partial Content 状态码。 假如所请求的范围不合法，那么服务器会返回
 * 416 Range Not Satisfiable 状态码，表示客户端错误。 服务器允许忽略Range首部，从而返回整个文件，状态码用200。
 * 
 * <pre>
 * Range: <unit>=<range-start>-
 * Range: <unit>=<range-start>-<range-end>
 * Range: <unit>=<range-start>-<range-end>, <range-start>-<range-end>
 * Range: <unit>=<range-start>-<range-end>, <range-start>-<range-end>, <range-start>-<range-end>
 * Range: bytes=200-1000, 2000-6576, 19000-
 * </pre>
 * 
 * <unit>通常是bytes
 * 
 * @author ZhangXi
 * @date 2021年1月6日
 */
public final class Range extends Header {

	public final static String NAME = HTTP1.Range;
	public final static String UNIT = "bytes";

	private final List<ByteRange> ranges = new ArrayList<>();

	@Override
	public String getHeaderName() {
		return HTTP1.Range;
	}

	@Override
	public String getHeaderValue() {
		if (ranges == null || ranges.isEmpty()) {
			return null;
		}

		// bytes=200-1000, 2000-6576, 19000-
		StringBuilder sb = new StringBuilder();
		ByteRange range = null;
		for (int index = 0; index < ranges.size(); index++) {
			range = ranges.get(index);
			if (sb.length() > 0) {
				sb.append(HTTP1Coder.COMMA);
				sb.append(HTTP1Coder.SPACE);
			} else {
				sb.append(UNIT);
				sb.append(HTTP1Coder.EQUAL);
			}
			sb.append(range.getStart());
			sb.append(HTTP1Coder.MINUS);
			if (range.getEnd() > 0) {
				sb.append(range.getEnd());
			}
		}
		return sb.toString();
	}

	@Override
	public void setHeaderValue(String value) {
		// bytes=100-
		// bytes=-100
		// bytes=100-200
		// bytes=100-200, 201-300

		if (value.startsWith(UNIT)) {
			int start = value.indexOf(HTTP1Coder.EQUAL, UNIT.length());
			if (start > 0) {
				int minus, end;
				do {
					minus = value.indexOf(HTTP1Coder.MINUS, ++start);
					if (minus > 0) {
						end = value.indexOf(HTTP1Coder.COMMA, minus + 1);
						if (end < 0) {
							end = value.length();
						}
						if (start < minus) {
							if (minus + 1 < end) {
								add(new ByteRange(//
									Long.parseUnsignedLong(value, start, minus, 10), //
									Long.parseUnsignedLong(value, minus + 1, end, 10)//
								));
							} else {
								add(new ByteRange(Long.parseUnsignedLong(value, start, minus, 10)));
							}
						} else if (minus + 1 < end) {
							add(new ByteRange(-1, Long.parseUnsignedLong(value, minus + 1, end, 10)));
						} else {
							break;
						}
						start = end + 1;
						while (start < value.length()) {
							if (Character.isWhitespace(value.charAt(start))) {
								start++;
							} else {
								break;
							}
						}
					} else {
						break;
					}
				} while (start < value.length());
			}
		}
	}

	public final static Range parse(String value) {
		if (Utility.noEmpty(value)) {
			Range header = new Range();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public void add(ByteRange range) {
		ranges.add(range);
	}

	public boolean hasRanges() {
		if (ranges == null || ranges.isEmpty()) {
			return false;
		}
		return true;
	}

	public List<ByteRange> getRanges() {
		return ranges;
	}

	/**
	 * 按位置进行排序的排序对象实例
	 */
	final static Comparator<ByteRange> COMPARATOR = new Comparator<ByteRange>() {
		@Override
		public int compare(ByteRange a, ByteRange b) {
			return Long.compare(a.getStart(), b.getStart());
		}
	};

	public final static class ByteRange {

		private long start;
		private long end;

		public ByteRange() {
			this(-1, -1);
		}

		public ByteRange(long s) {
			this(s, -1);
		}

		public ByteRange(long s, long e) {
			start = s;
			end = e;
		}

		/**
		 * 检查范围是否有效，如果未指定结束位置，则自动填充有效的结束位置
		 * 
		 * @param total 资源总长度字节
		 * @param block 建议分块大小
		 * @param max 可部分请求的最大长度字节
		 * @return true有效/false无效
		 */
		public boolean valid(long total, int block, int max) {
			// Range: bytes=0-499 表示第 0-499 字节范围的内容
			// Range: bytes=500-999 表示第 500-999 字节范围的内容
			// Range: bytes=-500 表示最后 500 字节的内容
			// Range: bytes=500- 表示从第 500 字节开始到文件结束部分的内容
			// Range: bytes=0-0,-1 表示第一个和最后一个字节

			if (start < 0) {
				if (end < 0) {
					return false;
				}
				if (end >= total || end > max) {
					return false;
				}
				// total=100, end=40 > start=60, end=99;
				start = total - end;
				end = total - 1;
			} else if (start >= total) {
				return false;
			} else if (end < 0) {
				end = total - 1;
				if (end - start > block) {
					end = start + block;
				}
			} else if (end < start) {
				return false;
			} else if (end >= total) {
				return false;
			} else if (end - start >= total) {
				return false;
			} else if (end - start >= max) {
				return false;
			}
			return true;
		}

		/**
		 * size = end - start + 1
		 */
		public long getSize() {
			// 开始结束索引均计算在内 0-100=101个字节
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
}