/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.chain.ChainChannel;

/**
 * HTTP Servlet
 * 
 * @author ZhangXi
 * @date 2021年10月9日
 */
public abstract class HTTPServlet {

	// METHODS
	public final static String GET = "GET";
	public final static String HEAD = "HEAD";
	public final static String POST = "POST";
	public final static String PUT = "PUT";
	public final static String DELETE = "DELETE";
	public final static String CONNECT = "CONNECT";
	public final static String OPTIONS = "OPTIONS";
	public final static String TRACE = "TRACE";

	public abstract void service(ChainChannel<Message> chain, Request request, Response response) throws Exception;
}