/**
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved. 
 */
package com.joyzl.network.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.joyzl.network.buffer.DataBuffer;

/**
 * HTTP Coder 相关测试
 * 
 * @author simon (ZhangXi TEL:13883833982)
 * @date 2021年5月25日
 */
class TestHTTPCoder {

	final DataBuffer buffer = DataBuffer.instance();

	final Request request = new Request();
	final Response response = new Response();

	@BeforeEach
	void setUp() throws Exception {
		buffer.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testCommand() throws IOException {
		Request request = new Request();
		request.setMethod(HTTP.GET);
		request.setVersion(HTTP.V11);
		request.setURL("SCHEME://HOST:80/PATH?PARAMETERS#ANCHOR");
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);
		assertEquals(request.getMethod(), HTTP.GET);
		assertEquals(request.getVersion(), HTTP.V11);
		assertEquals(request.getURL(), "SCHEME://HOST:80/PATH?PARAMETERS#ANCHOR");
		assertEquals(request.getScheme(), "SCHEME");
		assertEquals(request.getHost(), "HOST");
		assertEquals(request.getPort(), 80);
		assertEquals(request.getPath(), "/PATH");
		assertEquals(request.getQuery(), "?PARAMETERS");
		assertEquals(request.getAnchor(), "#ANCHOR");
		assertEquals(request.pathLength(), 5);
		assertTrue(HTTP.V11 == request.getVersion());
		assertTrue(HTTP.GET == request.getMethod());

		request = new Request();
		request.setMethod(HTTP.POST);
		request.setVersion(HTTP.V11);
		request.setURL("/background.png");
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);
		assertEquals(request.getMethod(), HTTP.POST);
		assertEquals(request.getVersion(), HTTP.V11);
		assertEquals(request.getURL(), "/background.png");
		assertEquals(request.getScheme(), null);
		assertEquals(request.getHost(), null);
		assertEquals(request.getPort(), 0);
		assertEquals(request.getPath(), "/background.png");
		assertEquals(request.getQuery(), null);
		assertEquals(request.getAnchor(), null);
		assertEquals(request.pathLength(), 15);
		assertTrue(HTTP.V11 == request.getVersion());
		assertTrue(HTTP.POST == request.getMethod());

