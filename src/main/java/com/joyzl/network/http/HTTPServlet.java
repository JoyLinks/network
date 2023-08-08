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

	public abstract void service(ChainChannel<Message> chain, Request request, Response response) throws Exception;
}