/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
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