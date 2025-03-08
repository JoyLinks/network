package com.joyzl.network.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map.Entry;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.MultipartFile.MultipartFiles;
import com.joyzl.network.web.MIMEType;

/**
 * Content-Type: multipart/form-data<br>
 * Content-Type: application/x-www-form-urlencoded
 * 
 * @author ZhangXi 2024年11月20日
 */
public class FormDataCoder extends HTTPCoder {
	/*-
	 * RFC7578  Returning Values from Forms: multipart/form-data
	 * 
	 * 允许的分块头
	 * Content-Disposition 必须
	 * Content-Type 可选
	 * Content-Transfer-Encoding 可选，极少使用
	 * 
	 * BOUNDARY 开始和结束标志,分隔时[--BOUNDARY<CR/LF>],结束时[--BOUNDARY--<CRLF>] 
	 * 
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
	 * ...ignored...
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
	 * ...ignored...
	 * 
	 * 格式参考（多个文件）
	 * 
	 * POST http://www.joyzl.com/test HTTP/1.1
	 * Content-Type: multipart/form-data, boundary=AaB03x
	 * Content-Length: 1280
	 * 
	 * ...ignored...
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
	 * ...ignored...
	 * 
	 * application/x-www-form-urlencoded
	 * name=Xavier+Xantico&verdict=Yes&colour=Blue&happy=sad&Utf%F6r=Send
	 */

	/**
	 * 解析 multipart/form-data 或 application/x-www-form-urlencoded 格式数据
	 */
	public static void read(Request request) throws IOException {
		if (request.getContent() != null && request.getContent() instanceof DataBuffer) {
			final ContentType contentType = ContentType.parse(request.getHeader(ContentType.NAME));
			if (contentType != null) {
				if (Utility.same(MIMEType.MULTIPART_FORMDATA, contentType.getType())) {
					if (Utility.noEmpty(contentType.getBoundary())) {
						readFormData(request, (DataBuffer) request.getContent(), contentType.getBoundary());
					}
				} else //
				if (Utility.same(MIMEType.X_WWW_FORM_URLENCODED, contentType.getType())) {
					readXWWWForm(request, (DataBuffer) request.getContent());
				} else //
				if (Utility.same(MIMEType.X_FORM_WWW_URLENCODED, contentType.getType())) {
					readXWWWForm(request, (DataBuffer) request.getContent());
				}
			}
		}
	}

	/**
	 * 构建 multipart/form-data 或 application/x-www-form-urlencoded 格式数据
	 */
	public static void write(Request request) throws IOException {
		final ContentType contentType = ContentType.parse(request.getHeader(ContentType.NAME));
		if (contentType != null) {
			final DataBuffer buffer = DataBuffer.instance();
			if (Utility.same(MIMEType.MULTIPART_FORMDATA, contentType.getType())) {
				if (Utility.noEmpty(contentType.getBoundary())) {
					writeFormData(request, buffer, contentType.getBoundary());
				}
			} else //
			if (Utility.same(MIMEType.X_WWW_FORM_URLENCODED, contentType.getType())) {
				writeXWWWForm(request, buffer);
			} else //
			if (Utility.same(MIMEType.X_FORM_WWW_URLENCODED, contentType.getType())) {
				writeXWWWForm(request, buffer);
			}
			request.setContent(buffer);
		}
	}

