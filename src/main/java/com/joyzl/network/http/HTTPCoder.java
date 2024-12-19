/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferUnit;
import com.joyzl.network.http.MultipartRange.MultipartRanges;

/**
 * HTTP(Hyper Text Transfer Protocol)
 * <p>
 * RFC3986 URL编码 ASCII+UNICODE<br>
 * RFC2616 RFC7230 超文本传输​​协议HTTP/1.1<br>
 * RFC7540 超文本传输​​协议HTTP/2<br>
 * https://htmlspecs.com/
 * </p>
 * 
 * @author ZhangXi
 * @date 2021年10月7日
 */
public class HTTPCoder extends HTTP {

	public final static char CR = '\r';
	public final static char LF = '\n';
	public final static char SPACE = ' ';
	public final static char COLON = ':';
	public final static char COMMA = ',';
	public final static char SEMI = ';';
	public final static char EQUAL = '=';
	public final static char QUOTE = '"';
	public final static char QUEST = '?';
	public final static char AND = '&';
	public final static char MINUS = '-';
	public final static char SLASH = '/';
	public final static char NUM = '#';
	public final static String CRLF = "\r\n";

	/**
	 * 默认分块投递字节数量64K<br>
	 * 1Mbps带宽每秒理论可投递约2块(1Mbps = 1,000,000/8 = 125,000Byte/s)
	 */
	public final static int BLOCK_BYTES = DataBufferUnit.BYTES * 64;

	/**
	 * 请求帧结构 REQUEST
	 * 
	 * <pre>
	 * +--------+-------+-----+-------+---------+-------+------+
	 * | METHOD | SPACE | URL | SPACE | VERSION | ENTER | LINE |
	 * +--------+-------+-----+-------+---------+-------+------+
	 * +------+---+-------+-------+-------+------+
	 * | NAME | : | SPACE | VALUE | ENTER | LINE |
	 * +------+---+-------+-------+-------+------+
	 * +------+
	 * | .... |
	 * +------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * +---------+
	 * | CONTENT |
	 * +---------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * </pre>
	 * 
	 */
	/**
	 * 响应帧结构 RESPONSE
	 * 
	 * <pre>
	 * +---------+-------+--------+-------+-------------+-------+------+
	 * | VERSION | SPACE | STATUS | SPACE | STATUS-TEXT | ENTER | LINE |
	 * +---------+-------+--------+-------+-------------+-------+------+
	 * +------+---+-------+-------+------+
	 * | NAME | : | VALUE | ENTER | LINE |
	 * +------+---+-------+-------+------+
	 * +------+
	 * | .... |
	 * +------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * +------+
	 * | BODY |
	 * +------+
	 * +-------+------+
	 * | ENTER | LINE |
	 * +-------+------+
	 * </pre>
	 */

	/**
	 * 读取请求首行，采用 ifString 模式减少 new String()
	 * <p>
	 * 采用 ifString 模式应事先常量化字符串，并组成顺序排序数组，递进判断匹配常量字符串；如果匹配失败则 new String() 。
	 * </p>
	 * 
	 * @param buffer
	 * @param request
	 * @return true 成功结束(CRLF) / false 未能解析到结束标识(CRLF)
	 * @throws IOException
	 */
	public static boolean readCommand(DataBuffer buffer, Request request) throws IOException {
		final StringBuilder builder = Utility.getStringBuilder();
		int a = 0, b = 0, c;
		buffer.mark();

		// METHOD
		builder.setLength(0);
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if (c == SPACE) {
				request.setMethod(METHODS.get(builder));
				break;
			}
			if (c == CR) {
				c = buffer.readByte();
				if (c == LF) {
					request.setMethod(METHODS.get(builder));
					return true;
				}
			}
			builder.append((char) c);
		}

		// URL/URI
		// SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
		// GET /background.png HTTP/1.0
		// GET http://www.joyzl.org/docs/Web/HTTP/Messages HTTP/1.1
		// CONNECT developer.mozilla.org:80 HTTP/1.1
		// OPTIONS * HTTP/1.1

