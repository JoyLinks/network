/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.joyzl.network.SegmentInputStream;
import com.joyzl.network.Utility;
import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentEncoding;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentRange;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Date;
import com.joyzl.network.http.ETag;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.Range;
import com.joyzl.network.http.Range.ByteRange;
import com.joyzl.network.http.TransferEncoding;

/**
 * 文件资源请求服务
 * 
 * @author ZhangXi
 * @date 2020年8月30日
 */
public abstract class FileServlet extends WEBServlet {

	// Cache-Control: no-store
	// 缓存中不得存储任何关于客户端请求和服务端响应的内容。每次由客户端发起的请求都会下载完整的响应内容。
	// Cache-Control: no-cache
	// 每次有请求发出时，缓存会将此请求发到服务器，服务器端会验证请求中所描述的缓存是否过期，若未过期则缓存才使用本地缓存副本。
	// Cache-Control: private
	// 该响应是专用于某单个用户的，中间人不能缓存此响应，该响应只能应用于浏览器私有缓存中。
	// Cache-Control: public
	// 该响应可以被任何中间人缓存。
	// Cache-Control: max-age=31536000
	// 表示资源能够被缓存（保持新鲜）的最大时间。相对Expires而言，max-age是距离请求发起的时间的秒数。
	// Cache-Control: must-revalidate
	// 缓存在考虑使用一个陈旧的资源时，必须先验证它的状态，已过期的缓存将不被使用。

	// Pragma
	// 是HTTP/1.0标准中定义的一个header属性，请求中包含Pragma的效果跟在头信息中定义Cache-Control:no-cache相同，
	// 但是HTTP的响应头没有明确定义这个属性，所以它不能拿来完全替代HTTP/1.1中定义的Cache-control头。
	// 通常定义Pragma以向后兼容基于HTTP/1.0的客户端。

	// Expires
	// 通过比较Expires的值和头里面时间属性的值来判断是否缓存还有效。

	// Last-Modified
	// 源头服务器认定的资源做出修改的日期及时间。 它通常被用作一个验证器来判断接收到的或者存储的资源是否彼此一致。
	// 如果max-age和expires属性都没有才会使用Last-Modified，缓存的寿命就等于头里面Date的值减去Last-Modified的值除以10(根据rfc2626其实也就是乘以10%)。
	// 客户端可以在后续的请求中带上If-Modified-Since来验证缓存。

	// ETags
	// 如果资源请求的响应头里含有ETag, 客户端可以在后续的请求的头中带上If-None-Match头来验证缓存。
	// 当向服务端发起缓存校验的请求时，服务端会返回200(OK)表示返回正常的结果或者304(NotModified不返回body)表示浏览器可以使用本地缓存文件。
	// 304的响应头也可以同时更新缓存文档的过期时间。

	// If-Match/If-None-Match
	// 请求首部If-Match的使用表示这是一个条件请求。在请求方法为GET和HEAD的情况下，服务器仅在请求的资源满足此首部列出的ETag值时才会返回资源。
	// 而对于PUT或其他非安全方法来说，只有在满足条件的情况下才可以将资源上传。
	// If-Unmodified-Since/If-Modified-Since
	// 浏览器检查该资源副本是否是依然还是算新鲜的，若服务器返回了304(Not Modified 该响应不会有带有实体信息)，则表示此资源副本是新鲜的。
	// 若服务器判断后发现已过期，那么会带有该资源的实体内容返回。

	// Vary
	// 使用vary头有利于内容服务的动态多样性。

	// Accept-Ranges:bytes
	// 服务器使用响应头Accept-Ranges标识自身支持范围请求。
	// Content-Range:
	// 响应首部Content-Range显示的是一个数据片段在整个文件中的位置。
	// If-Range
	// 请求头字段用来使得Range头字段在一定条件下起作用：
	// 当字段值中的条件得到满足时，Range头字段才会起作用，同时服务器回复206部分内容状态码，以及Range头字段请求的相应部分；
	// 如果字段值中的条件没有得到满足，服务器将会返回200(OK)状态码，并返回完整的请求资源。
	// 字段值中既可以用Last-Modified时间值用作验证，也可以用ETag标记作为验证，但不能将两者同时使用。
	// Range
	// 告知服务器返回文件的哪一部分。可以一次性请求多个部分，服务器会以multipart文件的形式将其返回。
	// 如果服务器返回的是范围响应，需要使用206(Partial Content)状态码。
	// 假如所请求的范围不合法，那么服务器会返回416(Range Not Satisfiable)状态码，表示客户端错误。
	// 服务器允许忽略Range首部，从而返回整个文件，状态码用200。