	/**
	 * 解析 application/x-www-form-urlencoded 格式数据
	 */
	public static void readXWWWForm(Request request, DataBuffer buffer) throws IOException {
		// POST的键值对参数
		// 可能有空name和value
		// 转义+为空格，转义%XX为字符
		// 可能没有 [ENTER|LINE] 结束标志
		// https://htmlspecs.com/url/
		final StringBuilder builder = getStringBuilder();
		int c = 0;
		String name = null;
		while (buffer.readable() > 1) {
			c = buffer.readByte();
			if (c == EQUAL) {
				name = builder.toString();
				builder.setLength(0);
				continue;
			}
			if (c == AND) {
				request.addParameter(name, builder.toString());
				builder.setLength(0);
				continue;
			}
			if (c == '%') {
				percentDecode(buffer, builder);
				continue;
			}
			if (c == '+') {
				c = SPACE;
			} else if (c == CR) {
				c = buffer.readByte();
				if (c == LF) {
					request.addParameter(name, builder.toString());
					return;
				}
			}
			builder.append((char) c);
		}
		// 检查非[CRLF]结束
		if (name == null) {
			// ""
			// "name"
			if (builder.length() > 0) {
				request.addParameter(builder.toString(), null);
			}
		} else {
			// "="
			// "name="
			// "name=value"
			// "name=value&"
			if (c != AND) {
				request.addParameter(name, builder.toString());
			}
		}
	}

	/**
	 * 构建 application/x-www-form-urlencoded 格式数据
	 */
	public static void writeXWWWForm(Request request, DataBuffer buffer) throws IOException {
		// POST的键值对参数 没有 [ ENTER LINE ] 结束标志
		// 转义+为空格，转义%XX为字符
		if (request.hasParameters()) {
			int index, size = 0;
			String value;
			for (Entry<String, String[]> item : request.getParametersMap().entrySet()) {
				if (item.getKey() != null && item.getValue() != null) {
					for (index = 0; index < item.getValue().length; index++) {
						if (size > 0) {
							buffer.write(HTTPCoder.AND);
						}
						percentEncode(buffer, item.getKey(), true);
						buffer.writeASCII(HTTPCoder.EQUAL);
						value = item.getValue()[index];
						if (value != null && value.length() > 0) {
							percentEncode(buffer, value, true);
						}
						size++;
					}
				}
			}
		}
	}

	/**
	 * 解析 multipart/form-data 格式数据
	 */
	public static void readFormData(Request request, DataBuffer buffer, String boundary) throws IOException {
		// 检查开始边界
		// 忽略边界之前的字节
		while (buffer.readable() > boundary.length() + 4) {
			if (checkBoundary(buffer, boundary)) {
				break;
			}
		}

		String temp;
		final HTTPMessage headers = new HTTPMessage();
		final MultipartFiles files = new MultipartFiles();
		final ContentDisposition disposition = new ContentDisposition();
		final ContentType contentType = new ContentType();
		while (buffer.readable() > boundary.length() + 4) {
			if (readHeaders(buffer, headers)) {
				// Content-Disposition: form-data; name="***"; filename=""
				// content-disposition: form-data; name="_charset_"
				temp = headers.getHeader(ContentDisposition.NAME);
				if (temp == null) {
					// 缺失 Content-Disposition 头
					return;
				}
				disposition.setHeaderValue(temp);
				if (Utility.same(ContentDisposition.FORM_DATA, disposition.getDisposition())) {
					if (Utility.noEmpty(disposition.getFilename())) {
						// 文件块
						final File file = readFile(buffer, boundary);
						final MultipartFile m = new MultipartFile(file, disposition.getFilename());
						m.setContentType(headers.getHeader(ContentType.NAME));
						m.setField(disposition.getField());
						request.addParameter(disposition.getField(), disposition.getFilename());
						files.add(m);
					} else {
						// Content-Type: text/plain; charset=UTF-8
						temp = headers.getHeader(ContentType.NAME);
						if (temp == null) {
							// 文本参数块
							temp = readASCII(buffer, boundary);
							request.addParameter(disposition.getField(), temp);
						} else {
							contentType.setHeaderValue(temp);
							if (Utility.same(MIMEType.MULTIPART_MIXED, contentType.getType())) {
								// 混合数据块（此方案在 RFC7578 已被废弃）
								readFormData(request, buffer, contentType.getBoundary());
							} else {
								// 特定编码参数块
								temp = readText(buffer, contentType.getCharset(), boundary);
								request.addParameter(disposition.getField(), temp);
							}
						}
					}
				}
			}
		}
		if (files.size() > 0) {
			if (request.getContent() instanceof MultipartFiles) {
				((MultipartFiles) request.getContent()).addAll(files);
			} else {
				// 不由编解码清理前内容
				// request.clearContent();
				request.setContent(files);
			}
		}
	}

