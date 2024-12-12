/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.IOException;

import com.joyzl.network.Utility;
import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.ContentEncoding;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentRange;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.ETag;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.MultipartRange;
import com.joyzl.network.http.MultipartRange.MultipartRanges;
import com.joyzl.network.http.Range;
import com.joyzl.network.http.Range.ByteRange;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.TransferEncoding;

/**
 * 资源请求服务
 * 
 * @author ZhangXi
 * @date 2020年8月30日
 */
public abstract class WEBResourceServlet extends WEBServlet {

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

	// Location
	// 如果请求资源需要重定向，例如请求目录 /eno 将其重定向为 /eno/

	// 建议分块大小
	private int BLOCK_BYTES = HTTPCoder.BLOCK_BYTES;
	// 最大请求大小
	private int MAX_BYTES = HTTPCoder.BLOCK_BYTES * 1024;
	// 最多请求分块
	private int PART_MAX = MAX_BYTES / BLOCK_BYTES;
	// 强制分块请求
	private boolean RANGE_MUST = true;

	@Override
	protected void options(Request request, Response response) throws Exception {
		response.addHeader(HTTP.Allow, "OPTIONS, GET, HEAD, TRACE");
		response.setStatus(HTTPStatus.OK);
	}

	@Override
	protected void delete(Request request, Response response) throws Exception {
		response.addHeader(HTTP.Allow, "OPTIONS, GET, HEAD, TRACE");
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	protected void head(Request request, Response response) throws Exception {
		// execute(request, response, false);
		locate(request, response, false);
	}

	@Override
	protected void get(Request request, Response response) throws Exception {
		// execute(request, response, true);
		locate(request, response, true);
	}

	@Override
	protected void put(Request request, Response response) throws Exception {
		response.addHeader(HTTP.Allow, "OPTIONS, GET, HEAD, TRACE");
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	@Override
	protected void patch(Request request, Response response) throws Exception {
		response.addHeader(HTTP.Allow, "OPTIONS, GET, HEAD, TRACE");
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	/**
	 * 资源定位
	 */
	protected void locate(Request request, Response response, boolean content) throws IOException {
		WEBResource resource = find(request.getPath());
		if (resource == null) {
			// 资源未找到
			response.setStatus(HTTPStatus.NOT_FOUND);
			// 查找错误页面
			resource = find(HTTPStatus.NOT_FOUND);
			if (resource != null) {
				whole(request, response, resource, content);
			}
		} else if (resource.getETag() == null) {
			// 缺失ETag则认为无法定位资源或为目录
			if (resource.getContentLocation() == null) {
				// Multiple Files
				response.setStatus(HTTPStatus.MULTIPLE_CHOICE);
				response.addHeader(ContentType.NAME, MIMEType.TEXT_HTML);
				response.addHeader(HTTP.Alternates, resource.getContentType());
				response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
			} else {
				// DIR / DIR Browse
				if (request.pathLength() != resource.getContentLocation().length()) {
					// 目录重定向 /dir 且存在 重定向 /dir/
					response.setStatus(HTTPStatus.MOVED_PERMANENTLY);
					response.addHeader(HTTP.Location, resource.getContentLocation());
				} else {
					if (resource.getContentType() == null) {
						// 不允许浏览目录
						response.setStatus(HTTPStatus.NOT_FOUND);
					} else {
						// 列出子目录和文件
						response.setStatus(HTTPStatus.OK);
						response.addHeader(ContentType.NAME, resource.getContentType());
						whole(request, response, resource, content);
					}
				}
			}
		} else {
			// 资源正常处理
			output(request, response, resource, content);
		}
	}

	/**
	 * 资源输出，将根据请求头处理资源输出方式；设置响应状态和响应头
	 * 
	 * <pre>
	 * Content-Type: text/html
	 * Cache-Control: no-cache
	 * Content-Language: *
	 * Content-Location: *
	 * Last-Modified: *
	 * ETag: *
	 * </pre>
	 */
	protected void output(Request request, Response response, WEBResource resource, boolean content) throws IOException {

		// 公共头部分
		response.addHeader(ContentType.NAME, resource.getContentType());
		response.addHeader(CacheControl.NAME, CacheControl.NO_CACHE);
		response.addHeader(HTTP.Content_Language, resource.getContentLanguage());
		response.addHeader(HTTP.Content_Location, resource.getContentLocation());
		response.addHeader(HTTP.Last_Modified, resource.getLastModified());
		response.addHeader(ETag.NAME, resource.getETag());

		// ETAG不同则返回资源 RFC7232
		String value = request.getHeader(HTTP.If_None_Match);
		if (Utility.noEmpty(value)) {
			if (Utility.equal(value, resource.getETag())) {
				response.setStatus(HTTPStatus.NOT_MODIFIED);
			} else {
				response.setStatus(HTTPStatus.OK);
				whole(request, response, resource, content);
			}
			return;
		}

		// ETAG相同则返回资源 RFC7232
		value = request.getHeader(HTTP.If_Match);
		if (Utility.noEmpty(value)) {
			if (Utility.equal(value, resource.getETag())) {
				response.setStatus(HTTPStatus.OK);
				whole(request, response, resource, content);
			} else {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
			}
			return;
		}

		// 修改时间有更新返回文件内容
		value = request.getHeader(HTTP.If_Modified_Since);
		if (Utility.noEmpty(value)) {
			if (Utility.equal(value, resource.getLastModified())) {
				response.setStatus(HTTPStatus.NOT_MODIFIED);
			} else {
				response.setStatus(HTTPStatus.OK);
				whole(request, response, resource, content);
			}
			return;
		}

		// 修改时间未更新返回文件内容
		value = request.getHeader(HTTP.If_Unmodified_Since);
		if (Utility.noEmpty(value)) {
			if (Utility.equal(value, resource.getLastModified())) {
				response.setStatus(HTTPStatus.OK);
				whole(request, response, resource, content);
			} else {
				response.setStatus(HTTPStatus.PRECONDITION_FAILED);
			}
			return;
		}

		// RANGE部分请求
		final Range range = Range.parse(request.getHeader(Range.NAME));
		if (range != null) {
			value = request.getHeader(HTTP.If_Range);
			if (Utility.noEmpty(value)) {
				// Last-Modified/ETag相同时Range生效
				if (Utility.equal(value, resource.getETag())) {
					parts(request, response, resource, range, content);
				} else
				// Last-Modified/ETag相同时Range生效
				if (Utility.equal(value, resource.getLastModified())) {
					parts(request, response, resource, range, content);
				} else {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, resource, content);
				}
			} else {
				parts(request, response, resource, range, content);
			}
			return;
		}

		// 其它情况
		response.setStatus(HTTPStatus.OK);
		whole(request, response, resource, content);
	}

	/**
	 * 此方法已被拆分为 locate 和 output 方法
	 */
	@Deprecated
	protected void execute(Request request, Response response, boolean content) throws IOException {
		final String path = request.getPath();
		WEBResource resource = find(path);
		if (resource == null) {
			// 资源未找到
			response.setStatus(HTTPStatus.NOT_FOUND);
			// 查找错误页面
			resource = find(HTTPStatus.NOT_FOUND);
			if (resource != null) {
				response.addHeader(ContentType.NAME, resource.getContentType());
				whole(request, response, resource, content);
			}
		} else {
			if (resource.getETag() == null) {
				// 缺失ETag则认为无法定位资源或为目录
				if (resource.getContentLocation() == null) {
					// Multiple Files
					response.setStatus(HTTPStatus.MULTIPLE_CHOICE);
					response.addHeader(ContentType.NAME, MIMEType.TEXT_HTML);
					response.addHeader(HTTP.Alternates, resource.getContentType());
					response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
				} else {
					// DIR / DIR Browse
					if (path.length() != resource.getContentLocation().length()) {
						// 目录重定向 /dir 且存在 重定向 /dir/
						response.setStatus(HTTPStatus.MOVED_PERMANENTLY);
						response.addHeader(HTTP.Location, resource.getContentLocation());
					} else {
						// 列出子目录和文件
						response.setStatus(HTTPStatus.OK);
						response.addHeader(ContentType.NAME, resource.getContentType());
						whole(request, response, resource, content);
					}
				}
				return;
			}

			// 公共头部分
			response.addHeader(ContentType.NAME, resource.getContentType());
			response.addHeader(HTTP.Content_Language, resource.getContentLanguage());
			response.addHeader(HTTP.Content_Location, resource.getContentLocation());
			response.addHeader(CacheControl.NAME, CacheControl.NO_CACHE);
			response.addHeader(HTTP.Last_Modified, resource.getLastModified());
			response.addHeader(ETag.NAME, resource.getETag());

			// ETAG不同则返回资源 RFC7232
			String value = request.getHeader(HTTP.If_None_Match);
			if (Utility.noEmpty(value)) {
				if (Utility.equal(value, resource.getETag())) {
					response.setStatus(HTTPStatus.NOT_MODIFIED);
				} else {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, resource, content);
				}
				return;
			}

			// ETAG相同则返回资源 RFC7232
			value = request.getHeader(HTTP.If_Match);
			if (Utility.noEmpty(value)) {
				if (Utility.equal(value, resource.getETag())) {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, resource, content);
				} else {
					response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				}
				return;
			}

			// 修改时间有更新返回文件内容
			value = request.getHeader(HTTP.If_Modified_Since);
			if (Utility.noEmpty(value)) {
				if (Utility.equal(value, resource.getLastModified())) {
					response.setStatus(HTTPStatus.NOT_MODIFIED);
				} else {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, resource, content);
				}
				return;
			}

			// 修改时间未更新返回文件内容
			value = request.getHeader(HTTP.If_Unmodified_Since);
			if (Utility.noEmpty(value)) {
				if (Utility.equal(value, resource.getLastModified())) {
					response.setStatus(HTTPStatus.OK);
					whole(request, response, resource, content);
				} else {
					response.setStatus(HTTPStatus.PRECONDITION_FAILED);
				}
				return;
			}

			// RANGE部分请求
			final Range range = Range.parse(request.getHeader(Range.NAME));
			if (range != null) {
				value = request.getHeader(HTTP.If_Range);
				if (Utility.noEmpty(value)) {
					// Last-Modified/ETag相同时Range生效
					if (Utility.equal(value, resource.getETag())) {
						parts(request, response, resource, range, content);
					} else
					// Last-Modified/ETag相同时Range生效
					if (Utility.equal(value, resource.getLastModified())) {
						parts(request, response, resource, range, content);
					} else {
						response.setStatus(HTTPStatus.OK);
						whole(request, response, resource, content);
					}
				} else {
					parts(request, response, resource, range, content);
				}
				return;
			}

			// 其它情况
			response.setStatus(HTTPStatus.OK);
			whole(request, response, resource, content);
		}
	}

	/**
	 * 响应全部内容，并设定与内容相关 HTTP Header；
	 * 
	 * <pre>
	 * Accept-Ranges: bytes
	 * Content-Type: text/html
	 * Content-Encoding: br/gzip/deflate
	 * Content-Length: 9
	 * Transfer-Encoding: chunked
	 * </pre>
	 */
	private final void whole(Request request, Response response, WEBResource resource, boolean content) throws IOException {
		final AcceptEncoding acceptEncoding = AcceptEncoding.parse(request.getHeader(AcceptEncoding.NAME));
		final String encoding = resource.fitEncoding(acceptEncoding);
		final long length = resource.getLength(encoding);

		// Content-Encoding: br/gzip/deflate
		response.addHeader(ContentEncoding.NAME, encoding);

		if (length < HTTPCoder.BLOCK_BYTES) {
			// Content-Length:9
			response.addHeader(ContentLength.NAME, Long.toString(length));
		} else {
			// Transfer-Encoding: chunked
			response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
			// Accept-Ranges: bytes
			response.addHeader(HTTP.Accept_Ranges, Range.UNIT);
		}
		if (content) {
			response.setContent(resource.getData(encoding));
		}
	}

	/**
	 * 响应部分内容，并设定与内容相关 HTTP Header；
	 * 
	 * <pre>
	 * 206
	 * Accept-Ranges: bytes
	 * Content-Encoding: br/gzip/deflate
	 * Content-Range: bytes 200-1000/67589
	 * Content-Length: 9
	 * </pre>
	 * 
	 * <pre>
	 * 206
	 * Accept-Ranges: bytes
	 * Content-Type: multipart/byteranges; boundary=something
	 * Content-Encoding: br/gzip/deflate
	 * Content-Length: 9
	 * </pre>
	 */
	private final void parts(Request request, Response response, WEBResource resource, Range range, boolean content) throws IOException {
		final AcceptEncoding acceptEncoding = AcceptEncoding.parse(request.getHeader(AcceptEncoding.NAME));
		final String encoding = resource.fitEncoding(acceptEncoding);
		final long length = resource.getLength(encoding);

		if (RANGE_MUST || length > BLOCK_BYTES) {
			// 单块请求
			if (range.getRanges().size() == 1) {
				final ByteRange byterange = range.getRanges().get(0);
				if (byterange.valid(length, BLOCK_BYTES, MAX_BYTES)) {
					// Accept-Ranges: bytes
					response.addHeader(HTTP.Accept_Ranges, Range.UNIT);
					// Content-Length:9
					response.addHeader(ContentLength.NAME, Long.toString(byterange.getSize()));
					// Content-Encoding: br/gzip/deflate
					response.addHeader(ContentEncoding.NAME, encoding);
					// Content-Range: bytes 200-1000/67589
					response.addHeader(new ContentRange(byterange.getStart(), byterange.getEnd(), length));
					// 206
					response.setStatus(HTTPStatus.PARTIAL_CONTENT);
					if (content) {
						response.setContent(resource.getData(encoding, byterange));
					}
				} else {
					// 416
					response.setStatus(HTTPStatus.RANGE_NOT_SATISFIABLE);
				}
			} else
			// 多块请求
			if (range.getRanges().size() > 1 && range.getRanges().size() < PART_MAX) {
				final ContentType contentType = new ContentType();
				contentType.setType(MIMEType.MULTIPART_BYTERANGES);
				contentType.setBoundary(ContentType.boundary());

				ByteRange bange;
				MultipartRange part;
				final MultipartRanges parts = new MultipartRanges(contentType.getBoundary());
				for (int index = 0; index < range.getRanges().size(); index++) {
					bange = range.getRanges().get(index);
					if (bange.valid(length, BLOCK_BYTES, MAX_BYTES)) {
						// Content-Range: bytes 200-1000/67589
						part = new MultipartRange(length, bange.getStart(), bange.getEnd());
						// Content-Type: text/html
						part.setContentType(resource.getContentType());
						// Content-Encoding: br/gzip/deflate
						part.setContentEncoding(encoding);
						if (content) {
							part.setContent(resource.getData(encoding, bange));
						}
						parts.add(part);
					} else {
						// 416
						response.setStatus(HTTPStatus.RANGE_NOT_SATISFIABLE);
						return;
					}
				}
				// 206
				response.setStatus(HTTPStatus.PARTIAL_CONTENT);
				// Accept-Ranges: bytes
				response.addHeader(HTTP.Accept_Ranges, Range.UNIT);
				// Content-Length:9
				response.addHeader(ContentLength.NAME, Integer.toString(parts.length()));
				// Content-Type: multipart/byteranges;
				response.addHeader(contentType);
				response.setContent(parts);
			} else {
				// 416 无效分部请求
				response.setStatus(HTTPStatus.RANGE_NOT_SATISFIABLE);
			}
		} else {
			// 资源无须分部请求
			// Content-Length:9
			response.addHeader(ContentLength.NAME, Long.toString(length));
			// Content-Encoding: br/gzip/deflate
			response.addHeader(ContentEncoding.NAME, encoding);
			// 200 OK
			response.setStatus(HTTPStatus.OK);
			if (content) {
				response.setContent(resource.getData(encoding));
			}
		}
	}

	protected abstract WEBResource find(String uri);

	protected abstract WEBResource find(HTTPStatus status);
}