	// Accept-Encoding
	// Accept-Encoding: deflate, gzip;q=1.0, *;q=0.5
	// Content-Encoding

	final static String LAST_MODIFIED = "Last-Modified";
	final static String ACCEPT_RANGES = "Accept-Ranges";
	final static String CONTENT_LOCATION = "Content-Location";

	final static String IF_MATCH = "If-Match";
	final static String IF_NONE_MATCH = "If-None-Match";
	final static String IF_MODIFIED_SINCE = "If-Modified-Since";
	final static String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
	final static String IF_RANGE = "If-Range";

	@Override
	protected void head(WEBRequest request, WEBResponse response) throws Exception {
		todo(request, response, false);
	}

	@Override
	protected void get(WEBRequest request, WEBResponse response) throws Exception {
		todo(request, response, true);
	}

	private final void todo(WEBRequest request, WEBResponse response, boolean content) throws Exception {
		final File file = find(request.getURI());
		if (file == null) {
			// 文件未找到
			response.setStatus(HTTPStatus.NOT_FOUND);
			return;
		}

		if (file.exists() && file.isFile()) {
			final long last_modified = file.lastModified() / 1000;
			final String etag = ETag.makeWTag(file);

			// 公共头部分
			// Cache-Control、Content-Location、Date、ETag、Expires 和 Vary
			response.addHeader(CONTENT_LOCATION, request.getURI());
			response.addHeader(CacheControl.NAME, CacheControl.NO_CACHE);
			response.addHeader(LAST_MODIFIED, DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.of(LocalDateTime.ofEpochSecond(last_modified, 0, ZoneOffset.UTC), Date.GMT)));
			response.addHeader(ACCEPT_RANGES, Range.UNIT);
			response.addHeader(ETag.NAME, etag);

			// ETAG不同则返回资源 RFC7232
			String code = request.getHeader(IF_NONE_MATCH);
			if (Utility.noEmpty(code)) {
				if (Utility.equals(code, etag, false)) {
					response.setStatus(HTTPStatus.NOT_MODIFIED);
				} else {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, file, content);
				}
				return;
			}
			// ETAG相同则返回资源 RFC7232
			code = request.getHeader(IF_MATCH);
			if (Utility.noEmpty(code)) {
				if (Utility.equals(code, etag, false)) {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, file, content);
				} else {
					response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				}
				return;
			}

			// 修改时间有更新返回文件内容
			code = request.getHeader(IF_MODIFIED_SINCE);
			if (Utility.noEmpty(code)) {
				final ZonedDateTime d = ZonedDateTime.parse(code, DateTimeFormatter.RFC_1123_DATE_TIME);
				if (d.toEpochSecond() < last_modified) {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, file, content);
				} else {
					response.setStatus(HTTPStatus.NOT_MODIFIED);
				}
				return;
			}
			// 修改时间未更新返回文件内容
			code = request.getHeader(IF_UNMODIFIED_SINCE);
			if (Utility.noEmpty(code)) {
				final ZonedDateTime d = ZonedDateTime.parse(code, DateTimeFormatter.RFC_1123_DATE_TIME);
				if (d.toEpochSecond() == last_modified) {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, file, content);
				} else {
					response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				}
				return;
			}

			// RANGE部分请求
			final Range range = Range.parse(request.getHeader(Range.NAME));
			if (range != null) {
				code = request.getHeader(IF_RANGE);
				if (Utility.noEmpty(code)) {
					// Last-Modified/ETag相同时Range生效
					if (Utility.equals(code, etag, false)) {
						response.setStatus(HTTPStatus.PARTIAL_CONTENT);
						parts(range, response, file, content);
					} else {
						try {
							final ZonedDateTime d = ZonedDateTime.parse(code, DateTimeFormatter.RFC_1123_DATE_TIME);
							if (d.toEpochSecond() == last_modified) {
								response.setStatus(HTTPStatus.PARTIAL_CONTENT);
								parts(range, response, file, content);
							} else {
								response.setStatus(HTTPStatus.OK);
								whole(request, response, file, content);
							}
						} catch (Exception e) {
							response.setStatus(HTTPStatus.OK);
							whole(request, response, file, content);
						}
					}
				} else {
					response.setStatus(HTTPStatus.PARTIAL_CONTENT);
					parts(range, response, file, content);
				}
				return;
			}

			response.setStatus(HTTPStatus.OK);
			whole(request, response, file, content);
		} else {
			response.setStatus(HTTPStatus.NOT_FOUND);
		}
	}

	/**
	 * 响应全部内容
	 */
	private final void whole(WEBRequest request, WEBResponse response, File file, boolean content) throws IOException {
		// Content-Type: text/html
		response.addHeader(new ContentType(MIMEType.getMIMEType(file)));

		long length = file.length();
		final AcceptEncoding acceptEncoding = AcceptEncoding.parse(request.getHeader(AcceptEncoding.NAME));
		if (acceptEncoding != null) {
			if (canCompress(file)) {
				if (AcceptEncoding.BR.equalsIgnoreCase(acceptEncoding.getValue())) {
					final File f = br(file);
					if (f != null) {
						// Content-Encoding: br
						response.addHeader(ContentEncoding.NAME, ContentEncoding.BR);
						length = f.length();
						file = f;
					}
				} else if (AcceptEncoding.GZIP.equalsIgnoreCase(acceptEncoding.getValue())) {
					final File f = gzip(file);
					if (f != null) {
						// Content-Encoding: gzip
						response.addHeader(ContentEncoding.NAME, ContentEncoding.GZIP);
						length = f.length();
						file = f;
					}
				} else if (AcceptEncoding.DEFLATE.equalsIgnoreCase(acceptEncoding.getValue())) {
					final File f = deflate(file);
					if (f != null) {
						// Content-Encoding: deflate
						response.addHeader(ContentEncoding.NAME, ContentEncoding.DEFLATE);
						length = f.length();
						file = f;
					}
				}
			}
		}
		if (length < WEBContentCoder.BLOCK) {
			response.addHeader(ContentLength.NAME, Long.toString(length));
		} else {
			response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
		}
		if (content) {
			response.setContent(file);
		}
	}

	/**
	 * 响应部分内容
	 */
	private final void parts(Range range, WEBResponse response, File file, boolean content) throws Exception {
		long length = file.length();
		// 单块请求
		if (range.getRanges().size() == 1) {
			ByteRange byterange = range.getRanges().get(0);
			if (byterange.valid(length, WEBContentCoder.BLOCK, WEBContentCoder.MAX)) {
				response.addHeader(new ContentType(MIMEType.getMIMEType(file)));
				response.addHeader(new ContentRange(byterange.getStart(), byterange.getEnd(), length));
				response.addHeader(ContentLength.NAME, Long.toString(byterange.getSize()));
				if (content) {
					response.setContent(new SegmentInputStream(file, byterange.getStart(), byterange.getSize()));
				}
			} else {
				response.setStatus(HTTPStatus.RANGE_NOT_SATISFIABLE);
			}
		} else // 多块请求
		if (range.getRanges().size() > 1) {
			final String contentType = MIMEType.getMIMEType(file);
			final List<Part> parts = new ArrayList<>(range.getRanges().size());
			for (int index = 0; index < range.getRanges().size(); index++) {
				ByteRange byterange = range.getRanges().get(index);
				if (byterange.valid(length, WEBContentCoder.BLOCK, WEBContentCoder.MAX)) {
					final Part part = new Part();
					part.addHeader(ContentType.NAME, contentType);
					part.addHeader(new ContentRange(byterange.getStart(), byterange.getEnd(), length));
					if (content) {
						part.setContent(new SegmentInputStream(file, byterange.getStart(), byterange.getSize()));
					}
					parts.add(part);
				} else {
					Message.close(parts);
					response.setStatus(HTTPStatus.RANGE_NOT_SATISFIABLE);
					return;
				}
			}
			response.addHeader(new ContentType(MIMEType.MULTIPART_BYTERANGES));
			response.setContent(parts);
		} else {
			response.setStatus(HTTPStatus.RANGE_NOT_SATISFIABLE);
		}
	}

	protected abstract boolean canCompress(File file);

	protected abstract File find(String uri);

	protected abstract File br(File source) throws IOException;

	protected abstract File gzip(File source) throws IOException;

	protected abstract File deflate(File source) throws IOException;

}
