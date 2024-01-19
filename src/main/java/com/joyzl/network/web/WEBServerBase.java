/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Message;
import com.joyzl.odbs.ODBSReflect;

/**
 * WEB SERVER
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public class WEBServerBase extends WEBServerHandler {

	private final Wildcards<Servlet> SERVLETS = new Wildcards<>();
	private WEBFileServlet fileServlet;
	private WEBServer server;

	public WEBServerBase() {
	}

	public WEBServerBase(String path) {
		SERVLETS.bind("*", fileServlet = new WEBFileServlet(path));
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
		e.printStackTrace();
	}

	/**
	 * 扫描指定包中的Servlet并绑定ServletURI注解指定的URI
	 */
	public void scan(String pkg) throws Exception {
		final List<Class<?>> classes = ODBSReflect.scanClass(pkg);
		for (Class<?> clazz : classes) {
			if (Servlet.class.isAssignableFrom(clazz)) {
				ServletURI annotation = ODBSReflect.findAnnotation(clazz, ServletURI.class);
				if (annotation != null) {
					try {
						Servlet servlet = (Servlet) clazz.getConstructor().newInstance();
						getServlets().bind(annotation.uri(), servlet);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						throw e;
					}
				}
			}
		}
	}

	public Wildcards<Servlet> getServlets() {
		return SERVLETS;
	}

	public WEBFileServlet getFileServlet() {
		return fileServlet;
	}
}