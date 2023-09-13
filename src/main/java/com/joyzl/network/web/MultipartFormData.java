package com.joyzl.network.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
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
 * Content-Type: multipart/mixed<br>
 * 
 * RFC1867 Form-based File Upload in HTML<br>
 * RFC7233<br>
 * RFC7204<br>
 * RFC7578 Returning Values from Forms: multipart/form-data<br>
 * 
 * @author ZhangXi 2023年9月8日
 */
public class MultipartFormData extends WEBContentCoder {

	/** 特殊约定字段名，指定参数字符编码 */
	final static String CHARSET_NAME = "_charset_";

	/*-
	 * 请求表单
	 * 
	 * <FORM ACTION="http://www.joyzl.com/test" ENCTYPE="multipart/form-data" METHOD=POST>
	 * 		<INPUT TYPE=TEXT NAME=field1>
	 * 		<INPUT TYPE=FILE NAME=files>
	 * </FORM>
	 * 
	 * 格式参考（单个文件）
	 * 
	 * POST http://www.joyzl.com/test HTTP/1.1
	 * Content-Type: multipart/form-data, boundary=AaB03x
	 * Content-Length: 1280
	 * 
	 * --AaB03x
	 * content-disposition: form-data; name="_charset_"
	 * 
	 * iso-8859-1
	 * --AaB03x
	 * Content-Disposition: form-data; name="field1"
	 * 
	 * ...TEXT Content...
	 * --AaB03x
	 * Content-Disposition: form-data; name="files"; filename="test.txt"
	 * Content-Type: text/plain
	 * 
	 * ...FILE DATA...
	 * --AaB03x--
	 * 
	 * 格式参考（多个文件）
	 * 
	 * POST http://www.joyzl.com/test HTTP/1.1
	 * Content-Type: multipart/form-data, boundary=AaB03x
	 * Content-Length: 1280
	 * 
	 * --AaB03x
	 * Content-Disposition: form-data; name="field1"
	 * 
	 * ...TEXT Content...
	 * --AaB03x
	 * Content-disposition: form-data; name="files"
	 * Content-type: multipart/mixed, boundary=BbC04y
	 * 
	 * --BbC04y
	 * Content-disposition: attachment; filename="file1.txt"
	 * Content-Type: text/plain
	 * 
	 * ...FILE DATA...
	 * --BbC04y
	 * Content-disposition: attachment; filename="file2.gif"
	 * Content-type: image/gif
	 * Content-Transfer-Encoding: binary
	 * 
	 * ...FILE DATA...
	 * --BbC04y--
	 * --AaB03x--
	 * 
	 */

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
					writer.buffer().write(input(part));
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

		// 已弃用
		// Content-Transfer-Encoding支持以下数据格式：BASE64,QUOTED-PRINTABLE,8BIT,7BIT,BINARY,X-TOKEN
		// 7BIT:ASCII格式

		// 首先查找第一个分隔标记
		final String boundary = BOUNDARY_TAG + type.getBoundary();
		if (reader.readTo(boundary)) {
			if (reader.readTo(HTTPCoder.CRLF)) {
				if (BOUNDARY_TAG.contentEquals(reader.sequence())) {
					return; // 全部结束
				}

				List<Part> parts;
				if (request.getContent() == null) {
					request.setContent(parts = new ArrayList<>());
				} else {
					parts = (List<Part>) request.getContent();
				}

				while (true) {
					final Part part = new Part();
					if (HTTPCoder.readHeaders(reader, part)) {
						final ContentDisposition disposition = ContentDisposition.parse(part.getHeader(ContentDisposition.NAME));
						if (disposition == null) {
							throw new IOException("PART消息头Content-Disposition缺失");
						}
						final ContentType contentType = ContentType.parse(part.getHeader(ContentType.NAME));
						if (ContentDisposition.FORM_DATA.equalsIgnoreCase(disposition.getDisposition())) {
							if (Utility.noEmpty(disposition.getFilename())) {
								part.setContent(readFile(reader, boundary));
								parts.add(part);
							} else {
								if (contentType == null) {
									// 未指定Content-Type的参数块
									request.addParameter(disposition.getField(), readArgment(reader, contentType, boundary));
								} else if (MIMEType.MULTIPART_MIXED.equalsIgnoreCase(contentType.getType())) {
									// 混合数据块
									read(reader, request, contentType);
								} else {
									// 指定Content-Type的参数块
									request.addParameter(disposition.getField(), readArgment(reader, contentType, boundary));
								}
							}
						} else if (ContentDisposition.ATTACHMENT.equalsIgnoreCase(disposition.getDisposition())) {
							part.setContent(readFile(reader, boundary));
							parts.add(part);
						} else {
							throw new IOException("PART消息头Content-Disposition类型无效:" + disposition);
						}
					} else {
						throw new IOException("PART消息头解析失败");
					}

					if (reader.readTo(HTTPCoder.CRLF)) {
						if (BOUNDARY_TAG.contentEquals(reader.sequence())) {
							break;// 结束标记
						}
					}
				}
			} else {
				throw new IOException("PART BOUNDARY意外结束");
			}
		} else {
			throw new IOException("PART BOUNDARY意外结束");
		}
	}

	/**
	 * 读取文件数据块保存为临时文件
	 */
	static File readFile(HTTPReader reader, String boundary) throws IOException {
		final File file = File.createTempFile("JOYZL_HTTP_PART", ".tmp");
		try (OutputStream output = new FileOutputStream(file, true)) {
			if (reader.readBy(output, HTTPCoder.CRLF, boundary)) {
				return file;
			} else {
				throw new IOException("PART内容意外结束");
			}
		}
	}

	/**
	 * 读取参数块
	 */
	static String readArgment(HTTPReader reader, ContentType type, String boundary) throws IOException {
		Charset charset = HTTPCoder.URL_CHARSET;
		if (type != null) {
			if (type.getCharset() != null) {
				if (!HTTPCoder.URL_CHARSET_NAME.equalsIgnoreCase(type.getCharset())) {
					charset = Charset.forName(type.getCharset());
				}
			}
		}
		if (reader.readAt(HTTPCoder.CRLF, boundary)) {
			final byte[] bytes = new byte[reader.sequence().length()];
			for (int index = 0; index < bytes.length; index++) {
				bytes[index] = (byte) reader.sequence().charAt(index);
			}
			final ByteBuffer buffer = ByteBuffer.wrap(bytes);
			final CharBuffer chars = charset.decode(buffer);
			return chars.toString();
		} else {
			throw new IOException("PART内容意外结束");
		}
	}

	public static void write(HTTPWriter writer, WEBResponse response, ContentType type) throws IOException {
		throw new UnsupportedOperationException();
	}

	public static void read(HTTPReader reader, WEBResponse response, ContentType type) throws IOException {
		throw new UnsupportedOperationException();
	}
}