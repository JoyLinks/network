/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.ContentDisposition;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.http.Message;

/**
 * WEB HTTP 相关编解码
 * 
 * @author ZhangXi
 * @date 2021年10月13日
 */
@Deprecated
public class WEBCoder extends HTTPCoder {

	/** 最大请求长度 16Mb */
	public final static int FRAME_MAX = 1024 * 1024 * 16;
	/** 实体数据发送块大小 1M */
	public final static int BLOCK = 1024 * 1024;

	/** BOUNDARY 开始和结束标志,分隔时[--BOUNDARY],结束时[--BOUNDARY--] */
	public final static String BOUNDARY_TAG = "--";

	// Content-Type: 内容类型
	// Content-Length: 内容长度
	// Content-Encoding: 内容压缩方式，内容提供者决定
	// Transfer-Encoding: 内容传输方式，通常是长度不确定而采用逐块发送

	/**
	 * 检查消息内容实体是否合法并返回字节长度
	 * 
	 * @return 0~n 字节长度 / <0 无法获取字节长度
	 * @throws IOException
	 * @throws IllegalStateException
	 */
	public static int size(final Object content) throws IOException {
		if (content == null) {
			// 未设置消息内容，返回0字节数量
			return 0;
		} else if (content instanceof File) {
			// 文件字节数量限制2GB
			long length = ((File) content).length();
			if (length > Integer.MAX_VALUE) {
				// 因超出整形范围而标记为无法确定字节数量
				return -1;
			}
			return (int) length;
		} else if (content instanceof DataBuffer) {
			return ((DataBuffer) content).readable();
		} else if (content instanceof InputStream) {
			return ((InputStream) content).available();
		} else if (content instanceof Collection<?>) {
			return -1;
		} else {
			throw new IllegalStateException("内容实体类型无效:" + content.getClass());
		}
	}

	/**
	 * 编码POST请求键值对数据
	 * <p>
	 * 必须设置Content-Type: application/x-www-form-urlencoded<br>
	 * 必须完整编码才能获知Content-Length
	 */
	public final static void writeWWWForm(HTTPWriter writer, WEBRequest request) throws IOException {
		// name=value&name=value
		// POST的键值对参数 没有 [ ENTER LINE ] 结束标志
		// 必须编码完成才能提供Content-Length

		if (request.hasParameter()) {
			boolean more = false;
			for (Entry<String, String[]> item : request.getParametersMap().entrySet()) {
				for (int index = 0; index < item.getValue().length; index++) {
					if (more) {
						writer.write(HTTPCoder.AND);
					} else {
						more = true;
					}
					writer.write(item.getKey());
					writer.write(HTTPCoder.EQUAL);
					writer.write(URLEncoder.encode(item.getValue()[index], HTTPCoder.URL_CHARSET));
				}
			}
		}
	}

	/**
	 * 解码POST请求键值对数据
	 * <p>
	 * 必须获取Content-Type: application/x-www-form-urlencoded<br>
	 * 必须接收完整数据才能解码Content-Length
	 */
	public final static void readWWWForm(HTTPReader reader, WEBRequest request) throws IOException {
		// name=value&name=value
		// POST的键值对参数 没有 [ ENTER LINE ] 结束标志
		// 读取键值对参数，调用此方法之前应当判断数据流长度，
		// 因Content-Type=application/x-www-form-urlencoded提交的POST参数无结束标志
		// 因此在调用此方法读取POST参数之前应当通过Content-Length判断数据长度是否足够

		String name;
		while (reader.readTo(HTTPCoder.EQUAL)) {
			name = reader.string();
			if (reader.readTo(HTTPCoder.AND)) {
				request.addParameter(name, URLDecoder.decode(reader.string(), HTTPCoder.URL_CHARSET));
			} else {
				request.addParameter(name, null);
			}
		}
	}