	/**
	 * 构建 multipart/form-data 格式数据
	 */
	public static void writeFormData(Request request, DataBuffer buffer, String boundary) throws IOException {
		// PARAMETERS
		int index;
		if (request.hasParameters()) {
			String value;
			for (Entry<String, String[]> item : request.getParametersMap().entrySet()) {
				if (item.getKey() != null && item.getValue() != null) {
					for (index = 0; index < item.getValue().length; index++) {
						buffer.writeASCII(MINUS);
						buffer.writeASCII(MINUS);
						buffer.writeASCIIs(boundary);
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
						// Content-Disposition: form-data; name="field1"
						buffer.writeASCIIs(ContentDisposition.NAME);
						buffer.writeASCII(COLON);
						buffer.writeASCII(SPACE);
						buffer.writeASCIIs(ContentDisposition.FORM_DATA);
						buffer.writeASCII(SEMI);
						buffer.writeASCII(SPACE);
						buffer.writeASCIIs(ContentDisposition.FIELD);
						buffer.writeASCII(EQUAL);
						buffer.writeASCII(QUOTE);
						buffer.writeASCIIs(item.getKey());
						buffer.writeASCII(QUOTE);
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
						// HEADERS END
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
						// DATA
						value = item.getValue()[index];
						if (value != null && value.length() > 0) {
							buffer.writeASCIIs(value);
						}
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
					}
				}
			}
		}
		// FILE
		if (request.getContent() != null) {
			if (request.getContent() instanceof MultipartFiles) {
				MultipartFile file;
				InputStream input;
				final MultipartFiles files = (MultipartFiles) request.getContent();
				for (index = 0; index < files.size(); index++) {
					file = files.get(index);
					if (file != null && file.getFile() != null) {
						buffer.writeASCII(MINUS);
						buffer.writeASCII(MINUS);
						buffer.writeASCIIs(boundary);
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
						// Content-Disposition:form-data;name="files";filename="test.txt"
						buffer.writeASCIIs(ContentDisposition.NAME);
						buffer.writeASCII(COLON);
						buffer.writeASCII(SPACE);
						buffer.writeASCIIs(ContentDisposition.FORM_DATA);
						buffer.writeASCII(SEMI);
						buffer.writeASCII(SPACE);
						buffer.writeASCIIs(ContentDisposition.FIELD);
						buffer.writeASCII(EQUAL);
						buffer.writeASCII(QUOTE);
						buffer.writeASCIIs(file.getField());
						buffer.writeASCII(QUOTE);
						buffer.writeASCII(SEMI);
						buffer.writeASCII(SPACE);
						buffer.writeASCIIs(ContentDisposition.FILENAME);
						buffer.writeASCII(EQUAL);
						buffer.writeASCII(QUOTE);
						buffer.writeASCIIs(file.getFilename());
						buffer.writeASCII(QUOTE);
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
						// Content-Type: text/plain
						if (file.getContentType() != null) {
							buffer.writeASCIIs(ContentType.NAME);
							buffer.writeASCII(COLON);
							buffer.writeASCII(SPACE);
							buffer.writeASCIIs(file.getContentType());
							buffer.writeASCII(CR);
							buffer.writeASCII(LF);
						}
						// HEADERS END
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
						// DATA
						input = new FileInputStream(file.getFile());
						buffer.write(input);
						input.close();
						buffer.writeASCII(CR);
						buffer.writeASCII(LF);
					}
				}
			}
		}
		// END
		buffer.writeASCII(MINUS);
		buffer.writeASCII(MINUS);
		buffer.writeASCIIs(boundary);
		buffer.writeASCII(MINUS);
		buffer.writeASCII(MINUS);
		buffer.writeASCII(CR);
		buffer.writeASCII(LF);
	}

