/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPMessage;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.TransferEncoding;

/**
 * HTTP WEB CONTENT 相关编解码
 * 
 * @author ZhangXi
 * @date 2021年10月13日
 */
public class WEBContentCoder extends HTTPCoder {

	/** 最大请求字节数 16Mb */
	public final static int MAX = 1024 * 1024 * 16;
	/** 实体数据发送块大小 8K */
	public final static int BLOCK = 1024 * 8;

	/** BOUNDARY 开始和结束标志,分隔时[--BOUNDARY],结束时[--BOUNDARY--] */
	public final static String BOUNDARY_TAG = "--";

	// WEB Content
	// Content-Type: 内容类型
	// Content-Length: 内容长度
	// Content-Encoding: 内容压缩方式，内容提供者决定
	// Transfer-Encoding: 内容传输方式，通常是长度不确定而采用逐块发送

	// 数据切分的三级策略
	// 业务逻辑层切分，例如 Content-Type: multipart/byteranges
	// 协议编码级切分，例如 Transfer-Encoding: chunked
	// 数据发送级切分，例如 writeRaw()

	/**
	 * 获取消息内容字节数<br>
	 * InputStream 无法保证准确的字节数量
	 * 
	 * @return 0~n / -1 无法判断
	 */
	public static long size(Message message) throws IOException {
		if (message.getContent() == null) {
			return 0;
		}
		if (message.getContent() instanceof File) {
			return ((File) message.getContent()).length();
		}
		if (message.getContent() instanceof DataBuffer) {
			return ((DataBuffer) message.getContent()).readable();
		}
		if (message.getContent() instanceof InputStream) {
			return ((InputStream) message.getContent()).available();
		}
		if (message.getContent() instanceof String) {
			return -1;
		}
		if (message.getContent() instanceof byte[]) {
			return ((byte[]) message.getContent()).length;
		}
		return -1;
	}

	public static InputStream input(final Message message) throws IOException {
		if (message.getContent() instanceof File) {
			final InputStream input = new FileInputStream((File) message.getContent());
			message.setContent(input);
			return input;
		}
		if (message.getContent() instanceof DataBuffer) {
			final InputStream input = new DataBufferInput((DataBuffer) message.getContent());
			message.setContent(input);
			return input;
		}
		if (message.getContent() instanceof InputStream) {
			return (InputStream) message.getContent();
		}
		if (message.getContent() instanceof String) {
			final String text = (String) message.getContent();
			final ByteArrayInputStream input = new ByteArrayInputStream(text.getBytes(URL_CHARSET));
			message.setContent(input);
			return input;
		}
		if (message.getContent() instanceof byte[]) {
			final ByteArrayInputStream input = new ByteArrayInputStream((byte[]) message.getContent());
			message.setContent(input);
			return input;
		}
		throw new IOException("内容实体类型无效:" + message.getContent().getClass());
	}

	public static OutputStream output(final Message message) throws IOException {
		if (message.getContent() instanceof File) {
			final OutputStream output = new FileOutputStream((File) message.getContent());
			message.setContent(output);
			return output;
		}
		if (message.getContent() instanceof DataBuffer) {
			final OutputStream output = new DataBufferOutput((DataBuffer) message.getContent());
			message.setContent(output);
			return output;
		}
		if (message.getContent() instanceof OutputStream) {
			return (OutputStream) message.getContent();
		}
		throw new IOException("内容实体类型无效:" + message.getContent().getClass());
	}

	/**
	 * 请求消息发送前的预处理<br>
	 * Content-Type: application/x-www-form-urlencoded<br>
	 * Content-Type: multipart/form-data<br>
	 * Content-Length: *<br>
	 */
	public static void prepare(WEBRequest request) throws IOException {
		final ContentType contentType = ContentType.parse(request.getHeader(ContentType.NAME));
		if (contentType == null) {
			// RFC7231 缺省为"application/octet-stream"
			check(request);
		} else if (MIMEType.X_WWW_FORM_URLENCODED.equalsIgnoreCase(contentType.getType())) {
			final HTTPWriter writer = new HTTPWriter(DataBuffer.instance());
			XWWWFormUrlencoded.write(writer, request);
			request.addHeader(ContentLength.NAME, Integer.toString(writer.buffer().readable()));
			request.setContent(writer.buffer());
		} else if (MIMEType.MULTIPART_FORMDATA.equalsIgnoreCase(contentType.getType())) {
			final HTTPWriter writer = new HTTPWriter(DataBuffer.instance());
			MultipartFormData.write(writer, request, contentType);
			request.addHeader(ContentLength.NAME, Integer.toString(writer.buffer().readable()));
			request.setContent(writer.buffer());
		} else {
			// 其它类型按原始输出
			check(request);
		}
	}

