/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.http.HTTPSlave;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * Servlet
 * 
 * @author ZhangXi
 * @date 2021年10月9日
 */
public abstract class Servlet {

	public abstract void service(HTTPSlave chain, Request request, Response response) throws Exception;
}