		a = b = 0;
		builder.setLength(0);
		request.setQuery(0);
		request.setAnchor(0);
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if (c == SPACE) {
				request.setUrl(builder.toString());
				break;
			}
			if (c == CR) {
				c = buffer.readByte();
				if (c == LF) {
					request.setUrl(builder.toString());
					return true;
				}
			}
			if (c == COLON) {
				if (a == 0 && b == 0) {
					request.setPort(builder.length() + 1);
					request.setHost(0);
				}
				if (a == 1 && b == 2) {
					request.setPort(builder.length() + 1);
				}
				a++;
			} else if (c == SLASH) {
				if (a == 1 && b == 1) {
					request.setHost(builder.length() + 1);
					request.setPort(0);
				}
				if (a == 0 && b == 0 || a > 0 && b == 2) {
					request.setPath(builder.length());
				}
				b++;
			} else if (c == QUEST) {
				request.setQuery(builder.length());
			} else if (c == NUM) {
				request.setAnchor(builder.length());
			} else if (c == '%') {
				// 百分号编码字节
				percentDecode(buffer, builder);
				continue;
			}
			builder.append((char) c);
		}

		// VERSION
		builder.setLength(0);
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if (c == SPACE) {
				request.setVersion(VERSIONS.get(builder));
				break;
			}
			if (c == CR) {
				c = buffer.readByte();
				if (c == LF) {
					request.setVersion(VERSIONS.get(builder));
					return true;
				}
			}
			builder.append((char) c);
		}

		buffer.reset();
		return false;
	}

	/**
	 * 读取响应首行
	 * 
	 * @param buffer
	 * @param response
	 * @return true 成功结束(CRLF) / false 未能解析到结束标识(CRLF)
	 * @throws IOException
	 */
	public static boolean readCommand(DataBuffer buffer, Response response) throws IOException {
		final StringBuilder builder = Utility.getStringBuilder();
		int c;
		buffer.mark();

		// VERSION
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if (c == SPACE) {
				response.setVersion(VERSIONS.get(builder));
				break;
			}
			if (c == CR) {
				c = buffer.readByte();
				if (c == LF) {
					response.setVersion(VERSIONS.get(builder));
					return true;
				}
			}
			builder.append((char) c);
		}

		// STATUS
		builder.setLength(0);
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if (c == SPACE) {
				response.setStatus(Integer.parseUnsignedInt(builder, 0, builder.length(), 10));
				break;
			}
			if (c == CR) {
				c = buffer.readByte();
				if (c == LF) {
					response.setStatus(Integer.parseUnsignedInt(builder, 0, builder.length(), 10));
					return true;
				}
			}
			builder.append((char) c);
		}

		// TEXTS
		builder.setLength(0);
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if (c == CR) {
				c = buffer.readByte();
				if (c == LF) {
					response.setText(builder.toString());
					return true;
				}
			}
			builder.append((char) c);
		}

		buffer.reset();
		return false;
	}

	/**
	 * 读取请求或响应标头
	 * 
	 * @param buffer
	 * @param message
	 * @return true 成功结束(CRLF) / false 未能解析到结束标识(CRLF)
	 * @throws IOException
	 */
	public static boolean readHeaders(DataBuffer buffer, HTTPMessage message) throws IOException {
		final StringBuilder builder = Utility.getStringBuilder();
		int c;
		buffer.mark();

		String name = null;
		while (buffer.readable() > 1) {
			// NAME
			builder.setLength(0);
			do {
				c = buffer.readByte();
				if (c == CR) {
					c = buffer.readByte();
					if (c == LF) {
						// 符合规范的CRLF结尾
						return true;
					}
				}
				if (c == LF) {
					// 不合规的LF结尾
					// 在cs531a5测试中有出现
					return true;
				}
				if (c == COLON) {
					name = HEADERS.get(builder);
					break;
				}
				builder.append((char) c);
			} while (buffer.readable() > 1);

			// VALUE
			builder.setLength(0);
			while (buffer.readable() > 1) {
				c = buffer.readByte();
				if (c == CR) {
					c = buffer.readByte();
					if (c == LF) {
						// 符合规范的CRLF结尾
						message.getHeaders().put(name, builder.toString());
						buffer.mark();
						break;
					}
				}
				if (c == LF) {
					// 不合规的LF结尾
					// 在cs531a5测试中有出现
					message.getHeaders().put(name, builder.toString());
					buffer.mark();
					break;
				}
				if (Character.isWhitespace(c)) {
					if (builder.length() == 0) {
						continue;
					}
				}
				builder.append((char) c);
			}
		}

		buffer.reset();
		return false;
	}

	/**
	 * 读取请求内容；不会对请求内容执行任何解析，应由业务处理层执行进一步解析；<br>
	 * 请求内容如果未能全部接收，解析不完整的请求内容没有实际意义。
	 * 
	 * <pre>
	 * Content-Type: multipart/form-data
	 * Content-Type: multipart/mixed
	 * Content-Type: application/x-www-form-urlencoded
	 * Content-Type: ***
	 * </pre>
	 * 
	 * @param buffer
	 * @param request
	 * @return true 读取所有 Content-Length 长度内容 / false 数据不足够 Content-Length
	 * @throws IOException
	 */
	public static boolean readContent(DataBuffer buffer, Request request) throws IOException {
		String value = request.getHeader(ContentLength.NAME);
		if (value == null || value.length() == 0) {
			return true;
		}
		final int length = Integer.parseUnsignedInt(value);
		if (length == 0) {
			return true;
		}
		if (buffer.readable() >= length) {
			// 数据足时全部转移为内容
			final DataBuffer content = DataBuffer.instance();
			buffer.transfer(content, length);
			request.setContent(content);
			return true;
		}
		return false;
	}

	/**
	 * 读取响应内容；
	 * 
	 * <pre>
	 * Content-Length: n
	 * Transfer-Encoding: chunked
	 * Content-Type: multipart/byteranges; boundary=xxxx
	 * </pre>
	 * 
	 * @param buffer
	 * @param response
	 * @return true 响应内容读取完成 / false 响应内容未读取完成
	 * @throws IOException
	 */
	public static boolean readContent(DataBuffer buffer, Response response) throws IOException {
		if (response.isChunked()) {
			return readContentChunked(buffer, response);
		} else {
			return readContentIdentity(buffer, response);
		}
	}

	/**
	 * 必须 Content-Length: n<br>
	 * 可选 Transfer-Encoding: identity
	 */
	public static boolean readContentIdentity(DataBuffer buffer, Response message) {
		String value = message.getHeader(ContentLength.NAME);
		if (value == null || value.length() == 0) {
			return true;
		}
		final int length = Integer.parseUnsignedInt(value);
		if (length == 0) {
			return true;
		}
		if (buffer.readable() >= length) {
			// 数据足时全部转移为内容
			final DataBuffer content = DataBuffer.instance();
			buffer.transfer(content, length);
			message.setContent(content);
			return true;
		}
		return false;
	}

	/**
	 * 必须 Transfer-Encoding: chunked
	 */
	public static boolean readContentChunked(DataBuffer buffer, Message message) throws IOException {
		if (buffer.readable() > 2) {
			buffer.mark();
			int c, length = 0;
			// LENGTH
			do {
				c = buffer.readByte();
				if (c == CR) {
					c = buffer.readByte();
					if (c == LF) {
						break;
					}
				}
				length = length * 16 + Character.digit(c, 16);
			} while (buffer.readable() > 0);
			// CHUNK
			if (length > 0) {
				if (buffer.readable() >= length + 2) {
					final DataBuffer content;
					if (message.getContent() == null) {
						content = DataBuffer.instance();
					} else {
						content = (DataBuffer) message.getContent();
					}
					buffer.transfer(content, length);
					message.setContent(content);
					// 忽略并丢弃[CRLF]
					buffer.skipBytes(2);
				} else {
					buffer.reset();
				}
			} else {
				if (buffer.readable() > 1) {
					// 忽略并丢弃[CRLF]
					buffer.skipBytes(2);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 必须 Content-Type: multipart/byteranges; boundary=xxxx
	 */
	public static boolean readContentMultipart(DataBuffer buffer, Message message) throws IOException {
		// TODO read byteranges
		return false;
	}

	/**
	 * 输出请求首行
	 * 
	 * @param buffer
	 * @param request
	 * @return 始终返回 true
	 * @throws IOException
	 */
	public static boolean writeCommand(DataBuffer buffer, Request request) throws IOException {
		buffer.writeASCIIs(request.getMethod());
		buffer.writeASCII(SPACE);
		buffer.writeASCIIs(request.getURL());
		buffer.writeASCII(SPACE);
		buffer.writeASCIIs(request.getVersion());
		buffer.writeASCII(CR);
		buffer.writeASCII(LF);
		return true;
	}

	/**
	 * 输出响应首行
	 * 
	 * @param buffer
	 * @param response
	 * @return 始终返回 true
	 * @throws IOException
	 */
	public static boolean writeCommand(DataBuffer buffer, Response response) throws IOException {
		buffer.writeASCIIs(response.getVersion());
		buffer.writeASCII(SPACE);
		buffer.writeASCIIs(Integer.toUnsignedString(response.getStatus()));
		buffer.writeASCII(SPACE);
		buffer.writeASCIIs(response.getText());
		buffer.writeASCII(CR);
		buffer.writeASCII(LF);
		return true;
	}

	/**
	 * 输出请求标头，忽略空值
	 * 
	 * @param buffer
	 * @param message
	 * @return 始终返回 true
	 * @throws IOException
	 */
	public static boolean writeHeaders(DataBuffer buffer, Request message) throws IOException {
		for (Entry<String, String> header : message.getHeaders().entrySet()) {
			if (header.getKey() != null && header.getValue() != null) {
				buffer.writeASCIIs(header.getKey());
				buffer.writeASCII(COLON);
				buffer.writeASCII(SPACE);
				buffer.writeASCIIs(header.getValue());
				buffer.writeASCII(CR);
				buffer.writeASCII(LF);
			}
		}
		buffer.writeASCII(CR);
		buffer.writeASCII(LF);
		return true;
	}

	/**
	 * 输出响应标头，忽略空值
	 * 
	 * @param buffer
	 * @param message
	 * @return 始终返回 true
	 * @throws IOException
	 */
	public static boolean writeHeaders(DataBuffer buffer, Response message) throws IOException {
		for (Entry<String, String> header : message.getHeaders().entrySet()) {
			if (header.getKey() != null && header.getValue() != null) {
				buffer.writeASCIIs(header.getKey());
				buffer.writeASCII(COLON);
				buffer.writeASCII(SPACE);
				buffer.writeASCIIs(header.getValue());
				buffer.writeASCII(CR);
				buffer.writeASCII(LF);
			}
		}
		// 这是特殊处理，输出服务端配置的附加信息头
		if (message.getAttachHeaders() != null) {
			for (Entry<String, String> header : message.getAttachHeaders().entrySet()) {
				if (header.getKey() != null && header.getValue() != null) {
					buffer.writeASCIIs(header.getKey());
					buffer.writeASCII(COLON);
					buffer.writeASCII(SPACE);
					buffer.writeASCIIs(header.getValue());
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
				}
			}
		}
		buffer.writeASCII(CR);
		buffer.writeASCII(LF);
		return true;
	}

	/**
	 * 输出请求内容
	 * 
	 * @param buffer
	 * @param request
	 * @return true 内容已全部输出 / false 内部部分输出
	 * @throws IOException
	 */
	public static boolean writeContent(DataBuffer buffer, Request request) throws IOException {
		if (request.getContent() == null) {
			// HEAD
			return true;
		}
		if (buffer.readable() >= BLOCK_BYTES) {
			return false;
		}
		return writeContentIdentity(buffer, request);
	}

	/**
	 * 输出响应内容
	 * 
	 * @param buffer
	 * @param response
	 * @return true 内容已全部输出 / false 内部部分输出
	 * @throws IOException
	 */
	public static boolean writeContent(DataBuffer buffer, Response response) throws IOException {
		if (response.getContent() == null) {
			// HEAD
			return true;
		}
		if (buffer.readable() >= BLOCK_BYTES) {
			return false;
		}
		if (response.isChunked()) {
			return writeContentChunked(buffer, response);
		} else {
			if (response.getContent() instanceof MultipartRanges) {
				return writeContentMultipart(buffer, response);
			} else {
				return writeContentIdentity(buffer, response);
			}
		}
	}

	/**
	 * 必须 Content-Length: n<br>
	 * 可选 Transfer-Encoding: identity
	 */
	public static boolean writeContentIdentity(DataBuffer buffer, Message message) throws IOException {
		// 当前最多可发送字节数
		final int max = BLOCK_BYTES - buffer.readable();
		if (message.getContent() instanceof DataBuffer) {
			final DataBuffer content = (DataBuffer) message.getContent();
			if (content.readable() > 0) {
				if (content.readable() > max) {
					content.transfer(buffer, max);
					return false;
				} else {
					content.transfer(buffer);
				}
			}
		} else //
		if (message.getContent() instanceof InputStream) {
			final InputStream content = (InputStream) message.getContent();
			int length = buffer.write(content, max);
			if (length == max) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 必须 Transfer-Encoding: chunked
	 */
	public static boolean writeContentChunked(DataBuffer buffer, Message message) throws IOException {
		// 分块的形式进行发送时无Content-Length值
		// 分块长度以十六进制的形式表示，后面紧跟着 '\r\n'
		// 之后是分块本身，后面也是'\r\n'
		// 终止块是一个常规的分块，不同之处在于其长度为0

		// 当前最多可发送字节数
		final int max = BLOCK_BYTES - buffer.readable();

		if (message.getContent() instanceof DataBuffer) {
			final DataBuffer content = (DataBuffer) message.getContent();
			if (content.readable() > 0) {
				if (content.readable() > max) {
					// 块长度
					buffer.writeASCIIs(Integer.toHexString(max));
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
					// 数据
					content.transfer(buffer, max);
					// 块结束
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
					return false;
				} else {
					// 块长度
					buffer.writeASCIIs(Integer.toHexString(content.readable()));
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
					// 数据
					content.transfer(buffer);
					// 块结束
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
				}
			}
		} else //
		if (message.getContent() instanceof InputStream) {
			final InputStream content = (InputStream) message.getContent();
			int length = content.available();
			if (length > 0) {
				if (length > max) {
					// 块长度
					buffer.writeASCIIs(Integer.toHexString(max));
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
					// 数据
					buffer.write(content, max);
					// 块结束
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
				} else {
					// 块长度
					buffer.writeASCIIs(Integer.toHexString(length));
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
					// 数据
					buffer.write(content, length);
					// 块结束
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
				}
				// 流必须确定无数据才能结束
				// available == 0
				return false;
			}
		}

		// 全结束
		buffer.writeASCII('0');
		buffer.writeASCII(CR);
		buffer.writeASCII(LF);
		buffer.writeASCII(CR);
		buffer.writeASCII(LF);
		return true;
	}

	/**
	 * 必须 Content-Type: multipart/byteranges; boundary=something
	 */
	public static boolean writeContentMultipart(DataBuffer buffer, Message message) throws IOException {
		// 当前最多可发送字节数
		int max = BLOCK_BYTES - buffer.readable();

		// PARTS

		if (message.getContent() instanceof MultipartRanges) {
			MultipartRange part;
			final MultipartRanges multiparts = (MultipartRanges) message.getContent();
			while (multiparts.hasNext()) {
				if (max <= 0) {
					// 未进行严格的分块数量控制
					return false;
				}

				part = multiparts.next();
				// BOUNDARY
				buffer.writeASCII(MINUS);
				buffer.writeASCII(MINUS);
				buffer.writeASCIIs(multiparts.getBoundary());
				buffer.writeASCII(CR);
				buffer.writeASCII(LF);
				// HEADERS
				if (part.getContentType() != null) {
					buffer.writeASCIIs(ContentType.NAME);
					buffer.writeASCII(COLON);
					buffer.writeASCII(SPACE);
					buffer.writeASCIIs(part.getContentType());
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
				}
				if (part.getContentEncoding() != null) {
					buffer.writeASCIIs(ContentEncoding.NAME);
					buffer.writeASCII(COLON);
					buffer.writeASCII(SPACE);
					buffer.writeASCIIs(part.getContentEncoding());
					buffer.writeASCII(CR);
					buffer.writeASCII(LF);
				}
				buffer.writeASCIIs(ContentRange.NAME);
				buffer.writeASCII(COLON);
				buffer.writeASCII(SPACE);
				buffer.writeASCIIs(part.getContentRange());
				buffer.writeASCII(CR);
				buffer.writeASCII(LF);
				buffer.writeASCII(CR);
				buffer.writeASCII(LF);

				// CONTENT
				max -= buffer.write(part.getContent());
				buffer.writeASCII(CR);
				buffer.writeASCII(LF);
			}

			// END
			buffer.writeASCII(MINUS);
			buffer.writeASCII(MINUS);
			buffer.writeASCIIs(multiparts.getBoundary());
			buffer.writeASCII(MINUS);
			buffer.writeASCII(MINUS);
			buffer.writeASCII(CR);
			buffer.writeASCII(LF);
			return true;
		} else {
			throw new IllegalArgumentException("响应内容不是有效的Part集合");
		}
	}

	/**
	 * 百分号解码字节，假定第一个'%'已被读取识别；<br>
	 * 此方法仅处理百分号编码，不处理 application/x-www-form-urlencoded 中的'+'转换为空格字符；<br>
	 * URL中空格转换为%20，FORM中空格转换为'+'。
	 */
	public static void percentDecode(DataBuffer buffer, StringBuilder builder) {
		// %XX%XX
		// 如果%后两位不是有效16进制字符则视为非百分号编码
		// 百分号编码是个很糟糕的设计
		ByteBuffer bytes = null;
		int h = -1, l = -1, c = 0;
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if ((h = Character.digit(c, 16)) < 0) {
				builder.append('%');
				builder.append((char) c);
				break;
			}
			c = buffer.readByte();
			if ((l = Character.digit(c, 16)) < 0) {
				builder.append('%');
				builder.append(Character.forDigit(h, 16));
				builder.append((char) c);
				break;
			}
			c = h * 16 + l;
			if (buffer.readable() > 0) {
				if (buffer.get(0) == '%') {
					// 可能连续
					if (bytes == null) {
						bytes = ByteBuffer.allocate(64);
					} else if (bytes.remaining() < 1) {
						final ByteBuffer temp = ByteBuffer.allocate(bytes.capacity() + bytes.capacity() / 4);
						temp.put(bytes);
						bytes = temp;
					}
					bytes.put((byte) c);
					// 继续解码
					buffer.readByte();
					continue;
				}
				if (bytes != null) {
					bytes.put((byte) c);
				} else {
					builder.append((char) c);
				}
				break;
			} else {
				if (bytes != null) {
					bytes.put((byte) c);
				} else {
					builder.append((char) c);
				}
				break;
			}
		}
		if (bytes != null) {
			final CharBuffer chars = StandardCharsets.UTF_8.decode(bytes.flip());
			builder.append(chars);
		}
	}

	/**
	 * 百分号编码字节<br>
	 * 保留字符：ASCII 'a'~'z' 'A'~'Z' '0'~'9' '-' '_' '*' '.'；<br>
	 * 转义字符：' '='+'
	 * 
	 * @param plus 空格转'+'时设置为 true
	 */
	public static void percentEncode(DataBuffer buffer, CharSequence chars, boolean plus) throws IOException {
		char c, e;
		CharBuffer b = null;
		for (int index = 0; index < chars.length(); index++) {
			c = chars.charAt(index);
			if (c >= 'a' && c <= 'z') {
				buffer.writeASCII(c);
				continue;
			} else if (c >= 'A' && c <= 'Z') {
				buffer.writeASCII(c);
				continue;
			} else if (c >= '0' && c <= '9') {
				buffer.writeASCII(c);
				continue;
			} else if (c == '-' || c == '_' || c == '*' || c == '.') {
				buffer.writeASCII(c);
				continue;
			} else if (plus && c == ' ') {
				buffer.writeASCII('+');
				continue;
			}

			if (b == null) {
				b = CharBuffer.allocate(2);
			} else {
				b.clear();
			}
			b.put(c);
			if (c >= 0xD800 && c <= 0xDBFF) {
				if ((index + 1) < chars.length()) {
					e = chars.charAt(index + 1);
					if (e >= 0xDC00 && e <= 0xDFFF) {
						b.put(e);
						index++;
					}
				}
			}
			byte value;
			final ByteBuffer bytes = StandardCharsets.UTF_8.encode(b.flip());
			while (bytes.hasRemaining()) {
				buffer.writeASCII('%');
				value = bytes.get();
				c = Character.forDigit((value >> 4) & 0xF, 16);
				if (Character.isLetter(c)) {
					c -= 32;
				}
				buffer.writeASCII(c);
				c = Character.forDigit(value & 0xF, 16);
				if (Character.isLetter(c)) {
					c -= 32;
				}
				buffer.writeASCII(c);
			}
		}
	}

	/**
	 * TEST
	 */
	public static String toString(DataBuffer buffer) {
		final StringBuilder builder = Utility.getStringBuilder();
		buffer.mark();
		int c;
		while (buffer.readable() > 0) {
			c = buffer.readByte();
			if (c == CR) {
				builder.append("CR");
				continue;
			} else if (c == LF) {
				builder.append("LF");
			}
			builder.append((char) c);
		}
		buffer.reset();
		return builder.toString();
	}
}