	/**
	 * 响应消息发送前的预处理<br>
	 * Content-Type: multipart/byteranges<br>
	 * Content-Length: *<br>
	 */
	public static void prepare(WEBResponse response) throws IOException {
		final ContentType contentType = ContentType.parse(response.getHeader(ContentType.NAME));
		if (contentType == null) {
			// RFC7231 缺省为"application/octet-stream"
			check(response);
		} else if (MIMEType.MULTIPART_BYTERANGES.equalsIgnoreCase(contentType.getType())) {
			final HTTPWriter writer = new HTTPWriter(DataBuffer.instance());
			MultipartByteranges.write(writer, response, contentType);
			response.addHeader(ContentLength.NAME, Integer.toString(writer.buffer().readable()));
			response.setContent(writer.buffer());
		} else {
			// 其它类型按原始输出
			check(response);
		}
	}

	/**
	 * 检查消息内容设定长度和传输方式<br>
	 * Transfer-Encoding: chunked<br>
	 * Content-Length: *<br>
	 */
	static void check(HTTPMessage message) throws IOException {
		if (message.getContent() == null) {
			// 未设置消息内容不做任何处理
			return;
		}
		if (message.hasHeader(ContentLength.NAME)) {
			// 用户已设置Content-Length
			return;
		}
		if (message.hasHeader(TransferEncoding.NAME)) {
			// 用户已设置Transfer-Encoding
			return;
		}

		long length = size(message);

		// 未经验证，大多数浏览器不建议同时出现 Transfer-Encoding 和 Content-Length
		if (length < 0 || length > BLOCK) {
			message.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
		} else {
			message.addHeader(ContentLength.NAME, Long.toString(length));
		}
	}

	/**
	 * 客户端编码写入请求内容<br>
	 * 客户端请求必须明确内容长度 Content-Length: *<br>
	 * 客户端请求内容不支持分块传输 Transfer-Encoding: chunked <br>
	 * 客户端请求内容不支持压缩格式 Content-Encoding: gzip<br>
	 */
	public static boolean write(HTTPWriter writer, WEBRequest request) throws IOException {
		// final ContentType contentType =
		// ContentType.parse(request.getHeader(ContentType.NAME));
		// if (contentType == null) {
		// // RFC7231 缺省为"application/octet-stream"
		// } else if
		// (MIMEType.X_WWW_FORM_URLENCODED.equalsIgnoreCase(contentType.getType()))
		// {
		// XWWWFormUrlencoded.write(writer, request);
		// return true;
		// } else if
		// (MIMEType.MULTIPART_FORMDATA.equalsIgnoreCase(contentType.getType()))
		// {
		// MultipartFormData.write(writer, request, contentType);
		// return true;
		// } else {
		// // 其它类型按原始输出
		// }
		if (request.getContent() == null) {
			return true;
		}
		return writeRaw(writer, request);
	}

	/**
	 * 服务端编码写入响应内容
	 */
	public static boolean write(HTTPWriter writer, WEBResponse response) throws IOException {
		// final ContentType contentType =
		// ContentType.parse(response.getHeader(ContentType.NAME));
		// if (contentType == null) {
		// // RFC7231 缺省为"application/octet-stream"
		// } else if
		// (MIMEType.MULTIPART_BYTERANGES.equalsIgnoreCase(contentType.getType()))
		// {
		// // 按范围输出无须再考虑分块
		// MultipartByteranges.write(writer, response, contentType);
		// return true;
		// } else {
		// // 其它类型按原始输出
		// }
		if (response.getContent() == null) {
			return true;
		}
		final String transferEncoding = response.getHeader(TransferEncoding.NAME);
		if (transferEncoding == null) {
			return writeRaw(writer, response);
		} else {
			return TransferEncoder.write(writer, response, transferEncoding);
		}
	}

