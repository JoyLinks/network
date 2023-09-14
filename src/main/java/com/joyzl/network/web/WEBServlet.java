/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.Date;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * WEBServlet
 * 
 * @author ZhangXi
 * @date 2021年10月15日
 */
public abstract class WEBServlet extends Servlet {

	// HEADERS
	public final static Date DATE = new Date();
	public final static String SERVER = "Server";
	public final static String CONTENT_LANGUAGE = "Content-Language";

	@Override
	public void service(ChainChannel<Message> chain, Request request, Response response) throws Exception {
		switch (request.getMethod()) {
			case GET:
				get((WEBRequest) request, (WEBResponse) response);
				break;
			case HEAD:
				head((WEBRequest) request, (WEBResponse) response);
				break;
			case POST:
				post((WEBRequest) request, (WEBResponse) response);
				break;
			case PUT:
				put((WEBRequest) request, (WEBResponse) response);
				break;
			case DELETE:
				delete((WEBRequest) request, (WEBResponse) response);
				break;
			case TRACE:
				trace((WEBRequest) request, (WEBResponse) response);
				break;
			case OPTIONS:
				options((WEBRequest) request, (WEBResponse) response);
				break;
			case CONNECT:
				connect((WEBRequest) request, (WEBResponse) response);
				break;
			default:
				throw new IllegalStateException("无效Method:" + request.getMethod());
		}
		response(chain, response);
	}

	protected void response(ChainChannel<Message> chain, Response response) {
		if (response == null) {
			// 无回复
		} else {
			if (response.getStatus() <= 0) {
				// 请求被挂起
			} else {
				// 以下默认处理回复发送消息头
				response.addHeader(SERVER, "JOYZL-HTTP");
				response.addHeader(CONTENT_LANGUAGE, "zh-CN");
				response.addHeader(DATE);
				chain.send(response);
			}
		}
	}

	protected void get(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void head(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void post(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void put(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void delete(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void trace(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void options(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}

	protected void connect(WEBRequest request, WEBResponse response) throws Exception {
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
	}
}