	/**
	 * 检查缓存数据当前是否分隔符，如果成功匹配分隔符则推进读取位置，如果未能匹配分隔符则读取位置不变；<br>
	 * 注意：此方法不检查缓存可读字节数量是否足够。<br>
	 * 匹配："--BOUNDARY" "--BOUNDARY--"，假定前导CRLF已读取
	 */
	static boolean checkBoundary(DataBuffer buffer, String boundary) throws IOException {
		if (buffer.get(0) != MINUS) {
			return false;
		}
		if (buffer.get(1) != MINUS) {
			return false;
		}
		int index = 0;
		for (; index < boundary.length(); index++) {
			if (buffer.get(index + 2) != boundary.charAt(index)) {
				return false;
			}
		}
		index += 2;
		if (buffer.get(index) == CR) {
			buffer.skipBytes(index + 1);
			return true;
		}
		if (buffer.get(index) == LF) {
			buffer.skipBytes(index + 1);
			return true;
		}
		if (buffer.get(index) == MINUS) {
			if (buffer.get(index + 1) == MINUS) {
				buffer.skipBytes(index + 2);
				return true;
			}
		}
		return false;
	}

	static File readFile(DataBuffer buffer, String boundary) throws IOException {
		final File file = File.createTempFile("JOYZL_HTTP_PART", ".tmp");
		try (OutputStream output = new FileOutputStream(file)) {
			byte value;
			while (buffer.readable() > boundary.length() + 4) {
				value = buffer.readByte();
				if (value == CR) {
					value = buffer.readByte();
					if (value == LF) {
						if (checkBoundary(buffer, boundary)) {
							// 规范结尾[CRLF]
							break;
						}
					}
					output.write(CR);
				}
				if (value == LF) {
					if (checkBoundary(buffer, boundary)) {
						// 非规范结尾[LF]
						// 在cs531a5测试中有出现
						break;
					}
				}
				output.write(value);
			}
			output.flush();
		}
		return file;
	}

	/**
	 * 读取ASCII内容文本
	 */
	static String readASCII(DataBuffer buffer, String boundary) throws IOException {
		final StringBuilder builder = getStringBuilder();
		byte value;
		while (buffer.readable() > boundary.length() + 4) {
			value = buffer.readByte();
			if (value == CR) {
				value = buffer.readByte();
				if (value == LF) {
					if (checkBoundary(buffer, boundary)) {
						// 规范结尾[CRLF]
						break;
					}
				}
				builder.append(CR);
			}
			if (value == LF) {
				if (checkBoundary(buffer, boundary)) {
					// 非规范结尾[LF]
					// 在cs531a5测试中有出现
					break;
				}
			}
			builder.append((char) value);
		}
		return builder.toString();
	}

	/**
	 * 读取指定编码内容文本
	 */
	static String readText(DataBuffer buffer, String charset, String boundary) throws IOException {
		byte value;
		ByteBuffer bytes = ByteBuffer.allocate(64);
		while (buffer.readable() > boundary.length() + 4) {
			value = buffer.readByte();
			if (value == CR) {
				value = buffer.readByte();
				if (value == LF) {
					if (checkBoundary(buffer, boundary)) {
						// 规范结尾[CRLF]
						break;
					}
				}
				bytes.put((byte) CR);
			}
			if (value == LF) {
				if (checkBoundary(buffer, boundary)) {
					// 非规范结尾[LF]
					// 在cs531a5测试中有出现
					break;
				}
			}
			if (bytes.remaining() < 1) {
				// 扩展空间
				ByteBuffer temp = ByteBuffer.allocate(bytes.capacity() + bytes.capacity() / 4);
				temp.put(bytes);
				bytes = temp;
			}
			bytes.put(value);
		}
		try {
			final Charset cs = Charset.forName(charset);
			final CharBuffer chars = cs.decode(bytes.flip());
			return chars.toString();
		} catch (UnsupportedCharsetException e) {
			final CharBuffer chars = StandardCharsets.UTF_8.decode(bytes);
			return chars.toString();
		}
	}
}