	/**
	 * 服务端解码读取请求内容<br>
	 * 必须明确的 Content-Length
	 */
	public static boolean read(HTTPReader reader, WEBRequest request) throws IOException {
		final ContentLength contentLength = ContentLength.parse(request.getHeader(ContentLength.NAME));
		if (contentLength == null) {
			// 无请求实体内容
			return true;
		} else if (contentLength.getLength() <= 0) {
			// 长度无效或零
			return true;
		} else if (contentLength.getLength() <= reader.buffer().readable()) {
			// 已接收所有数据
			// 根据类型解析数据
			final ContentType contentType = ContentType.parse(request.getHeader(ContentType.NAME));
			if (contentType == null) {
				// 默认二进制数据
			} else if (MIMEType.X_WWW_FORM_URLENCODED.equalsIgnoreCase(contentType.getType())) {
				// POST 表单数据
				XWWWFormUrlencoded.read(reader, request);
				return true;
			} else if (MIMEType.MULTIPART_FORMDATA.equalsIgnoreCase(contentType.getType())) {
				// POST 多部分表单数据
				MultipartFormData.read(reader, request, contentType);
				return true;
			} else {
				// 默认二进制数据
			}
			// 默认二进制数据
			reader.buffer().bounds(contentLength.getLength());
			request.setContent(reader.buffer());
			return true;
		} else {
			// 仅接收到部分数据
			// TODO 如何分块解析降低缓存压力
			return false;
		}
	}

	/**
	 * 客户端解码读取响应内容<br>
	 * Transfer-Encoding 优先于 Content-Length
	 */
	public static boolean read(HTTPReader reader, WEBResponse response) throws IOException {
		final String transferEncoding = response.getHeader(TransferEncoding.NAME);
		if (transferEncoding == null) {
			final ContentLength contentLength = ContentLength.parse(response.getHeader(ContentLength.NAME));
			if (contentLength == null) {
				// 无法解析内容
				return true;
			} else if (contentLength.getLength() <= 0) {
				// 长度无效或零
				return true;
			} else if (contentLength.getLength() <= reader.buffer().readable()) {
				// 已接收所有数据
				// 根据类型解析数据
				final ContentType contentType = ContentType.parse(response.getHeader(ContentType.NAME));
				if (contentType == null) {
					// 默认二进制数据
				} else if (MIMEType.MULTIPART_BYTERANGES.equalsIgnoreCase(contentType.getType())) {
					MultipartByteranges.read(reader, response, contentType);
					return true;
				} else {
					// 默认二进制数据
				}
				// 默认二进制数据
				reader.buffer().bounds(contentLength.getLength());
				response.setContent(reader.buffer());
				return true;
			} else {
				// 仅接收到部分数据
				// TODO 如何分块解析降低缓存压力
				return false;
			}
		} else {
			return TransferEncoder.read(reader, response, transferEncoding);
		}
	}

	/**
	 * 解码读取原始数据
	 * 
	 * @return true 全部输出 / false 部分输出
	 */
	static void readRaw(HTTPReader reader, Message message) {
		// 降低缓存占用写入临时文件？？
	}

	/**
	 * 编码输出原始数据
	 * 
	 * @return true 全部输出 / false 部分输出
	 */
	static boolean writeRaw(HTTPWriter writer, Message message) throws IOException {
		// 多次发送的好处
		// 降低缓存占用，文件一次性全部发送可能会占用太多缓存而导致性能降低
		// 提高响应效率，避免长时间复制数据而导致网络超时
		// 避免网络拥堵，均衡网络带宽

		final InputStream input = input(message);
		int value, length = 0;
		while ((value = input.read()) >= 0) {
			writer.buffer().write(value);
			if (length++ >= BLOCK) {
				return false;
			}
		}
		return true;
	}
}