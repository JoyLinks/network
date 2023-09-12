package com.joyzl.network.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.joyzl.network.Utility;
import com.joyzl.network.http.ContentDisposition;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;

/**
 * Content-Type: multipart/form-data<br>
 * RFC7233<br>
 * RFC7204<br>
 * RFC7578 Returning Values from Forms: multipart/form-data<br>
 * 
 * @author ZhangXi 2023年9月8日
 */
public class MultipartFormData extends WEBContentCoder {

	/**
	 * 编码POST请求多个数据部分通过boundary分隔
	 * <p>
	 * 必须设置Content-Type: multipart/form-data; boundary=BOUNDARY<br>
	 * 必须完整编码才能获知Content-Length
	 */
	public static void write(HTTPWriter writer, WEBRequest request, ContentType type) throws IOException {
		Part part;
		// PARAMETERS
		if (request.hasParameter()) {
			part = new Part();
			part.addHeader(Part.CONTENT_TYPE);
			final ContentDisposition contentDisposition = new ContentDisposition(ContentDisposition.FORM_DATA);
			for (Entry<String, String[]> entry : request.getParametersMap().entrySet()) {
				for (int index = 0; index < entry.getValue().length; index++) {
					// BOUNDARY
					writer.write(BOUNDARY_TAG);
					writer.write(type.getBoundary());
					writer.write(HTTPCoder.CRLF);
					// HEADERS
					contentDisposition.setField(entry.getKey());
					part.addHeader(contentDisposition);
					HTTPCoder.writeHeaders(writer, part);
					// CONTENT
					writer.write(entry.getValue()[index], HTTPCoder.URL_CHARSET);
					writer.write(HTTPCoder.CRLF);
				}
			}
		}

		// PARTS
		if (request.getContent() != null) {
			if (request.getContent() instanceof Collection) {
				for (Object item : (Collection<?>) request.getContent()) {
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
	 * 解码POST请求多个数据部分通过boundary分隔
	 * <p>
	 * 必须获取Content-Type: multipart/form-data; boundary=BOUNDARY<br>
	 * 必须接收完整数据才能解码Content-Length
	 */
	public static void read(HTTPReader reader, WEBRequest request, ContentType type) throws IOException {
		// multipart/form-data;boundary=---------------------------7d33a816d302b6
		// 必须判断Content-Length如果未附加任何文件和块数据则可能不会传递boundary，同时Content-Length==0

		// RFC 7233,RFC 7204
		// RFC7578 Returning Values from Forms: multipart/form-data
		// 分隔 [--BOUNDARY|CR|LF]
		// 结束 [--BOUNDARY--CR|LF]
		// Content-Type 默认为 "text/plain"

		// --WebKitFormBoundary7MA4YWxkTrZu0gW|CRLF
		// Content-Disposition: form-data; name="field1"|CRLF
		// CRLF
		// TEXT Content|CRLF
		// --WebKitFormBoundary7MA4YWxkTrZu0gW|CRLF
		// Content-Disposition:form-data;name="file";filename="/C:/Users/simon/Desktop/中后端框架.txt"|CRLF
		// Content-Type: text/plain|CRLF
		// CRLF
		// DATA|CRLF
		// --WebKitFormBoundary7MA4YWxkTrZu0gW--|CRLF

		// Content-Transfer-Encoding支持以下数据格式：BASE64,QUOTED-PRINTABLE,8BIT,7BIT,BINARY,X-TOKEN
		// 7BIT:ASCII格式

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
						final ContentDisposition disposition = ContentDisposition.parse(part.getHeader(ContentDisposition.NAME));
						if (disposition == null) {
							throw new IOException("PART消息头Content-Disposition缺失");
						} else if (ContentDisposition.FORM_DATA.equalsIgnoreCase(disposition.getDisposition())) {
							if (Utility.noEmpty(disposition.getFilename())) {
								// 数据块保存为临时文件
								final File file = File.createTempFile("JOYZL_HTTP_PART", ".tmp");
								try (OutputStream output = new FileOutputStream(file, true)) {
									if (reader.readBy(output, HTTPCoder.CRLF, boundary)) {
										part.setContent(file);
										parts.add(part);
									} else {
										throw new IOException("PART内容意外结束");
									}
								}
							} else {
								// 可参数化的PART块直接添加到参数集合中
								if (reader.readAt(HTTPCoder.CRLF, boundary)) {
									request.addParameter(disposition.getField(), reader.string());
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
							// 在HTTP场景中,第一个参数总是固定不变的"form-data"
							throw new IOException("PART消息头Content-Disposition类型无效:" + disposition);
						}
					} else {
						throw new IOException("PART消息头解析失败");
					}
				}
				request.setContent(parts);
			} else {
				throw new IOException("PART BOUNDARY意外结束");
			}
		} else {
			throw new IOException("PART BOUNDARY意外结束");
		}
	}

	public static void write(HTTPWriter writer, WEBResponse response, ContentType type) throws IOException {
		throw new UnsupportedOperationException();
	}

	public static void read(HTTPReader reader, WEBResponse response, ContentType type) throws IOException {
		throw new UnsupportedOperationException();
	}
}