	/**
	 * 编码POST请求多个数据部分通过boundary分隔
	 * <p>
	 * 必须设置Content-Type: multipart/form-data; boundary=BOUNDARY<br>
	 * 必须完整编码才能获知Content-Length
	 */
	public final static void writeMultipartFormData(HTTPWriter writer, WEBRequest request, ContentType type) throws IOException {
		Part part;
		// PARAMETERS
		if (request.hasParameter()) {
			part = new Part();
			final ContentDisposition content_disposition = new ContentDisposition();
			content_disposition.setDisposition(ContentDisposition.FORM_DATA);
			for (Entry<String, String[]> entry : request.getParametersMap().entrySet()) {
				for (int index = 0; index < entry.getValue().length; index++) {
					// BOUNDARY
					writer.write(BOUNDARY_TAG);
					writer.write(type.getBoundary());
					writer.write(HTTPCoder.CRLF);
					// HEADERS
					content_disposition.setField(entry.getKey());
					part.addHeader(content_disposition);
					HTTPCoder.writeHeaders(writer, part);
					// CONTENT
					writer.write(entry.getValue()[index]);
					writer.write(HTTPCoder.CRLF);
				}
			}
		}

		// PARTS
		if (request.getContent() != null) {
			if (request.getContent() instanceof Collection) {
				Collection<?> collection = (Collection<?>) request.getContent();
				for (Object item : collection) {
					part = (Part) item;
					// BOUNDARY
					writer.write(BOUNDARY_TAG);
					writer.write(type.getBoundary());
					writer.write(HTTPCoder.CRLF);
					// HEADERS
					HTTPCoder.writeHeaders(writer, part);
					// CONTENT
					// writer.writeContent(part.getContent());
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
	public final static void readMultipartFormData(HTTPReader reader, WEBRequest request, ContentType type) throws IOException {
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

		// System.out.println();
		// System.out.print(reader.toString());
		// System.out.println();

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

	/**
	 * 编码多部份响应,多个数据部分通过boundary分隔
	 * 
	 * <p>
	 * 必须设置Content-Type: multipart/byteranges; boundary=BOUNDARY<br>
	 * 必须完整编码才能获知Content-Length
	 */
	public final static void writeMultipart(HTTPWriter writer, WEBResponse response, ContentType type) throws IOException {
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
					// writer.writeContent(part.getContent());
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
	public final static void readMultipart(HTTPReader reader, WEBResponse response, ContentType type) throws IOException {
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

	/**
	 * 读取分块传输数据
	 * 
	 * @param reader
	 * @param response
	 * @return true 全部完成 / false 数据不足
	 * @throws IOException
	 */
	public final static boolean readChunked(HTTPReader reader, WEBResponse response) throws IOException {
		reader.mark();
		if (reader.readTo(HTTPCoder.CRLF)) {
			int length = Integer.parseUnsignedInt(reader.sequence(), 0, reader.sequence().length(), 10);
			if (reader.buffer().readable() >= length + 2/* CRLF */) {
				if (length == 0) {
					if (reader.readTo(HTTPCoder.CRLF)) {
						return true;
					} else {
						return false;
					}
				} else {
					DataBuffer buffer = (DataBuffer) response.getContent();
					if (buffer == null) {
						response.setContent(buffer = DataBuffer.instance());
					}
					reader.buffer().bounds(length);
					buffer.residue(reader.buffer());
					reader.buffer().discard();
					reader.readTo(HTTPCoder.CRLF);
					return false;
				}
			}
		}
		reader.reset();
		return false;
	}

	/**
	 * 输出分块传输数据
	 * 
	 * @param writer
	 * @param response
	 * @return true 全部完成 / false 单块输出
	 * @throws IOException
	 */
	public final static boolean writeChunked(HTTPWriter writer, WEBResponse response) throws IOException {
		// 分块的形式进行发送时无Content-Length值
		// 分块长度以十六进制的形式表示，后面紧跟着 '\r\n'
		// 之后是分块本身，后面也是'\r\n'
		// 终止块是一个常规的分块，不同之处在于其长度为0

		InputStream input;
		if (response.getContent() instanceof File) {
			input = new FileInputStream((File) response.getContent());
			response.setContent(input);
		} else if (response.getContent() instanceof InputStream) {
			input = (InputStream) response.getContent();
		} else {
			throw new IllegalArgumentException("不支持的响应内容:" + response.getContent());
		}

		boolean end = false;
		int length = input.available();
		if (length - BLOCK > 1024) {
			length = BLOCK;
		} else {
			end = true;
		}

		// 长度
		writer.write(Integer.toHexString(length));
		writer.write(HTTPCoder.CRLF);
		// 内容块
		while (length-- > 0) {
			writer.buffer().write(input.read());
		}
		writer.write(HTTPCoder.CRLF);
		if (end) {
			// 结束块
			writer.write("0");
			writer.write(HTTPCoder.CRLF);
			writer.write(HTTPCoder.CRLF);
		}
		return end;
	}

	/**
	 * 解码请求的原始数据
	 * 
	 * @param buffer
	 * @param message
	 */
	public final static void readRAW(DataBuffer buffer, Message message) {
		final DataBuffer content = DataBuffer.instance();
		content.residue(buffer);
		message.setContent(content);
	}

	/**
	 * 输出原始数据
	 * 
	 * @param writer
	 * @param message
	 * @return true 全部完成 / false 部分输出
	 * @throws IOException
	 */
	public final static boolean writeRAW(DataBuffer writer, Message message) throws IOException {
		if (message.getContent() == null) {
			return true;
		}

		// DataBuffer
		if (message.getContent() instanceof DataBuffer) {
			final DataBuffer content = (DataBuffer) message.getContent();
			if (content.readable() > 0) {
				if (content.readable() > BLOCK) {
					content.bounds(BLOCK);
					writer.write(content);
					content.discard();
					return false;
				} else {
					writer.write(content);
				}
			}
			message.setContent(null);
			return true;
		}

		// File InputStream
		InputStream input;
		if (message.getContent() instanceof File) {
			input = new FileInputStream((File) message.getContent());
			message.setContent(input);
		} else if (message.getContent() instanceof InputStream) {
			input = (InputStream) message.getContent();
		} else {
			throw new IllegalArgumentException("不支持的响应内容:" + message.getContent().getClass());
		}
		if (input.available() > 0) {
			if (input.available() > BLOCK) {
				int length = BLOCK;
				while (length-- > 0) {
					writer.write(input.read());
				}
				return false;
			} else {
				writer.write(input);
			}
		}
		message.setContent(null);
		return true;
	}
}