		request = new Request();
		request.setMethod(HTTP.PUT);
		request.setVersion(HTTP.V11);
		request.setURL("http://www.joyzl.org/docs/Web/HTTP/Messages");
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);
		assertEquals(request.getMethod(), HTTP.PUT);
		assertEquals(request.getVersion(), HTTP.V11);
		assertEquals(request.getURL(), "http://www.joyzl.org/docs/Web/HTTP/Messages");
		assertEquals(request.getScheme(), "http");
		assertEquals(request.getHost(), "www.joyzl.org");
		assertEquals(request.getPort(), 0);
		assertEquals(request.getPath(), "/docs/Web/HTTP/Messages");
		assertEquals(request.getQuery(), null);
		assertEquals(request.getAnchor(), null);
		assertEquals(request.pathLength(), 23);
		assertTrue(HTTP.V11 == request.getVersion());
		assertTrue(HTTP.PUT == request.getMethod());

		request = new Request();
		request.setMethod(HTTP.GET);
		request.setVersion(HTTP.V11);
		request.setURL("http://192.168.2.15/a1-test/2/index.html");
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);
		assertEquals(request.getMethod(), HTTP.GET);
		assertEquals(request.getVersion(), HTTP.V11);
		assertEquals(request.getURL(), "http://192.168.2.15/a1-test/2/index.html");
		assertEquals(request.getScheme(), "http");
		assertEquals(request.getHost(), "192.168.2.15");
		assertEquals(request.getPort(), 0);
		assertEquals(request.getPath(), "/a1-test/2/index.html");
		assertEquals(request.getQuery(), null);
		assertEquals(request.getAnchor(), null);
		assertEquals(request.pathLength(), 21);
		assertTrue(HTTP.V11 == request.getVersion());
		assertTrue(HTTP.GET == request.getMethod());

		request = new Request();
		request.setMethod(HTTP.CONNECT);
		request.setVersion(HTTP.V11);
		request.setURL("developer.mozilla.org:80");
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);
		assertEquals(request.getMethod(), HTTP.CONNECT);
		assertEquals(request.getVersion(), HTTP.V11);
		assertEquals(request.getURL(), "developer.mozilla.org:80");
		assertEquals(request.getScheme(), null);
		assertEquals(request.getHost(), "developer.mozilla.org");
		assertEquals(request.getPort(), 80);
		assertEquals(request.getPath(), null);
		assertEquals(request.getQuery(), null);
		assertEquals(request.getAnchor(), null);
		assertEquals(request.pathLength(), 0);
		assertTrue(HTTP.V11 == request.getVersion());
		assertTrue(HTTP.CONNECT == request.getMethod());

		request = new Request();
		request.setMethod(HTTP.OPTIONS);
		request.setVersion(HTTP.V11);
		request.setURL("*");
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);
		assertEquals(request.getMethod(), HTTP.OPTIONS);
		assertEquals(request.getVersion(), HTTP.V11);
		assertEquals(request.getURL(), "*");
		assertEquals(request.getScheme(), null);
		assertEquals(request.getHost(), null);
		assertEquals(request.getPort(), 0);
		assertEquals(request.getPath(), null);
		assertEquals(request.getQuery(), null);
		assertEquals(request.getAnchor(), null);
		assertEquals(request.pathLength(), 0);
		assertTrue(HTTP.V11 == request.getVersion());
		assertTrue(HTTP.OPTIONS == request.getMethod());

		request = new Request();
		request.setMethod("M");
		request.setVersion("V");
		request.setURL("XX");
		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);
		assertEquals(request.getMethod(), "M");
		assertEquals(request.getVersion(), "V");
		assertEquals(request.getURL(), "XX");
		assertEquals(request.getScheme(), null);
		assertEquals(request.getHost(), null);
		assertEquals(request.getPort(), 0);
		assertEquals(request.getPath(), null);
		assertEquals(request.getQuery(), null);
		assertEquals(request.getAnchor(), null);
		assertEquals(request.pathLength(), 0);
	}

	@Test
	void testRequestCommand() throws IOException {
		// 测试样本
		response.setStatus(HTTPStatus.OK);
		response.setVersion("HTTP/1.1");

		HTTPCoder.writeCommand(buffer, response);
		HTTPCoder.readCommand(buffer, response);

		assertEquals(response.getStatus(), HTTPStatus.OK.code());
		assertEquals(response.getText(), HTTPStatus.OK.text());
		assertEquals(response.getVersion(), "HTTP/1.1");
	}

	@Test
	void testResponseCommand() throws IOException {
		// 测试样本
		request.setMethod(HTTP.GET);
		request.setURL("/test");
		request.setVersion("HTTP/1.1");

		HTTPCoder.writeCommand(buffer, request);
		HTTPCoder.readCommand(buffer, request);

		assertEquals(request.getMethod(), HTTP.GET);
		assertEquals(request.getURL(), "/test");
		assertEquals(request.getVersion(), "HTTP/1.1");
	}

	@Test
	void testHeader() throws Exception {
		// 测试样本
		request.addHeader(new Accept("text/html,application/xhtml+xml,application/xml"));
		request.addHeader(new CacheControl(CacheControl.NO_CACHE));
		request.addHeader(new Connection(Connection.KEEP_ALIVE));

		HTTPCoder.writeHeaders(buffer, request);
		HTTPCoder.readHeaders(buffer, request);

		assertEquals(request.getHeader(Accept.NAME), "text/html, application/xhtml+xml, application/xml");
		assertEquals(request.getHeader(CacheControl.NAME), CacheControl.NO_CACHE);
		assertEquals(request.getHeader(Connection.NAME), Connection.KEEP_ALIVE);
	}

	@Test
	void testQuery() {
		// 测试样本
		final String sample = "http://127.0.0.1/code?error1&error2=&save=false&type=CODE_128&width=80&height=30&code=7056436484794495&error3&error4=";
		final Map<String, String[]> parameters = new HashMap<>();

		QueryCoder.parseQuery(sample, parameters);

		assertEquals(parameters.get("save")[0], "false");
		assertEquals(parameters.get("type")[0], "CODE_128");
		assertEquals(parameters.get("width")[0], "80");
		assertEquals(parameters.get("height")[0], "30");
		assertEquals(parameters.get("code")[0], "7056436484794495");

		String url = QueryCoder.makeQuery("http://127.0.0.1/code", parameters);
		QueryCoder.parseQuery(url, parameters);

		assertEquals(parameters.get("save")[0], "false");
		assertEquals(parameters.get("type")[0], "CODE_128");
		assertEquals(parameters.get("width")[0], "80");
		assertEquals(parameters.get("height")[0], "30");
		assertEquals(parameters.get("code")[0], "7056436484794495");
	}

	@Test
	void testString() {
		final StringBuilder builder = new StringBuilder();

		builder.setLength(0);
		builder.append(HTTP.GET);
		assertEquals(HTTP.METHODS.get(builder), HTTP.GET);

		builder.setLength(0);
		builder.append(HTTP.PUT);
		assertEquals(HTTP.METHODS.get(builder), HTTP.PUT);

		builder.setLength(0);
		builder.append(HTTP.HEAD);
		assertEquals(HTTP.METHODS.get(builder), HTTP.HEAD);

		builder.setLength(0);
		builder.append(HTTP.POST);
		assertEquals(HTTP.METHODS.get(builder), HTTP.POST);

		builder.setLength(0);
		builder.append(HTTP.OPTIONS);
		assertEquals(HTTP.METHODS.get(builder), HTTP.OPTIONS);

		builder.setLength(0);
		builder.append(HTTP.CONNECT);
		assertEquals(HTTP.METHODS.get(builder), HTTP.CONNECT);

		builder.setLength(0);
		builder.append(HTTP.DELETE);
		assertEquals(HTTP.METHODS.get(builder), HTTP.DELETE);

		builder.setLength(0);
		builder.append(HTTP.PATCH);
		assertEquals(HTTP.METHODS.get(builder), HTTP.PATCH);

		builder.setLength(0);
		builder.append(HTTP.TRACE);
		assertEquals(HTTP.METHODS.get(builder), HTTP.TRACE);

		builder.setLength(0);
		builder.append("TEST");
		assertEquals(HTTP.METHODS.get(builder), "TEST");

		builder.setLength(0);
		builder.append("HTTP/2.2");
		assertEquals(HTTP.VERSIONS.get(builder), "HTTP/2.2");

		builder.setLength(0);
		builder.append(HTTP.V10);
		assertEquals(HTTP.VERSIONS.get(builder), HTTP.V10);

		builder.setLength(0);
		builder.append(HTTP.V20);
		assertEquals(HTTP.VERSIONS.get(builder), HTTP.V20);

		builder.setLength(0);
		builder.append(HTTP.V11);
		assertEquals(HTTP.VERSIONS.get(builder), HTTP.V11);

		// toString
		long time = System.currentTimeMillis();
		for (int index = 0; index < 1000000; index++) {
			builder.toString();
		}
		time = System.currentTimeMillis() - time;
		System.out.println("toString:" + time);

		// ifString
		time = System.currentTimeMillis();
		for (int index = 0; index < 1000000; index++) {
			HTTP.VERSIONS.get(builder);
		}
		time = System.currentTimeMillis() - time;
		System.out.println("ifString:" + time);
	}

	@Test
	void testPercentCode() throws IOException {
		HTTPCoder.percentEncode(buffer, "≡Say what‽", false);
		assertEquals(HTTPCoder.toString(buffer), "%E2%89%A1Say%20what%E2%80%BD");

		final StringBuilder builder = new StringBuilder();
		int c;
		while (buffer.readable() > 0) {
			c = buffer.readByte();
			if (c == '%') {
				HTTPCoder.percentDecode(buffer, builder);
			} else {
				builder.append((char) c);
			}
		}
		assertEquals(builder.toString(), "≡Say what‽");
	}

	private final static ConcurrentLinkedQueue<Request> REQUESTS = new ConcurrentLinkedQueue<>();

	@Test
	void cacheRequestAndResponse() {
		REQUESTS.add(new Request());
		final int size = 100000;
		Request request;

		long time = System.currentTimeMillis();
		for (int index = 0; index < size; index++) {
			request = REQUESTS.poll();
			if (request != null) {
				REQUESTS.add(request);
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("CACHE:" + time);

		time = System.currentTimeMillis();
		for (int index = 0; index < size; index++) {
			request = new Request();
			if (request != null) {
				;
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("CREATE:" + time);
	}
}