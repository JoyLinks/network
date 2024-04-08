/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.IOException;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;

/**
 * WEB SERVER
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBServerBase extends WEBServerHandler {

	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();
	private WEBServer server;

	public WEBServerBase() {
	}

	public void start(String host, int port) throws IOException {
		server = new WEBServer(this, host, port);
	}

	public void close() {
		if (server != null) {
			server.close();
			server = null;
		}
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		final WEBSlave slave = (WEBSlave) chain;
		final WEBRequest request = (WEBRequest) message;
		final WEBResponse response = slave.getResponse();

		request.setURI(HTTPCoder.parseQuery(request.getURI(), request.getParametersMap()));
		final Servlet servlet = SERVLETS.find(request.getURI());
		if (servlet == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
			chain.send(response);
		} else {
			try {
				servlet.service(chain, request, response);
			} catch (Exception e) {
				response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
				chain.send(response);
				error(chain, e);
			}
		}
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		super.error(chain, e);
	}

	public Wildcards<Servlet> getServlets() {
		return SERVLETS;
	}
}