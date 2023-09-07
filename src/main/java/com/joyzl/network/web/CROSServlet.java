/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 基于CROS请求/响应的Servlet
 *
 * @author ZhangXi
 * @date 2020年7月30日
 */
public abstract class CROSServlet extends WEBServlet {

	/** REQUEST RFC6454 */
	public final static String ORIGIN = "Origin";
	public final static String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public final static String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
	public final static String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	public final static String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	public final static String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
	public final static String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	public final static String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
	public final static String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

	@Override
	public void service(ChainChannel<Message> chain, Request request, Response response) throws Exception {
		cros(request, response);
		super.service(chain, request, response);
	}

	@Override
	protected final void options(WEBRequest request, WEBResponse response) {
		// 提供默认跨域请求能力
		response.setStatus(HTTPStatus.OK);
		// 是否允许发送Cookie
		response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
		// 允许的请求METHOD
		response.addHeader(ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE,OPTIONS");
		// 允许的HEADER
		// 默认:Accept,Accept-Language,Content-Language,Content-Type
		// Authorization标头不能使用通配符，并且始终需要明确列出。
		response.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, "*,Authorization");
		// 允许响应的HEADER
		// 默认:Cache-Control,Content-Language,Content-Length,Content-Type,Expires,Last-Modified,Pragma
		response.addHeader(ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization");
		// 允许的请求源
		response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		// 允许的有效期
		response.addHeader(ACCESS_CONTROL_MAX_AGE, "7200"/* 2小时有效 */);

		// 注意：Authorization是常用的HTTP令牌和身份校验方式；
		// 如果不明确列出跨域支持将导致每次浏览器请求都会事先发送OPTIONS预检请求
		// 正常情况下在指定的时间内Access-Control-Max-Age仅需要发送一次预检请求
	}

	protected final void cros(Request request, Response response) {
		final String origin = request.getHeader(ORIGIN);
		if (origin == null) {
			response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		} else {
			response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
		}
		response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
	}
}
