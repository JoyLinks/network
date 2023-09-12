package com.joyzl.network.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;

/**
 * Content-Type: multipart/byteranges
 * 
 * @author ZhangXi 2023年9月8日
 */
public class MultipartByteranges extends WEBContentCoder {

	/**
	 * 编码多部份响应,多个数据部分通过boundary分隔
	 * 
	 * <p>
	 * 必须设置Content-Type: multipart/byteranges; boundary=BOUNDARY<br>
	 * 必须完整编码才能获知Content-Length
	 */
	public static void write(HTTPWriter writer, WEBResponse response, ContentType type) throws IOException {
		// PARTS
		if (response.getContent() != null) {
			if (response.getContent() instanceof Collection) {
				Collection<?> collection = (Collection<?>) response.getContent();
				Part part;
				for (Object item : collection) {
					part = (Part) item;
					// BOUNDARY
					writer.write(BOUNDARY_TAG);
					writer.write(type.getBoundary());
					writer.write(HTTPCoder.CRLF);
					// HEADERS
					HTTPCoder.writeHeaders(writer, part);
					// CONTENT
					writer.writeContent(part.getContent());
					writer.write(HTTPCoder.CRLF);
				}
			} else {
				throw new IllegalArgumentException("响应内容不是有效的Part集合");
			}
		}

		// END
		writer.write(BOUNDARY_TAG);
		writer.write(type.getBoundary());
		writer.write(BOUNDARY_TAG);
		writer.write(HTTPCoder.CRLF);
	}

	/**
	 * 解码多部份响应,多个数据部分通过boundary分隔
	 * 
	 * <p>
	 * 必须获取Content-Type: multipart/byteranges; boundary=BOUNDARY<br>
	 * 必须接收完整数据才能解码Content-Length
	 */
	public static void read(HTTPReader reader, WEBResponse response, ContentType type) throws IOException {
		// 首先查找第一个分隔标记
		if (reader.readAt(BOUNDARY_TAG, type.getBoundary())) {
			if (reader.readTo(HTTPCoder.CRLF)) {
				if (BOUNDARY_TAG.contentEquals(reader.sequence())) {
					// 全部结束
					return;
				}
				final String boundary = BOUNDARY_TAG + type.getBoundary();
				final List<Part> parts = new ArrayList<>();
				while (true) {
					final Part part = new Part();
					if (HTTPCoder.readHeaders(reader, part)) {
						// TODO 存在多次数据复制，需优化
						try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
							if (reader.readBy(output, HTTPCoder.CRLF, boundary)) {
								part.setContent(new ByteArrayInputStream(output.toByteArray()));
								parts.add(part);
							} else {
								throw new IOException("PART内容意外结束");
							}
						}
						if (reader.readTo(HTTPCoder.CRLF)) {
							if (reader.sequence().length() == 0) {
								continue;
							} else if (BOUNDARY_TAG.contentEquals(reader.sequence())) {
								// 结束标记
								break;
							} else {
								throw new IOException("PART意外的结束字符:" + reader.string());
							}
						}
					} else {
						throw new IOException("PART消息头解析失败");
					}
				}
				response.setContent(parts);
			} else {
				throw new IOException("PART BOUNDARY意外结束");
			}
		} else {
			throw new IOException("PART BOUNDARY意外结束");
		}
	}

	public static void write(HTTPWriter writer, WEBRequest request, ContentType type) throws IOException {
		throw new UnsupportedOperationException();
	}

	public static void read(HTTPReader reader, WEBRequest request, ContentType type) throws IOException {
		throw new UnsupportedOperationException();
	}
}