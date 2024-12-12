/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 基于 CROS RFC6454 请求/响应的 Servlet
 *
 * @author ZhangXi
 * @date 2020年7月30日
 */
public abstract class CROSServlet extends WEBServlet {

	public final static String ANY = "*";

	@Override
	public void service(HTTPSlave chain, Request request, Response response) throws Exception {
		checkOrigin(request, response);
		super.service(chain, request, response);
	}

	@Override
	protected void options(Request request, Response response) {
		// 预检请求始终返回 200
		response.setStatus(HTTPStatus.OK);

		// Access-Control-Request-Headers: name, name
		// 浏览器预检请求时报告可能发送的标头
		// value = request.getHeader(HTTP.Access_Control_Request_Headers);
		// Access-Control-Request-Method: GET
		// 浏览器预检请求时报告可能使用的方法
		// value = request.getHeader(HTTP.Access_Control_Request_Method);

		response.addHeader(HTTP.Access_Control_Allow_Methods, allowMethods());
		response.addHeader(HTTP.Access_Control_Allow_Headers, allowHeaders());
		response.addHeader(HTTP.Access_Control_Expose_Headers, exposeHeaders());
		response.addHeader(HTTP.Access_Control_Max_Age, maxAge());
		if (allowCredentials()) {
			response.addHeader(HTTP.Access_Control_Allow_Credentials, "true");
		}
	}

	protected void checkOrigin(Request request, Response response) {
		String value = request.getHeader(HTTP.Origin);
		if (Utility.equal(ANY, allowOrigin())) {
			// 宽松默认，如果允许所有，即便缺失Origin标头也通过
			// 客户端程序在不严格时缺失Origin标头
			response.addHeader(HTTP.Access_Control_Allow_Origin, ANY);
		} else {
			if (Utility.noEmpty(value)) {
				if (allowOrigin() != null) {
					if (value.contains(allowOrigin())) {
						response.addHeader(HTTP.Access_Control_Allow_Origin, value);
					}
				}
			}
		}
	}

	/**
	 * 跨源请求是否允许携带凭据 Cookie、TLS，不允许时不发送此标头，默认不允许(false)
	 * 
	 * <pre>
	 * Access-Control-Allow-Credentials: true
	 * </pre>
	 */
	protected boolean allowCredentials() {
		return false;
	}

	/**
	 * 跨源请求允许的标头，默认白名单的标头：Accept,Accept-Language,Content-Language,Content-Type,Range，无须列出；
	 * Authorization标头如果允许始终要列出，即便已有 "*"
	 * 
	 * <pre>
	 * Access-Control-Allow-Headers: *
	 * </pre>
	 */
	protected String allowHeaders() {
		return ANY;
	}

	/**
	 * 跨源请求允许暴露给浏览器脚本的标头，白名单的标头默认暴露
	 * 
	 * <pre>
	 * Access-Control-Expose-Headers: *
	 * Access-Control-Expose-Headers: *, Authorization
	 * </pre>
	 */
	protected String exposeHeaders() {
		return ANY;
	}

	/**
	 * 跨源请求允许的方法
	 * 
	 * <pre>
	 * Access-Control-Allow-Methods: *
	 * </pre>
	 */
	protected String allowMethods() {
		return "OPTIONS, GET, POST";
	}

	/**
	 * 跨源请求允许的来源，请求有凭据时不能响应"*"
	 * 
	 * <pre>
	 * Access-Control-Allow-Origin: *
	 * Access-Control-Allow-Origin: https://www.joyzl.com
	 * Vary: Origin
	 * </pre>
	 */
	protected String allowOrigin() {
		return ANY;
	}

	/**
	 * 跨源请求预检结果有效时间（秒），超过此时间浏览器将再次发送OPTIONS预检请求，默认7200(2 hour)
	 * 
	 * <pre>
	 * Access-Control-Max-Age: 7200
	 * </pre>
	 */
	protected String maxAge() {
		return "7200";
	}
}