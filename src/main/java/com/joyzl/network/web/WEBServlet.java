/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.web;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.Connection;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.Date;
import com.joyzl.network.http.FormDataCoder;
import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTP1Coder;
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
		if (request.getVersion() == HTTP1.V20 || request.getVersion() == HTTP1.V11 || request.getVersion() == HTTP1.V10) {
			// 将查询参数合并到请求参数中
			QueryCoder.parse(request);
			// 将响应状态默认为 200
			// response.setStatus(HTTPStatus.OK);
			switch (request.getMethod()) {
				case HTTP1.GET:
					get((Request) request, (Response) response);
					break;
				case HTTP1.HEAD:
					head((Request) request, (Response) response);
					break;
				case HTTP1.POST:
					FormDataCoder.read(request);
					post((Request) request, (Response) response);
					break;
				case HTTP1.PUT:
					put((Request) request, (Response) response);
					break;
				case HTTP1.PATCH:
					patch((Request) request, (Response) response);
					break;
				case HTTP1.DELETE:
					delete((Request) request, (Response) response);
					break;
				case HTTP1.TRACE:
					trace((Request) request, (Response) response);
					break;
				case HTTP1.OPTIONS:
					options((Request) request, (Response) response);
					break;
				case HTTP1.CONNECT:
					connect((Request) request, (Response) response);
					break;
				default:
					response.setStatus(HTTPStatus.BAD_REQUEST);
			}

			// 设置响应后关闭标志
			if (Utility.same(Connection.CLOSE, request.getHeader(Connection.NAME))) {
				response.addHeader(Connection.NAME, Connection.CLOSE);
			}
		} else {
			response.setStatus(HTTPStatus.VERSION_NOT_SUPPORTED);
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
		HTTP1Coder.writeCommand(buffer, request);
		HTTP1Coder.writeHeaders(buffer, request);
		response.setContent(buffer);
	}
}