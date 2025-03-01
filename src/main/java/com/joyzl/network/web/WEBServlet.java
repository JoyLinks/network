/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Date;
import com.joyzl.network.http.FormDataCoder;
import com.joyzl.network.http.HTTP;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.QueryCoder;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.Server;

/**
 * WEBServlet
 * 
 * @author ZhangXi
 * @date 2021年10月15日
 */
public abstract class WEBServlet extends Servlet {

	// HEADERS
	public final static Date DATE = new Date();
	public final static Server SERVER = new Server();

	@Override
	public void service(HTTPSlave chain, Request request, Response response) throws Exception {
		if (request.getVersion() != HTTP.V11 && request.getVersion() != HTTP.V10) {
			response.setStatus(HTTPStatus.VERSION_NOT_SUPPORTED);
		} else {
			// 将查询参数合并到请求参数中
			QueryCoder.parse(request);
			// 将响应状态默认为 200
			// response.setStatus(HTTPStatus.OK);
			switch (request.getMethod()) {
				case HTTP.GET:
					get((Request) request, (Response) response);
					break;
				case HTTP.HEAD:
					head((Request) request, (Response) response);
					break;
				case HTTP.POST:
					FormDataCoder.read(request);
					post((Request) request, (Response) response);
					break;
				case HTTP.PUT:
					put((Request) request, (Response) response);
					break;
				case HTTP.PATCH:
					patch((Request) request, (Response) response);
					break;
				case HTTP.DELETE:
					delete((Request) request, (Response) response);
					break;
				case HTTP.TRACE:
					trace((Request) request, (Response) response);
					break;
				case HTTP.OPTIONS:
					options((Request) request, (Response) response);
					break;
				case HTTP.CONNECT:
					connect((Request) request, (Response) response);
					break;
				default:
					response.setStatus(HTTPStatus.BAD_REQUEST);
			}
		}
		response(chain, response);
	}

	protected void response(ChainChannel chain, Response response) {
		if (response.getStatus() > 0) {

			// 以下默认处理回复发送消息头
			response.addHeader(SERVER);
			response.addHeader(DATE);
		} else {
			// 请求被挂起
		}
	}

	protected void get(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void head(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void post(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void put(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void patch(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void delete(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void options(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void connect(Request request, Response response) throws Exception {
		response.setStatus(HTTPStatus.NOT_IMPLEMENTED);
	}

	protected void trace(Request request, Response response) throws Exception {
		// 响应内容类型为 ContentType: message/http
		response.addHeader(ContentType.NAME, MIMEType.MESSAGE_HTTP);
		// Test ca531a5 需求为 CHUNKED
		// response.addHeader(TransferEncoding.NAME, TransferEncoding.CHUNKED);
		// 将请求首行和头部作为内容原样返回
		final DataBuffer buffer = DataBuffer.instance();
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.writeHeaders(buffer, request);
		response.setContent(buffer);
	}
}