package com.joyzl.network.http;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * 多部分内容的部分形式
 * 
 * @author ZhangXi 2024年11月28日
 */
public class MultipartRange {

	private String contentType;
	private String contentEncoding;
	private final String contentRange;
	private InputStream content;
	private final long size;

	public MultipartRange(long total, long start, long end) {
		StringBuilder sb = new StringBuilder();
		sb.append(ContentRange.UNIT);
		sb.append(HTTP1Coder.SPACE);
		sb.append(start);
		sb.append(HTTP1Coder.MINUS);
		sb.append(end);
		sb.append(HTTP1Coder.SLASH);
		sb.append(total);
		contentRange = sb.toString();
		size = end - start + 1;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String value) {
		contentType = value;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String value) {
		contentEncoding = value;
	}

	public String getContentRange() {
		return contentRange;
	}

	public InputStream getContent() {
		return content;
	}

	public void setContent(InputStream value) {
		content = value;
	}

	/**
	 * 获取当前块字节数量，包含分块头和分块内容，不包含分隔符
	 */
	public int length() {
		/*-
		 * Content-Type: text/html<CRLF>
		 * Content-Range: bytes 100-200/1270<CRLF>
		 * <CRLF>
		 * CONTENT<CRLF>
		 */
		int length = (int) size;
		length += ContentType.NAME.length() + 2;
		length += contentType.length() + 2;
		if (contentEncoding != null) {
			length += ContentEncoding.NAME.length() + 2;
			length += contentEncoding.length() + 2;
		}
		length += 4;
		return length;
	}

	public static class MultipartRanges extends ArrayList<MultipartRange> {
		private static final long serialVersionUID = 1L;
		private final String boundary;

		public MultipartRanges() {
			this.boundary = ContentType.boundary();
		}

		public MultipartRanges(String boundary) {
			this.boundary = boundary;
		}

		/**
		 * 获取集合中所有部分的字节总长度，包含分块头和分块内容长度
		 */
		public int length() {
			/*-
			 * --boundary<CRLF>
			 * Content-Type: text/html<CRLF>
			 * Content-Range: bytes 100-200/1270<CRLF>
			 * <CRLF>
			 * CONTENT<CRLF>
			 * --boundary--<CRLF>
			 */
			int length = 0;
			for (int index = 0; index < size(); index++) {
				length += boundary.length() + 4;
				length += get(index).length();
			}
			if (length > 0) {
				length += length += 6;
			}
			return length;
		}

		public String getBoundary() {
			return boundary;
		}

		private int index = 0;

		public boolean hasNext() {
			return index < size();
		}

		public MultipartRange next() {
			return get(index++);
		}

		public void resetNext() {
			index = 0;
		}
	}
}