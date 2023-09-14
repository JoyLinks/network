/**
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved. 
 */
package com.joyzl.network.http.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.http.Accept;
import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.CacheControl;
import com.joyzl.network.http.Connection;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.web.Servlet;

/**
 * HTTP Coder 相关测试
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年5月25日
 */
class TestHTTPCoder {

	final DataBuffer buffer = DataBuffer.instance();
	final HTTPWriter writer = new HTTPWriter(buffer);
	final HTTPReader reader = new HTTPReader(buffer);

	final Request request = new Request();
	final Response response = new Response();

	@Test
	void testRequestCommand() throws IOException {
		// 测试样本
		response.setStatus(HTTPStatus.OK);
		response.setVersion("HTTP/1.1");

		HTTPCoder.writeCommand(writer, response);
		HTTPCoder.readCommand(reader, response);

		assertEquals(response.getStatus(), HTTPStatus.OK.code());
		assertEquals(response.getText(), HTTPStatus.OK.text());
		assertEquals(response.getVersion(), "HTTP/1.1");
	}

	@Test
	void testResponseCommand() throws IOException {
		// 测试样本
		request.setMethod(Servlet.GET);
		request.setURI("/test");
		request.setVersion("HTTP/1.1");

		HTTPCoder.writeCommand(writer, request);
		HTTPCoder.readCommand(reader, request);

		assertEquals(request.getMethod(), Servlet.GET);
		assertEquals(request.getURI(), "/test");
		assertEquals(request.getVersion(), "HTTP/1.1");
	}

	@Test
	void testHeader() throws Exception {
		// 测试样本
		request.addHeader(new Accept("text/html,application/xhtml+xml,application/xml"));
		request.addHeader(new Authorization(Authorization.BASIC, "ABCDEFGHIJKLMNOPQRSTUVWXTZ"));
		request.addHeader(new CacheControl(CacheControl.NO_CACHE));
		request.addHeader(new Connection(Connection.KEEP_ALIVE));

		HTTPCoder.writeHeaders(writer, request);
		HTTPCoder.readHeaders(reader, request);

		assertEquals(request.getHeader(Accept.NAME), "text/html, application/xhtml+xml, application/xml");
		assertEquals(request.getHeader(Authorization.NAME), "Basic ABCDEFGHIJKLMNOPQRSTUVWXTZ");
		assertEquals(request.getHeader(CacheControl.NAME), CacheControl.NO_CACHE);
		assertEquals(request.getHeader(Connection.NAME), Connection.KEEP_ALIVE);
	}

	@Test
	void testQuery() {
		// 测试样本
		final String sample = "http://127.0.0.1/code?error1&error2=&save=false&type=CODE_128&width=80&height=30&code=7056436484794495&error3&error4=";
		final Map<String, String[]> parameters = new HashMap<>();

		HTTPCoder.parseQuery(sample, parameters);

		assertEquals(parameters.get("save")[0], "false");
		assertEquals(parameters.get("type")[0], "CODE_128");
		assertEquals(parameters.get("width")[0], "80");
		assertEquals(parameters.get("height")[0], "30");
		assertEquals(parameters.get("code")[0], "7056436484794495");

		String url = HTTPCoder.makeQuery("http://127.0.0.1/code", parameters);
		HTTPCoder.parseQuery(url, parameters);

		assertEquals(parameters.get("save")[0], "false");
		assertEquals(parameters.get("type")[0], "CODE_128");
		assertEquals(parameters.get("width")[0], "80");
		assertEquals(parameters.get("height")[0], "30");
		assertEquals(parameters.get("code")[0], "7056436484794495");
	}
}